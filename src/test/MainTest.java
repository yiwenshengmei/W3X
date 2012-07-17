package test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zj.w3x.main.FilmInfo;
import com.zj.w3x.main.W3X;

public class MainTest {
	
	public static Logger logger = LoggerFactory.getLogger(MainTest.class);
	
	@Test
	public void testRegex() {
		String source0 = "nihao   <img src=\"xxxxxxx\"></img>  12344444";
		String source1 = "<img src='xxxxxxx'/>";
		Pattern pattern = Pattern.compile("\b<img src=\"(.+?)\".*?>\b");
		String[] strs = pattern.split(source0);
		for (int i=0;i<strs.length;i++) {
		    System.out.println(strs[i]);
		} 
	}
	
	@Test
	public void testSqlDate() {
		java.util.Date d = new Date();
		System.out.println(d);
		System.out.println(d.getTime());
		System.out.println(new java.sql.Date(d.getTime()));
		System.out.println(Calendar.getInstance().getTime().getTime());
		System.out.println(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
	}
	
	@Test
	public void testStringJoin() {
		List<String> source = Arrays.asList(new String[] {"a", "b", "c"});
		
	}
	
	@Test
	public void testSaveDataMap() throws ClassNotFoundException, SQLException {
		Map<String, List<FilmInfo>> datamap = new HashMap<String, List<FilmInfo>>();
		datamap.put("http://74.55.154.139/index1.html", Arrays.asList(new FilmInfo[] {
				new FilmInfo(list(new String[] {"123"}), list(new String[] {"http://xxx/download.jsp"}), list(new String[] {"C:\\pic.jpg", "C:\\pic2.jpg"}), "source", "http://74.55.154.139/index1.html"),
				new FilmInfo(list(new String[] {"456"}), list(new String[] {"http://xxx/download.jsp"}), list(new String[] {"C:\\pic.jpg", "C:\\pic2.jpg"}), "source", "http://74.55.154.139/index2.html"),
				new FilmInfo(list(new String[] {"789"}), list(new String[] {"http://xxx/download.jsp"}), list(new String[] {"C:\\pic.jpg"}), "source", "http://74.55.154.139/index3.html"),
				new FilmInfo(list(new String[] {"desc"}), list(new String[] {"http://xxx/download.jsp"}), list(new String[] {"C:\\pic.jpg"}), "source", "http://74.55.154.139/index4.html"),
				new FilmInfo(list(new String[] {"desc"}), list(new String[] {"http://xxx/download.jsp"}), list(new String[] {"C:\\pic.jpg"}), "source", "http://74.55.154.139/index5.html"),
				new FilmInfo(list(new String[] {"desc"}), list(new String[] {"http://xxx/download.jsp"}), list(new String[] {"C:\\pic.jpg"}), "source", "http://74.55.154.139/index6.html"),
				new FilmInfo(list(new String[] {"desc"}), list(new String[] {"http://xxx/download.jsp"}), list(new String[] {"C:\\pic.jpg"}), "source", "http://74.55.154.139/index7.html"),
				new FilmInfo(list(new String[] {"desc"}), list(new String[] {"http://xxx/download.jsp"}), list(new String[] {"C:\\pic.jpg"}), "source", "http://74.55.154.139/index8.html")
		}));
		
		W3X w3x = new W3X();
		w3x.saveDataMap(datamap);
	}
	
	private List<String> list(String[] arrays) {
		return Arrays.asList(arrays);
	}
	
	@Test
	public void testAll() throws IOException, ClassNotFoundException, SQLException {
		W3X x3 = new W3X();
		
		Map<String, String> src = x3.download(new String[] {
				W3X.DEFAULT_URL_TODAY, W3X.DEFAULT_URL_YESTERDAY, W3X.DEFAULT_URL_BEFORE_YESTERDAY
		});
		
		// Clean the html of every day.
		for (Entry<String, String> entry : src.entrySet()) {
			entry.setValue(x3.cleanHtml(entry.getValue()));
		}
		
		// Split and make into beans.
		Map<String, List<FilmInfo>> datamap = x3.getDataMap(src);
		
		// Save to sqlite database.
		x3.saveDataMap(datamap);
	}
}
