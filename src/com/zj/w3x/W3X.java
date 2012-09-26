package com.zj.w3x;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class W3X {
	
	private static Logger logger = LoggerFactory.getLogger(W3X.class);
	
	public static String URL_TODAY                  = "http://74.55.154.143/index1.html";
	public static String URL_YESTERDAY              = "http://74.55.154.143/index2.htm";
	public static String URL_BEFORE_YESTERDAY       = "http://74.55.154.143/index3.htm";
	public final static String SAVE_PATH_IMG        = "C:\\w3x\\images";
	public final static String SAVE_PATH_TORRENT    = "C:\\w3x\\torrents";
	public final static String DB_PATH              = "C:\\w3x.db";
	public static boolean      CREATE_REPORT_FILE   = false;
	public final static String SPILTER              = "------------------------------------------------";
	public final static String ENCODING_DWONLOAD    = "gb2312";
	public final static String ENCODING_TEMP_FILE   = "utf-8";
	public final static String ENCODING_REPORT_FILE = "utf-8";
	public final static int TIMEOUT_TORRENT_READ    = 10 * 1000;
	public final static int TIMEOUT_TORRENT_CONN    = 10 * 1000;
	public final static int TIMEOUT_IMAGE_READ      = 10 * 1000;
	public final static int TIMEOUT_IMAGE_CONN      = 10 * 1000;
	
	public static void main(String[] args) throws Exception {
		// w3x.jar -noimg -notorrent -nodatabase -test -today http://xxxx -yesterday http://xxx -beforeyesterday http://xxx
		boolean noimage = false;
		boolean notorrent = false;
		boolean nodatabase = false;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].toLowerCase().equals("-today")) URL_TODAY = args[i + 1];
			if (args[i].toLowerCase().equals("-yesterday")) URL_YESTERDAY = args[i + 1];
			if (args[i].toLowerCase().equals("-beforeyesterday")) URL_BEFORE_YESTERDAY = args[i + 1];
			if (args[i].toLowerCase().equals("-noimage")) noimage = true;
			if (args[i].toLowerCase().equals("-notorrent")) notorrent = true;
			if (args[i].toLowerCase().equals("-nodatabase")) nodatabase = true;
		}
		try {
			List<Day> days = new W3X().start(noimage, notorrent, nodatabase);
			report(days);
		}
		catch (Exception ex) {
			throw ex; // TODO zhaojie 未处理的异常
		}
		finally {
			W3XHelper.shutdownThreadPool();
		}
		
	}
	
	private static void report(List<Day> days) {
		int completeImages   = 0;
		int failedImages     = 0;
		int completeTorrents = 0;
		int failedTorrents   = 0;
		
		for (Day day : days) {
			for (Movie mv : day.getMovies()) {
				for (Image img : mv.getImages())
					if (img.isDownload()) completeImages++; else failedImages++;
				for (Link torrent : mv.getDownloadLinks()) 
					if (torrent.isDownload()) completeTorrents++; else failedTorrents++;
			}
		}
		
		logger.info(String.format("Download %1$s images, failed %2$s.", completeImages, failedImages));
		logger.info(String.format("Download %1$s torrents, failed %2$s.", completeTorrents, failedTorrents));
	}
	
	public List<Day> start(boolean noimage, boolean notorrent, boolean nodatabase) throws Exception {
		List<Day> days = new ArrayList<Day>();
		
		Map<String, String> urls = new HashMap<String, String>(); 
		urls.put("Today", URL_TODAY);
		urls.put("Yesterday", URL_YESTERDAY);
		urls.put("BeforeYesterday", URL_BEFORE_YESTERDAY);
		
		for (Entry<String, String> entry : urls.entrySet()) {
			String source = StringUtils.EMPTY;
			try {
				source = getCleanSource(entry.getValue());
			}
			catch (Exception e) {
				logger.debug(String.format("获取网页[%1$s]源代码时发生错误", entry.getValue()), e);
				logger.info(String.format("获取网页[%1$s]源代码时发生错误", entry.getValue()));
				continue;
			}
			
			List<Movie> movies = null;
			try {
				movies = parseMovie(source, entry.getValue());
			}
			catch (Exception e) {
				logger.debug(String.format("解析网页[%1$s]时发生错误", entry.getValue()), e);
				logger.info(String.format("解析网页[%1$s]时发生错误", entry.getValue()));
				continue;
			}
			
			for (Movie m : movies) 
				logger.debug("\n" + m.toJSONString() + "\n----------------------\n\n");
			
			if (!noimage) { 
				getImages(movies);
				saveImages(movies, SAVE_PATH_IMG);
				logger.info("Images get ok.");
			}
			
			if (!notorrent) {
				getTorrent(movies);
				saveTorrents(movies, SAVE_PATH_TORRENT);
				logger.info("Torrents get ok.");
			}
			
			if (!nodatabase) {
				saveMovies(movies);
				logger.info("All Write to Database OK.");
			}
			
			days.add(new Day(entry.getKey(), movies, entry.getValue()));
		}
		
		return days;
	}
	
	public static void getTorrent(List<Movie> movies) {
		W3XHelper.getTorrents(movies, TIMEOUT_TORRENT_READ, TIMEOUT_TORRENT_CONN);
	}
	
	private void saveTorrents(List<Movie> movies, String savepath) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date today = Calendar.getInstance().getTime();
		
		File folder = new File(savepath, sdf.format(today) + "-torrent");
		if (!folder.exists()) folder.mkdirs();
		
		for (Movie mv : movies) {
			for (Link torrent : mv.getDownloadLinks()) {
				torrent.save(folder);
			}
		}
		logger.info("Torrents saved to " + folder);
	}
	
	private void saveImages(List<Movie> movies, String savepath) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date today = Calendar.getInstance().getTime();
		
		File folder = new File(savepath, sdf.format(today) + "-image");
		if (!folder.exists()) folder.mkdirs();
		
		for (Movie mv : movies) {
			for (Image img : mv.getImages()) {
				img.save(folder);
			}
		}
		logger.info("Images saved to " + folder);
	}
	
	// 被调用3次
	private void getImages(List<Movie> movies) {
		W3XHelper.getImages(movies, 10 * 1000, 10 * 1000);
	}
	
	private void createTable(Connection conn) throws SQLException {
		logger.info("Creating Data Table...");
		StringBuilder crtDownloadLinkSQL = new StringBuilder();
		crtDownloadLinkSQL.append("CREATE TABLE [DOWNLOAD_LINK] ( ");
		crtDownloadLinkSQL.append("[_ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		crtDownloadLinkSQL.append("[DL_HEADER_ID] INTEGER NOT NULL, ");
		crtDownloadLinkSQL.append("[DL_LINK] TEXT, ");
		crtDownloadLinkSQL.append("[DL_LOCAL_PATH] TEXT); ");
		
		StringBuilder crtMovieSQL = new StringBuilder();
		crtMovieSQL.append("CREATE TABLE [MOVIE] ( ");
		crtMovieSQL.append("[_ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		crtMovieSQL.append("[FM_DATE] TIMESTAMP NOT NULL DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), ");
		crtMovieSQL.append("[FM_DESC] TEXT); ");
		
		StringBuilder crtImageSQL = new StringBuilder();
		crtImageSQL.append("CREATE TABLE [IMAGE] ( ");
		crtImageSQL.append("[_ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		crtImageSQL.append("[IMG_HEADER_ID] INTEGER NOT NULL, ");
		crtImageSQL.append("[IMG_NET_PATH] TEXT, ");
		crtImageSQL.append("[IMG_LOCAL_PATH] TEXT);");

		Statement stmt = conn.createStatement();
		for (String sql : new String[] {crtDownloadLinkSQL.toString(), crtImageSQL.toString(), crtMovieSQL.toString()}) {
			stmt.executeUpdate(sql);
		}
		stmt.close();
		logger.info("Data Table Created Successful.");
	}
	
	private void checkOrInitTable(Connection conn) throws SQLException {
		String sqlCheckTable = "SELECT COUNT(*) AS TBL_CNT FROM SQLITE_MASTER WHERE NAME IN ('MOVIE', 'DOWNLOAD_LINK', 'IMAGE');";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sqlCheckTable);
		if (rs.next()) {
			int tableCnt = rs.getInt("TBL_CNT");
			if (tableCnt != 3) {
				logger.info("No Table Schema Found!");
				createTable(conn);
			}
		}
	}
	
	// 被调用3次
	private void saveMovies(List<Movie> movies) {
		logger.info("Start save, totoal: " + movies.size());
		
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);

			checkOrInitTable(conn);
			
//			conn.setAutoCommit(false);
			PreparedStatement prepMovie = conn.prepareStatement(
					"INSERT INTO MOVIE(FM_DESC) VALUES(?);");
			PreparedStatement preplink = conn.prepareStatement(
					"INSERT INTO DOWNLOAD_LINK(DL_HEADER_ID, DL_LINK, DL_LOCAL_PATH) VALUES(?, ?, ?);");
			PreparedStatement prepimage = conn.prepareStatement(
					"INSERT INTO IMAGE(IMG_HEADER_ID, IMG_PATH, IMG_LOCAL_PATH) VALUES(?, ?, ?);");
			PreparedStatement prepheaderid = conn.prepareStatement(
					"SELECT last_insert_rowid() AS '_ID' FROM MOVIE;");
			
			for (Movie m : movies) {
				
				prepMovie.clearBatch();
				prepMovie.clearParameters();
//					prepMovie.setDate(1, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				prepMovie.setString(1, StringUtils.join(m.getDesc(), "\n"));
//				logger.debug("Save 1 record into MOVIE...");
				prepMovie.executeUpdate();
				ResultSet rs = prepheaderid.executeQuery();
				
				/** HeaderId **/
				long headerid = rs.getLong("_ID");
				
				preplink.clearBatch();
				preplink.clearParameters();
				for (Link link : m.getDownloadLinks()) {
					preplink.setLong(1, headerid);
					preplink.setString(2, link.getNeturl());
					preplink.setString(3, link.getLocalpath());
//					logger.debug("Save 1 record into DOWNLOAD_LINK...");
					preplink.addBatch();
				}
				preplink.executeBatch();
				
				prepimage.clearBatch();
				prepimage.clearParameters();
				for (Image imgUrl : m.getImages()) {
					prepimage.setLong(1, headerid);
					prepimage.setString(2, imgUrl.getNeturl());
					prepimage.setString(3, imgUrl.getLocalpath());
//					logger.debug("Save 1 record into IMAGE...");
					prepimage.addBatch();
				}
				prepimage.executeBatch();
				
				JDBCHelper.closeResultSet(rs);
			}
			
//			logger.debug("Commit...");
//			conn.commit();
//			logger.debug("Commit successful.");
			
			// TODO zhaojie Use DbUtils
			JDBCHelper.closeStatements(new Statement[] {prepMovie, preplink, prepimage, prepheaderid});
			JDBCHelper.closeConnection(conn);
			logger.info("Save done.");
		}
		catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	// 被调用3次
	public String getCleanSource(String url) throws Exception {
		logger.info("Start download " + url);
		URL today = new URL(url);
		URLConnection conn = today.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setReadTimeout(5 * 1000);

		InputStream inStream = conn.getInputStream();

		CleanerProperties props = new CleanerProperties();
		// 把js和css转换成CDATA
		props.setUseCdataForScriptAndStyle(true);
		// 过滤注释
		props.setOmitComments(true);

		HtmlCleaner cleaner = new HtmlCleaner(props);
		TagNode tagNode = cleaner.clean(inStream, ENCODING_DWONLOAD);
		

		PrettyXmlSerializer xmlSerializer = new PrettyXmlSerializer(props);
		
		String ret = xmlSerializer.getAsString(tagNode, ENCODING_DWONLOAD);
		
//		logger.debug("\n" + ret);
		logger.info("Download Done.");
		
		return ret;
	}

	// 被调用3次
	private List<Movie> parseMovie(String xml, String from) throws Exception {
		logger.debug("Start parse.");
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));
		
		NodeList divs = doc.getElementsByTagName("div");
		
		List<Movie> movies = new ArrayList<Movie>();
		for (int i = 0; i < divs.getLength(); i++) {
			Element div = (Element) divs.item(i);
			if (div.hasAttribute("align") && div.getAttribute("align").equals("left")) {
				Element content = (Element) div.getElementsByTagName("font").item(0);
				
				NodeList childs = content.getChildNodes();
				List<String> desc = new ArrayList<String>();
				List<Image> imgs = new ArrayList<Image>();
				List<Link> links = new ArrayList<Link>();
				for (int j = 0; j < childs.getLength(); j++) {
					
					Node e = childs.item(j);
					
					if (e.getNodeType() == Node.TEXT_NODE) { 
						String txt = e.getTextContent().trim() // 去掉两头的空格
								.replaceAll("\n", StringUtils.EMPTY) // 去掉回车
								.replaceAll(" ", StringUtils.EMPTY) // 去掉中间的空格
								.replaceAll("\t", StringUtils.EMPTY); // 去掉TAB
						if (StringUtils.isNotBlank(txt))
							desc.add(txt);
					}
					else if (e.getNodeType() == Node.ELEMENT_NODE) {
						Element elem = (Element) e;
						if (elem.getTagName().equals("img")) {
							imgs.add(new Image(elem.getAttribute("src")));
						}
						else if (elem.getTagName().equals("a")) {
							links.add(new Link(elem.getAttribute("href")));
						}
						else if (elem.getTagName().equals("font") && elem.getTextContent().contains(SPILTER)) {
							Movie movie = new Movie();
							movie.setDesc(desc);
							movie.setDownloadLinks(links);
							movie.setImages(imgs);
							movie.setUrl(from);
							movies.add(movie);
							
							desc = new ArrayList<String>();
							imgs = new ArrayList<Image>();
							links = new ArrayList<Link>();
						}
					}
				}
			}
		}
		
//		StringBuilder sb = new StringBuilder();
//		sb.append("\n");
//		for (Movie m : movies) {
//			sb.append(m.toString()).append("\n");
//		}
//		logger.debug(sb.toString());
		
		logger.info("Parse Done.");
		return movies;
	}

}
