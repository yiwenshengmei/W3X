package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.zj.w3x.Film;
import com.zj.w3x.Image;
import com.zj.w3x.JDBCHelper;
import com.zj.w3x.W3X;
import com.zj.w3x.W3XHelper;

public class MainTest {

	public static Logger logger = LoggerFactory.getLogger(MainTest.class);
	private final static String TEMP_FILE_PATH = "E:\\xingfulianmeng_temp.xml";
	private final static String REPORT_FILE_PATH = "E:\\xingfulianmeng_report.txt";
	private final static String IMG_SAVE_PATH = "E:\\Flim\\w3x";
	private final static String DB_PATH = "D:\\w3x.db";
	private static boolean CREATE_TEMP_FILE = false;
	private static boolean CREATE_REPORT_FILE = false;
	private final static String SPILTER = "------------------------------------------------";
	private final static String ENCODING_DWONLOAD = "gb2312";
	private final static String ENCODING_TEMP_FILE = "utf-8";
	private final static String ENCODING_REPORT_FILE = "utf-8";
	
	@Test
	public void testAll() throws Exception {
		List<Film> films = parseXmlByDOM(downloadSource(W3X.URL_TODAY));
		for (Film f : films) {
			f.setUrl(W3X.URL_TODAY);
		}
		saveFilms(films);
		downloadImages(films);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date today = Calendar.getInstance().getTime();
		
		File folder = new File(IMG_SAVE_PATH, sdf.format(today));
		if (!folder.exists()) folder.mkdirs();
		
		for (Film f : films) {
			for (Image img : f.getImages()) {
				img.save(folder);
			}
		}
		logger.debug("Images are saved to " + folder);
	}
	
	@Test
	public void quickTest() {
		String url = "http://xxx.freeimage.us/image.php?id=A95C_50435BD3&jpg";
		logger.debug(FilenameUtils.getName(url.replaceAll("&", ".").replaceAll("\\?", ".").replaceAll("=", ".")));
	}
	
	private void downloadImages(List<Film> films) {
		W3XHelper.multiDownload(films, 10 * 1000, 10 * 1000);
	}
	
