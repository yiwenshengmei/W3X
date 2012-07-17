package com.zj.w3x.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class W3X {
	
	Logger logger = LoggerFactory.getLogger(W3X.class);
	
	public final static String DEFAULT_URL_TODAY     = "http://74.55.154.143/index1.html";
	public final static String DEFAULT_URL_YESTERDAY = "http://74.55.154.143/index2.html";
	public final static String DEFAULT_URL_BEFORE_YESTERDAY = "http://74.55.154.143/index3.html";
	public final static String[] DEFAULT_URL_PACK = new String[] {
		DEFAULT_URL_TODAY, DEFAULT_URL_YESTERDAY, DEFAULT_URL_BEFORE_YESTERDAY
	};
	public final static String DEFAULT_DB_FILE = "x3.db";
	public final static String DEFAULT_SOURCE_PATH = "C:\\";
	public final static String DEFAULT_AFTER_CLEAN_PATH = "C:\\w3x(%1$s).html";
	public final static String DEFAULT_ENCODING = "gbk";
	public final static String DEFAULT_SPLIT_SPLITER = "-----------------------------------------------------------------------";
	
	private CleanerProperties props;
	private PrettyXmlSerializer xmlSerializer;
	private HtmlCleaner cleaner;
	
	public W3X() {
		props = new CleanerProperties();
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		
		xmlSerializer = new PrettyXmlSerializer(props);
		cleaner = new HtmlCleaner(props);
	}
		
	public Map<String, String> download(String[] urls) {
		int taskCnt = urls.length;
		int successCnt = taskCnt;
		HttpClient http = new DefaultHttpClient();
		HttpGet get = new HttpGet();
		Map<String, String> results = new HashMap<String, String>();
		for (String url : urls) {
			try {
				logger.debug("Begin download " + url);
				get.setURI(new URI(url));
				HttpResponse resp = http.execute(get);
				HttpEntity entity = resp.getEntity();
				logger.debug("Using default encoding: " + DEFAULT_ENCODING);
				String src = IOUtils.toString(entity.getContent(), DEFAULT_ENCODING);
				results.put(url, src);
				logger.debug(url + " download successful.");
			}
			catch (URISyntaxException e) {
				successCnt--;
				logger.debug(e.getMessage(), e);
			}
			catch (ClientProtocolException e) {
				successCnt--;
				logger.debug(e.getMessage(), e);
			}
			catch (IOException e) {
				successCnt--;
				// TODO Can retry
				logger.debug(e.getMessage(), e);
			}
		}
		logger.debug(String.format("%1$s task, %2$s success, %3$s failed.", 
				taskCnt, successCnt, taskCnt - successCnt));
		return results;
	}
	
	public String cleanHtml(String html) throws IOException {
	 
		TagNode tagNode = cleaner.clean(html);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		xmlSerializer.writeToStream(tagNode, out, DEFAULT_ENCODING);
		String ret = out.toString(DEFAULT_ENCODING);
//		logger.debug("After clean:\n" + ret);
		return ret;
	}
	
	public Map<String, List<FilmInfo>> getDataMap(Map<String, String> src) throws IOException {
		Map<String, List<FilmInfo>> ret = new HashMap<String, List<FilmInfo>>();
		for (Entry<String, String> entry : src.entrySet()) {
			
			String content = entry.getValue();
			
			if (!content.contains(DEFAULT_SPLIT_SPLITER)) continue;
			
			// TODO Do not use spliter, use <img>
			String[] tks = content.split(DEFAULT_SPLIT_SPLITER);
			List<FilmInfo> films = new ArrayList<FilmInfo>();
			
			for (String filmSource : tks) {
				FilmInfo info = new FilmInfo();
				Document d = Jsoup.parse(filmSource);
				
				List<String> images = new ArrayList<String>();
				for (Element img : d.select("img")) {
					String imgSrc = img.attr("src");
					images.add(imgSrc);
				}
				info.setImageSrcs(images);
				
				List<String> links = new ArrayList<String>();
				for (Element link : d.select("a")) {
					String l = link.attr("href");
					links.add(l);
				}
				info.setLink(links);
				
				info.setUrl(entry.getKey());
				info.setSource(filmSource);
				films.add(info);
				logger.debug(info.toString());
			}
			ret.put(entry.getKey(), films);
		}
		return ret;
	}
	
	/**
	 * 
	 * @param datamap
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public void saveDataMap(Map<String, List<FilmInfo>> datamap) throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:C:\\x3.db");
			conn.setAutoCommit(false);
			
			PreparedStatement prepfilm = conn.prepareStatement(
					"INSERT INTO FILM(FM_DESC) VALUES(?);");
			PreparedStatement preplink = conn.prepareStatement(
					"INSERT INTO DOWNLOAD_LINK(DL_HEADER_ID, DL_LINK) VALUES(?, ?);");
			PreparedStatement prepimage = conn.prepareStatement(
					"INSERT INTO IMAGE(IMG_HEADER_ID, IMG_PATH) VALUES(?, ?);");
			PreparedStatement prepheaderid = conn.prepareStatement(
					"SELECT last_insert_rowid() as 'ID' FROM FILM;");
			
			
			for (Entry<String, List<FilmInfo>> day : datamap.entrySet()) {
				for (FilmInfo f : day.getValue()) {
					
					// TODO Fill sql
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
			}
			
			logger.debug("Commit...");
			conn.commit();
			logger.debug("Commit successful.");
			
			// TODO Use DbUtils
			JDBCHelper.closeStatements(new Statement[] {prepfilm, preplink, prepimage, prepheaderid});
			JDBCHelper.closeConnection(conn);
		}
		catch (SQLException e) {
			logger.debug(e.getMessage(), e);
		}
	}
}
