package com.zj.w3x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessHandlerDefaultImpl implements ProcessHandler {
	
	private Logger logger = LoggerFactory.getLogger(ProcessHandlerDefaultImpl.class);
	private final static String DEFAULT_SPLIT_SPLITER = "-----------------------------------------------------------------------";
	private final static String ENCODING = "gbk";
	public final static String DEFAULT_DB_FILE = "D:\\w3x.db";
	public final static String DEFAULT_IMAGE_SAVE_PATH = "E:\\flim\\ceshi";

	@Override
	public List<String> split(String source) {
		if (!source.contains(DEFAULT_SPLIT_SPLITER)) { 
			return null;
		}
		
		// TODO zhaojie Do not use spliter, use <img>
		return Arrays.asList(source.split(DEFAULT_SPLIT_SPLITER));
	}

	@Override
	public Film resolve(String filmSource) {
			Film bean = new Film();
			Document d = Jsoup.parse(filmSource);
			
			List<Image> imageURLs = new ArrayList<Image>();
			for (Element img : d.select("img")) {
				String imgSrc = img.attr("src");
				imageURLs.add(new Image(imgSrc, StringUtils.EMPTY));
			}
			bean.setImages(imageURLs);
			
			List<String> links = new ArrayList<String>();
			for (Element link : d.select("a")) {
				String l = link.attr("href");
				links.add(l);
			}
			bean.setDownloadLinks(links);
			bean.setFilmSource(filmSource);
			
			return bean;			
	}
	
	private void createTable(Connection conn) throws SQLException {
		StringBuilder createDownloadLinkSQL = new StringBuilder();
		createDownloadLinkSQL.append("CREATE TABLE [DOWNLOAD_LINK] ( ");
		createDownloadLinkSQL.append("[ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		createDownloadLinkSQL.append("[DL_HEADER_ID] CHAR NOT NULL, ");
		createDownloadLinkSQL.append("[DL_LINK] TEXT); ");
		
		StringBuilder createFilmSQL = new StringBuilder();
		createFilmSQL.append("CREATE TABLE [FILM] ( ");
		createFilmSQL.append("[ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		createFilmSQL.append("[FM_DATE] TIMESTAMP NOT NULL DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), ");
		createFilmSQL.append("[FM_DESC] TEXT); ");
		
		StringBuilder createImageSQL = new StringBuilder();
		createImageSQL.append("CREATE TABLE [IMAGE] ( ");
		createImageSQL.append("[ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		createImageSQL.append("[IMG_HEADER_ID] INTEGER NOT NULL, ");
		createImageSQL.append("[IMG_PATH] TEXT, ");
		createImageSQL.append("[IMG_LOCAL_PATH] TEXT);");

		Statement stmt = conn.createStatement();
		for (String sql : new String[] {createDownloadLinkSQL.toString(), createImageSQL.toString(), createFilmSQL.toString()}) {
			stmt.executeUpdate(sql);
		}
		stmt.close();
	}
	
	private void checkOrInitTable(Connection conn) throws SQLException {
		String sqlCheckTable = "SELECT COUNT(*) AS TBL_CNT FROM SQLITE_MASTER WHERE NAME IN ('FILM', 'DOWNLOAD_LINK', 'IMAGE');";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sqlCheckTable);
		if (rs.next()) {
			int tableCnt = rs.getInt("TBL_CNT");
			if (tableCnt != 3) {
				createTable(conn);
			}
		}
	}
	
	private File checkSavePath() {
		File folder = new File(DEFAULT_IMAGE_SAVE_PATH);
		if (!folder.exists()) folder.mkdir();
		return folder;
	}
	
	@Override
	public void beforeSave(List<Film> beans) throws IOException {
		downloadImageUsingMultiThread(beans);
	}
	
	private void downloadImageUsingMultiThread(List<Film> beans) throws IOException {
		W3XHelper.multiDownload(beans, 5000, 20000);
		saveImage(beans);
	}
	
	private void saveImage(List<Film> beans) throws IOException {
		File parentFolder = checkSavePath();
		for (Film bean : beans) {
			for (Image img : bean.getImages()) {
				byte[] binary = img.getBinary();
				if (binary != null) {
					File localDest = new File(parentFolder, FilenameUtils.getName(img.getNeturl()));
					FileUtils.writeByteArrayToFile(localDest, binary);
				}
			}
		}
	}
	
	@Override
	public void save(List<Film> films) {
		
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DEFAULT_DB_FILE);
			
			checkOrInitTable(conn);
			
//			conn.setAutoCommit(false);
			PreparedStatement prepfilm = conn.prepareStatement(
					"INSERT INTO FILM(FM_DESC) VALUES(?);");
			PreparedStatement preplink = conn.prepareStatement(
					"INSERT INTO DOWNLOAD_LINK(DL_HEADER_ID, DL_LINK) VALUES(?, ?);");
			PreparedStatement prepimage = conn.prepareStatement(
					"INSERT INTO IMAGE(IMG_HEADER_ID, IMG_PATH, IMG_LOCAL_PATH) VALUES(?, ?, ?);");
			PreparedStatement prepheaderid = conn.prepareStatement(
					"SELECT last_insert_rowid() AS 'ID' FROM FILM;");
			
			logger.debug("Saving... total films: " + films.size());
			for (Film f : films) {
				
				prepfilm.clearBatch();
				prepfilm.clearParameters();
//					prepfilm.setDate(1, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				prepfilm.setString(1, StringUtils.join(f.getDesc(), "\n"));
//				logger.debug("Save 1 record into FILM...");
				prepfilm.executeUpdate();
				ResultSet rs = prepheaderid.executeQuery();
				long headerid = rs.getLong("ID");
				
				preplink.clearBatch();
				preplink.clearParameters();
				for (String link : f.getDownloadLinks()) {
					preplink.setLong(1, headerid);
					preplink.setString(2, link);
//					logger.debug("Save 1 record into DOWNLOAD_LINK...");
					preplink.addBatch();
				}
				preplink.executeBatch();
				
				prepimage.clearBatch();
				prepimage.clearParameters();
				for (Image imgUrl : f.getImages()) {
					prepimage.setLong(1, headerid);
					prepimage.setString(2, imgUrl.getNeturl());
					prepimage.setString(3, imgUrl.getLocalpath());
//					logger.debug("Save 1 record into IMAGE...");
					prepimage.addBatch();
				}
				prepimage.executeBatch();
				
				JDBCHelper.closeResultSet(rs);
			}
			
			logger.debug("Commit...");
//			conn.commit();
			logger.debug("Commit successful.");
			
			// TODO zhaojie Use DbUtils
			JDBCHelper.closeStatements(new Statement[] {prepfilm, preplink, prepimage, prepheaderid});
			JDBCHelper.closeConnection(conn);
		}
		catch (SQLException e) {
			logger.debug(e.getMessage(), e);
		}
		catch (ClassNotFoundException e) {
			logger.debug(e.getMessage(), e);
		}
	}

	@Override
	public String getEncoding() {
		return ENCODING;
	}

	@Override
	public void beforeDownload(String url, Map<String, String> param) {
		if (!url.equals(W3X.URL_TODAY)) {
			param.put("Referer", W3X.URL_TODAY);
		}
	}
}
