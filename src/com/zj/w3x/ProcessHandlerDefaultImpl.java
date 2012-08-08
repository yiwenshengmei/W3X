package com.zj.w3x;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessHandlerDefaultImpl implements ProcessHandler {
	
	private Logger logger = LoggerFactory.getLogger(ProcessHandlerDefaultImpl.class);
	private final static String DEFAULT_SPLIT_SPLITER = "-----------------------------------------------------------------------";
	private final static String ENCODING = "gbk";
	private final static String DEFAULT_DB_FILE = "D:\\w3x.db";

	@Override
	public List<String> split(String source) {
		if (!source.contains(DEFAULT_SPLIT_SPLITER)) { 
			logger.debug("No split_spliter found, return null.");
			return null;
		}
		
		// TODO zhaojie Do not use spliter, use <img>
		return Arrays.asList(source.split(DEFAULT_SPLIT_SPLITER));
	}

	@Override
	public FilmBean resolve(String filmSource) {
			FilmBean bean = new FilmBean();
			Document d = Jsoup.parse(filmSource);
			
			List<String> images = new ArrayList<String>();
			for (Element img : d.select("img")) {
				String imgSrc = img.attr("src");
				images.add(imgSrc);
			}
			bean.setImageSrcs(images);
			
			List<String> links = new ArrayList<String>();
			for (Element link : d.select("a")) {
				String l = link.attr("href");
				links.add(l);
			}
			bean.setLink(links);
			bean.setSource(filmSource);
				
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
		createImageSQL.append("[IMG_PATH] TEXT);");

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

	@Override
	public void save(List<FilmBean> beans) {
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
					"INSERT INTO IMAGE(IMG_HEADER_ID, IMG_PATH) VALUES(?, ?);");
			PreparedStatement prepheaderid = conn.prepareStatement(
					"SELECT last_insert_rowid() as 'ID' FROM FILM;");
			
			for (FilmBean f : beans) {
				
				prepfilm.clearBatch();
				prepfilm.clearParameters();
//					prepfilm.setDate(1, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				prepfilm.setString(1, StringUtils.join(f.getDesc(), "\n"));
				logger.debug("Save 1 record into FILM...");
				prepfilm.executeUpdate();
				ResultSet rs = prepheaderid.executeQuery();
				long headerid = rs.getLong("ID");
				
				preplink.clearBatch();
				preplink.clearParameters();
				for (String link : f.getLink()) {
					preplink.setLong(1, headerid);
					preplink.setString(2, link);
					logger.debug("Save 1 record into DOWNLOAD_LINK...");
					preplink.addBatch();
				}
				preplink.executeBatch();
				
				prepimage.clearBatch();
				prepimage.clearParameters();
				for (String img : f.getImageSrcs()) {
					prepimage.setLong(1, headerid);
					prepimage.setString(2, img);
					logger.debug("Save 1 record into IMAGE...");
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

}
