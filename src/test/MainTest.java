//package test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.StringReader;
//import java.net.URL;
//import java.net.URLConnection;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import junit.framework.Assert;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.StringUtils;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.StringBody;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.htmlcleaner.CleanerProperties;
//import org.htmlcleaner.HtmlCleaner;
//import org.htmlcleaner.PrettyXmlSerializer;
//import org.htmlcleaner.TagNode;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
//import com.zj.w3x.Movie;
//import com.zj.w3x.Image;
//import com.zj.w3x.JDBCHelper;
//import com.zj.w3x.Link;
//import com.zj.w3x.W3X;
//import com.zj.w3x.W3XHelper;
//
//public class MainTest {
//
//	public static Logger logger = LoggerFactory.getLogger(MainTest.class);
//	
//	@Test
//	public void testAll() throws Exception {
//		W3X.main(new String[] {});
//	}
//	
//	@Test
//	public void testGetSource() throws Exception {
//		W3X w3x = new W3X();
//		
//		String source = w3x.getCleanSource(W3X.URL_TODAY);
//		Assert.assertEquals("", source.length() > 1);
//	}
//	
//	@Test
//	public void testParseMovie() throws Exception {
//		parseMovie(IOUtils.toString(new FileInputStream(TEMP_FILE_PATH), ENCODING_TEMP_FILE), "");
//	}
//	
//	@Test
//	public void testName() throws Exception {
//		String downloadUrl = "http://www.jandown.com/link.php?ref=W16stghCEK";
//		URL url = new URL(downloadUrl);
//		logger.debug("protocol: " + url.getProtocol());
//		logger.debug("path: " + url.getPath());
//		logger.debug("host: " + url.getHost());
//	}
//	
//	@Test
//	public void testGetTorrentFileCode() throws IOException, ParserConfigurationException, SAXException {
//		String downloadUrl = "http://www.jandown.com/link.php?ref=W16stghCEK";
//		String expectCode = "W16stghCEK";
//		String expectFetchUrl = "http://www.jandown.com/fetch.php";
//		
//		String[] codeurl = getCodeUrl(downloadUrl);
//		
//		logger.debug("fetchUrl: " + codeurl[1]);
//		
//		Assert.assertEquals(expectCode, codeurl[0]);
//		Assert.assertEquals(expectFetchUrl, codeurl[1]);
//	}
//	
//	@Test
//	public void testGetTorrent() throws Exception {
//		
//		Link link = new Link("http://www.jandown.com/link.php?ref=W16stghCEK");
//		
//		String[] codeurl = getCodeUrl(link.getNeturl());
//		String code = codeurl[0], url = codeurl[1];
//		logger.debug("Code: " + code);
//		logger.debug("PostUrl:" + url);
//		
//		boolean isSuccessful = doGetTorrent(link);
//		Assert.assertTrue(isSuccessful);
//		
//		logger.debug("TorrentFileName: " + link.getFileName());
//		Assert.assertTrue("文件名长度不正确", link.getFileName().length() > 1);
//		logger.debug("FileLength: " + link.getBinary().length + " Bytes");
//		Assert.assertTrue("文件大小不正确", link.getBinary().length > 1);
//	}
//}
