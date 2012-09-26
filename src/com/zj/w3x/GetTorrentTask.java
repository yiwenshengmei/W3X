package com.zj.w3x;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GetTorrentTask implements Callable<GetTorrentTask> {
	
	private static Logger logger = LoggerFactory.getLogger(GetTorrentTask.class);
	
	private Link torrent;
	private int readTimeout = 10 * 1000;
	private int connTimeout = 10 * 1000;
	
	public static List<String> SUPPORT_HOST_LIST;
	
	static {
		SUPPORT_HOST_LIST = new ArrayList<String>();
		SUPPORT_HOST_LIST.add("www.jandown.com");
	}
	
	public GetTorrentTask(Link torrent, int readTimeout, int connTimeout) {
		this.torrent = torrent;
		this.readTimeout = readTimeout;
		this.connTimeout = connTimeout;
	}

	public GetTorrentTask(Link torrent) {
		this.torrent = torrent;
	}
	
	public static String[] getCodeUrl(String url) throws IOException, ParserConfigurationException, SAXException {
		return getCodeUrl(url, false);
	}

	@Override
	public GetTorrentTask call() throws Exception {
		try {
			boolean support = SUPPORT_HOST_LIST.contains(new URL(torrent.toString()).getHost());
			logger.info("Download: " + torrent.toString() + " " + (support ? StringUtils.EMPTY : "[NotSupport]"));
			if (!support) return this;
			String[] codeurl = getCodeUrl(torrent.getNeturl());
			String code = codeurl[0];
			String url = codeurl[1];
			
			logger.debug(String.format("Link: %1$s, PostCode: %2$s, PostUrl: %3$s", torrent.toString(), code, url));
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			
			HttpParams param = new BasicHttpParams();
			param.setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);
			param.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connTimeout);
			
			post.setParams(param);
			post.addHeader("Referer", torrent.getNeturl());
			post.addHeader("Origin", "http://www.jandown.com");
			
			StringBody codeBody = new StringBody(code);
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("code", codeBody);
			post.setEntity(reqEntity);
			
			HttpResponse response = client.execute(post);
			
			String torrentName = response.getHeaders("Content-Disposition")[0].getElements()[0].getParameterByName("filename").getValue();
//			logger.debug("TorrentName: " + torrentName);
			InputStream torrentStream = response.getEntity().getContent();
			
			ByteArrayOutputStream torrentBinary = new ByteArrayOutputStream();
			IOUtils.copy(torrentStream, torrentBinary);
			IOUtils.closeQuietly(torrentStream);
 			
			torrent.setBinary(torrentBinary.toByteArray());
			torrent.setFileName(torrentName);
			torrent.setDownload(true);
			
			logger.info(torrentName + " --> ¡Ì");
		}
		catch (Exception ex) {
			logger.info("x");
			logger.debug(torrent.toString() + " -> fail: " + ex.getMessage());
		}
		return this;
	}
	
	public Link getTorrent() {
		return this.torrent;
	}

	public static String[] getCodeUrl(String url, boolean debug) throws IOException, ParserConfigurationException, SAXException {
		URL targetUrl = new URL(url);
		URLConnection conn = targetUrl.openConnection();
		InputStream target_is = conn.getInputStream();
		
		CleanerProperties props = new CleanerProperties();
		props.setUseCdataForScriptAndStyle(true);
		props.setOmitComments(true);
	
		HtmlCleaner cleaner = new HtmlCleaner(props);
		TagNode tagNode = cleaner.clean(target_is, "utf-8");
	
		PrettyXmlSerializer xmlSerializer = new PrettyXmlSerializer(props);
		
		String xml = xmlSerializer.getAsString(tagNode, "utf-8");
		if (debug) logger.debug(xml);
		target_is.close();
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));
		
		Element eForm = (Element) doc.getElementsByTagName("form").item(0);
		Element eInput = (Element) eForm.getElementsByTagName("input").item(0);
		
		String posturl = String.format("%1$s://%2$s/%3$s", targetUrl.getProtocol(), targetUrl.getHost(), eForm.getAttribute("action"));
		String postcode = eInput.getAttribute("value");
		
		return new String[] { postcode, posturl };
	}

}