	private void createTable(Connection conn) throws SQLException {
		StringBuilder crtDownloadLinkSQL = new StringBuilder();
		crtDownloadLinkSQL.append("CREATE TABLE [DOWNLOAD_LINK] ( ");
		crtDownloadLinkSQL.append("[_ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		crtDownloadLinkSQL.append("[DL_HEADER_ID] CHAR NOT NULL, ");
		crtDownloadLinkSQL.append("[DL_LINK] TEXT); ");
		
		StringBuilder crtFilmSQL = new StringBuilder();
		crtFilmSQL.append("CREATE TABLE [FILM] ( ");
		crtFilmSQL.append("[_ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		crtFilmSQL.append("[FM_DATE] TIMESTAMP NOT NULL DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), ");
		crtFilmSQL.append("[FM_DESC] TEXT); ");
		
		StringBuilder crtImageSQL = new StringBuilder();
		crtImageSQL.append("CREATE TABLE [IMAGE] ( ");
		crtImageSQL.append("[_ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		crtImageSQL.append("[IMG_HEADER_ID] INTEGER NOT NULL, ");
		crtImageSQL.append("[IMG_PATH] TEXT, ");
		crtImageSQL.append("[IMG_LOCAL_PATH] TEXT);");

		Statement stmt = conn.createStatement();
		for (String sql : new String[] {crtDownloadLinkSQL.toString(), crtImageSQL.toString(), crtFilmSQL.toString()}) {
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
	
	private void saveFilms(List<Film> films) {
		logger.debug("Start save, totoal: " + films.size());
		
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);

			checkOrInitTable(conn);
			
//			conn.setAutoCommit(false);
			PreparedStatement prepfilm = conn.prepareStatement(
					"INSERT INTO FILM(FM_DESC) VALUES(?);");
			PreparedStatement preplink = conn.prepareStatement(
					"INSERT INTO DOWNLOAD_LINK(DL_HEADER_ID, DL_LINK) VALUES(?, ?);");
			PreparedStatement prepimage = conn.prepareStatement(
					"INSERT INTO IMAGE(IMG_HEADER_ID, IMG_PATH, IMG_LOCAL_PATH) VALUES(?, ?, ?);");
			PreparedStatement prepheaderid = conn.prepareStatement(
					"SELECT last_insert_rowid() AS '_ID' FROM FILM;");
			
			for (Film f : films) {
				
				prepfilm.clearBatch();
				prepfilm.clearParameters();
//					prepfilm.setDate(1, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				prepfilm.setString(1, StringUtils.join(f.getDesc(), "\n"));
//				logger.debug("Save 1 record into FILM...");
				prepfilm.executeUpdate();
				ResultSet rs = prepheaderid.executeQuery();
				long headerid = rs.getLong("_ID");
				
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
			
//			logger.debug("Commit...");
//			conn.commit();
//			logger.debug("Commit successful.");
			
			// TODO zhaojie Use DbUtils
			JDBCHelper.closeStatements(new Statement[] {prepfilm, preplink, prepimage, prepheaderid});
			JDBCHelper.closeConnection(conn);
			logger.debug("Save done.");
		}
		catch (SQLException e) {
			logger.debug(e.getMessage(), e);
		}
		catch (ClassNotFoundException e) {
			logger.debug(e.getMessage(), e);
		}
	}
	
	@Test
	public void testDownloadSource() throws Exception {
		CREATE_TEMP_FILE = true;
		downloadSource(W3X.URL_TODAY);
	}
	
	@Test
	public void testParseXml() throws Exception {
		CREATE_REPORT_FILE = true;
		parseXmlByDOM(IOUtils.toString(new FileInputStream(TEMP_FILE_PATH), ENCODING_TEMP_FILE));
	}

	private String downloadSource(String url) throws Exception {
		logger.debug("Start download " + url);
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
		
		if (CREATE_TEMP_FILE) {
			boolean append = false;
			FileOutputStream outStream = new FileOutputStream(TEMP_FILE_PATH, append);
			xmlSerializer.writeToStream(tagNode, outStream, ENCODING_TEMP_FILE);
			IOUtils.closeQuietly(outStream);
		}

		String ret = xmlSerializer.getAsString(tagNode, ENCODING_DWONLOAD);
		logger.debug("Download Done.");
		return ret;
	}

	private List<Film> parseXmlByDOM(String xml) throws Exception {
		logger.debug("Start parse.");
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));
		
		NodeList divs = doc.getElementsByTagName("div");
		
		List<Film> films = new ArrayList<Film>();
		for (int i = 0; i < divs.getLength(); i++) {
			Element div = (Element) divs.item(i);
			if (div.hasAttribute("align") && div.getAttribute("align").equals("left")) {
				Element content = (Element) div.getElementsByTagName("font").item(0);
				
				NodeList childs = content.getChildNodes();
				List<String> desc = new ArrayList<String>();
				List<Image> imgs = new ArrayList<Image>();
				List<String> links = new ArrayList<String>();
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
							links.add(elem.getAttribute("href"));
						}
						else if (elem.getTagName().equals("font") && elem.getTextContent().contains(SPILTER)) {
							Film film = new Film();
							film.setDesc(desc);
							film.setDownloadLinks(links);
							film.setImages(imgs);
							films.add(film);
							
							desc = new ArrayList<String>();
							imgs = new ArrayList<Image>();
							links = new ArrayList<String>();
						}
					}
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for (Film fb : films) {
			sb.append(fb.toString()).append("\n");
		}
		if (CREATE_REPORT_FILE) {
			FileOutputStream fos = new FileOutputStream(REPORT_FILE_PATH, false);
			fos.write(sb.toString().getBytes(ENCODING_REPORT_FILE));
			IOUtils.closeQuietly(fos);
		}
		else {
//			logger.debug(sb.toString());
		}
		
		logger.debug("Parse Done.");
		return films;
	}
}
