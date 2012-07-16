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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
	
	public static void main(String[] args) throws IOException {
		
		W3X x3 = new W3X();
		
		Map<String, String> src = x3.download(new String[] {
				DEFAULT_URL_TODAY, DEFAULT_URL_YESTERDAY, DEFAULT_URL_BEFORE_YESTERDAY
		});
		for (Entry<String, String> entry : src.entrySet()) {
			entry.setValue(x3.cleanHtml(entry.getValue()));
		}
		
		Map<String, List<FilmInfo>> datamap = x3.getDataMap(src);
		x3.saveDataMap(datamap);
	}
	
	public W3X() {
		props = new CleanerProperties();
		 
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
	}
		
	public Map<String, String> download(String[] urls) {
		HttpClient http = new DefaultHttpClient();
		HttpGet get = new HttpGet();
		Map<String, String> results = new HashMap<String, String>();
		for (String url : urls) {
			try {
				get.setURI(new URI(url));
				HttpResponse resp = http.execute(get);
				HttpEntity entity = resp.getEntity();
				String src = IOUtils.toString(entity.getContent(), DEFAULT_ENCODING);
				results.put(url, src);
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
			catch (ClientProtocolException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return results;
	}
	
	public String cleanHtml(String html) throws IOException {
	 
		TagNode tagNode = new HtmlCleaner(props).clean(html);
		 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PrettyXmlSerializer(props).writeToStream(tagNode, out, DEFAULT_ENCODING);
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
	
	public void saveDataMap(Map<String, List<FilmInfo>> datamap) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\x3.db");
			Statement stat = conn.createStatement();
			PreparedStatement prep = conn.prepareStatement(
					"INSERT INTO T_FILM VALUES(?, ?);"); // TODO Be sure the data_struct of x3.db
			for (Entry<String, List<FilmInfo>> aDay : datamap.entrySet()) {
				for (FilmInfo film : aDay.getValue()) {
					// TODO Fill sql
					prep.setString(1, "");
					prep.setString(2, "");
					prep.addBatch();
				}
			}
			
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
			
			// TODO Use DbUtils
			prep.close();
			stat.close();
			conn.close();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
