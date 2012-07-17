package com.zj.w3x.main;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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

import org.apache.commons.io.FileUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class W3X {
	
	Logger logger = LoggerFactory.getLogger(W3X.class);
	
	public final static String DEFAULT_URL_TODAY     = "http://74.55.154.143/index1.html";
	public final static String DEFAULT_URL_YESTERDAY = "http://74.55.154.143/index2.html";
	public final static String DEFAULT_URL_BEFORE_YESTERDAY = "http://74.55.154.143/index3.html";
	public final static String DEFAULT_DB_FILE = "x3.db";
	public final static String DEFAULT_SOURCE_PATH = "C:\\";
	public final static String DEFAULT_AFTER_CLEAN_PATH = "C:\\w3x(%1$s).html";
	public final static String DEFAULT_ENCODING = "utf-8";
	public final static String DEFAULT_SPLIT_SPLITER = "-----------------------------------------------------------------------";
	
	private CleanerProperties props;
	private PrettyXmlSerializer xmlSerializer;
	private HtmlCleaner cleaner;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		

	}
	
	public W3X() {
		props = new CleanerProperties();
		 
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		
		xmlSerializer = new PrettyXmlSerializer(props);
		cleaner = new HtmlCleaner(props);
	}
		
	public Map<String, String> download(String[] urls) {
		HttpClient http = new DefaultHttpClient();
		HttpGet get = new HttpGet();
		Map<String, String> results = new HashMap<String, String>();
		for (String url : urls) {
			boolean isSuccess = false;
			try {
				get.setURI(new URI(url));
				HttpResponse resp = http.execute(get);
				HttpEntity entity = resp.getEntity();
				String src = IOUtils.toString(entity.getContent(), DEFAULT_ENCODING);
				results.put(url, src);
				isSuccess = true;
			}
			catch (URISyntaxException e) {
				logger.debug(e.getMessage(), e);
				e.printStackTrace();
			}
			catch (ClientProtocolException e) {
				logger.debug(e.getMessage(), e);
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Can retry
				logger.debug(e.getMessage(), e);
				e.printStackTrace();
			}
			finally {
				logger.debug(String.format("URL: %1$s %2$s", url, isSuccess ? "Success." : "Failed."));
			}
		}
		return results;
	}
	
	public String cleanHtml(String html) throws IOException {
	 
		TagNode tagNode = cleaner.clean(html);
		 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		xmlSerializer.writeToStream(tagNode, out, DEFAULT_ENCODING);
		return out.toString(DEFAULT_ENCODING);
	}
	
	public Map<String, List<FilmInfo>> getDataMap(Map<String, String> src) throws IOException {
		Map<String, List<FilmInfo>> ret = new HashMap<String, List<FilmInfo>>();
		for (Entry<String, String> entry : src.entrySet()) {
			String content = entry.getValue();
			// TODO Do not use spliter, use <img>
			String[] tks = content.split(DEFAULT_SPLIT_SPLITER);
			List<FilmInfo> films = new ArrayList<FilmInfo>();
			for (String filmSource : tks) {
				FilmInfo info = new FilmInfo();
				info.setUrl(entry.getKey());
				info.setSource(filmSource);
				films.add(info);
			}
			ret.put(entry.getKey(), films);
		}
		return ret;
	}
	
	private void splitWithImage(String source) {
		
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
					"INSERT INTO FILM(FM_DATE, FM_DESC) VALUES(?, ?);");
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
					prepfilm.setDate(1, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
					prepfilm.setString(2, StringUtils.join(f.getDesc(), "\n"));
					prepfilm.executeUpdate();
					ResultSet rs = prepheaderid.executeQuery();
					long headerid = rs.getLong("ID");
					
					preplink.clearBatch();
					preplink.clearParameters();
					for (String link : f.getLink()) {
						preplink.setLong(1, headerid);
						preplink.setString(2, link);
						preplink.addBatch();
					}
					preplink.executeBatch();
					
					prepimage.clearBatch();
					prepimage.clearParameters();
					for (String img : f.getPics()) {
						prepimage.setLong(1, headerid);
						prepimage.setString(2, img);
						prepimage.addBatch();
					}
					prepimage.executeBatch();
					
					JDBCHelper.closeResultSet(rs);
				}
			}
			
			conn.commit();
			
			// TODO Use DbUtils
			JDBCHelper.closeStatements(new Statement[] {prepfilm, preplink, prepimage, prepheaderid});
			JDBCHelper.closeConnection(conn);
		}
		catch (SQLException e) {
			logger.debug(e.getMessage(), e);
		}
	}
}
