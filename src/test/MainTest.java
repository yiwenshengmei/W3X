package test;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.zj.w3x.GetTorrentTask;
import com.zj.w3x.Link;
import com.zj.w3x.W3X;

public class MainTest {

	public static Logger logger = LoggerFactory.getLogger(MainTest.class);
	
	@Test
	public void testGetSource() throws Exception {
		W3X w3x = new W3X();
		
		String source = w3x.getCleanSource(W3X.URL_TODAY);
		Assert.assertEquals("", source.length() > 1);
	}
	
	@Test
	public void testParseMovie() throws Exception {
	}
	
	@Test
	public void testGetTorrentFileCode() throws IOException, ParserConfigurationException, SAXException {
		String downloadUrl = "http://www.jandown.com/link.php?ref=W16stghCEK";
		String expectCode = "W16stghCEK";
		String expectFetchUrl = "http://www.jandown.com/fetch.php";
		
		String[] codeurl = GetTorrentTask.getCodeUrl(downloadUrl, true);
		
		logger.info("fetchUrl: " + codeurl[1]);
		
		Assert.assertEquals(expectCode, codeurl[0]);
		Assert.assertEquals(expectFetchUrl, codeurl[1]);
	}
	
	@Test
	public void testGetTorrent() throws Exception {
		
		Link link = new Link("http://www.jandown.com/link.php?ref=W16stghCEK");
		
		GetTorrentTask task = new GetTorrentTask(link);
		task = task.call();
		
		Assert.assertTrue(task.getTorrent().isDownload());
		
		logger.info("TorrentFileName: " + task.getTorrent().getFileName());
		Assert.assertTrue("文件名长度不正确", task.getTorrent().getFileName().length() > 1);
		logger.info("FileLength: " + task.getTorrent().getBinary().length + " Bytes");
		Assert.assertTrue("文件大小不正确", task.getTorrent().getBinary().length > 1);
	}
}
