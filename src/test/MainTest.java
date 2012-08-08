package test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zj.w3x.ProcessHandlerDefaultImpl;
import com.zj.w3x.W3X;
import com.zj.w3x.W3XHelper;

public class MainTest {
	
	public static Logger logger = LoggerFactory.getLogger(MainTest.class);
	
	@Test
	public void testAll() throws IOException, ClassNotFoundException, SQLException {
		W3X x = new W3X();
		x.addUrls(Arrays.asList(new String[] { W3X.DEFAULT_URL_TODAY, W3X.DEFAULT_URL_YESTERDAY, W3X.DEFAULT_URL_BEFORE_YESTERDAY }));
		x.setProcessHandler(new ProcessHandlerDefaultImpl());
		x.start();
	}
	
	@Test
	public void testDownloadImage() throws ClientProtocolException, URISyntaxException, IOException {
		String url = "http://www.apache.org/images/asf_logo_wide.gif";
		byte[] bytes = W3XHelper.downloadSingle(url, 5000, 5000);
		FileUtils.writeByteArrayToFile(new File("D:\\" + FilenameUtils.getName(url)), bytes);
	}
	
	@Test
	public void quickTest() {
		String filename = FilenameUtils.getName("http://www.apache.org/images/asf_logo_wide.gif");
		logger.debug(filename);
	}
	
	@Test
	public void testCreateTable() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:D:\\w3x.db");
		Statement stmt = conn.createStatement();
		
		StringBuilder sqlCreateTblDownloadLink = new StringBuilder();
		sqlCreateTblDownloadLink.append("CREATE TABLE [DOWNLOAD_LINK] ( ");
		sqlCreateTblDownloadLink.append("[ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sqlCreateTblDownloadLink.append("[DL_HEADER_ID] CHAR NOT NULL, ");
		sqlCreateTblDownloadLink.append("[DL_LINK] TEXT); ");
		
		StringBuilder sqlCreateTblFilm = new StringBuilder();
		sqlCreateTblFilm.append("CREATE TABLE [FILM] ( ");
		sqlCreateTblFilm.append("[ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sqlCreateTblFilm.append("[FM_DATE] TIMESTAMP NOT NULL DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), ");
		sqlCreateTblFilm.append("[FM_DESC] TEXT); ");
		
		StringBuilder sqlCreateTblImage = new StringBuilder();
		sqlCreateTblImage.append("CREATE TABLE [IMAGE] ( ");
		sqlCreateTblImage.append("[ID] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
		sqlCreateTblImage.append("[IMG_HEADER_ID] INTEGER NOT NULL, ");
		sqlCreateTblImage.append("[IMG_PATH] TEXT);");
		
		for (String sql : new String[] {sqlCreateTblDownloadLink.toString(), sqlCreateTblFilm.toString(), sqlCreateTblImage.toString()}) {
			stmt.executeUpdate(sql);
		}
		
		stmt.close();
		conn.close();
	}
}
