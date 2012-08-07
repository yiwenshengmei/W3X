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

public class ProcessHandlerDefImpl implements ProcessHandler {
	
	public Logger logger = LoggerFactory.getLogger(ProcessHandlerDefImpl.class);

	@Override
	public List<String> split(String source) {
		if (!source.contains(W3X.DEFAULT_SPLIT_SPLITER)) { 
			logger.debug("No split_spliter found, return null.");
			return null;
		}
		
		// TODO zhaojie Do not use spliter, use <img>
		return Arrays.asList(source.split(W3X.DEFAULT_SPLIT_SPLITER));
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

	@Override
	public void save(List<FilmBean> beans) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:D:\\w3x.db");;
			conn.setAutoCommit(false);
			
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
			conn.commit();
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
		return W3X.DEFAULT_ENCODING;
	}

}
