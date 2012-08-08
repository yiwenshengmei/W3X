package com.zj.w3x;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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


public class W3X {
	
	Logger logger = LoggerFactory.getLogger(W3X.class);
	
	public final static String DEFAULT_URL_TODAY     = "http://74.55.154.143/index1.html";
	public final static String DEFAULT_URL_YESTERDAY = "http://74.55.154.143/index2.html";
	public final static String DEFAULT_URL_BEFORE_YESTERDAY = "http://74.55.154.143/index3.html";
	public final static String[] DEFAULT_URL_PACK = new String[] {
		DEFAULT_URL_TODAY, DEFAULT_URL_YESTERDAY, DEFAULT_URL_BEFORE_YESTERDAY
	};
	public final static String DEFAULT_ENCODING = "gbk";
	
	private CleanerProperties cleanerProperties;
	private PrettyXmlSerializer xmlSerializer;
	private HtmlCleaner cleaner;
	
	List<ProcessData> datas = new ArrayList<ProcessData>();
	
	public W3X() {

	}
	
	private CleanerProperties getCleanerProperties() {
		if (cleanerProperties == null) {
			cleanerProperties = new CleanerProperties();
			cleanerProperties.setTranslateSpecialEntities(true);
			cleanerProperties.setTransResCharsToNCR(true);
			cleanerProperties.setOmitComments(true);
		}
		return this.cleanerProperties;
	}
	
	private PrettyXmlSerializer getXmlSerializer() {
		if (xmlSerializer == null) {
			xmlSerializer = new PrettyXmlSerializer(getCleanerProperties());
		}
		return this.xmlSerializer;
	}
	
	private HtmlCleaner getHtmlCleaner() {
		if (cleaner == null) {
			cleaner = new HtmlCleaner(getCleanerProperties());
		}
		return cleaner;
	}
	
	public void start() {
		int successCnt = 0;
		for (ProcessData data : datas) {
			try {
				download(data);
				cleanHtml(data);
			
				List<String> splited = data.getHandler().split(data.getSource());
				data.setSplited(splited);
				
				if (splited == null) {
					logger.debug(String.format("Splited result is nothing in %1$s, continue.", data.getUrl()));
					continue;
				}
				for (String sp : splited) {
					FilmBean bean = data.getHandler().resolve(sp);
					bean.setUrl(data.getUrl());
					
					logger.debug(bean.toString());
					
					if (data.getBeans() != null) {
						data.getBeans().add(bean);
					}
					else {
						List<FilmBean> beans = new ArrayList<FilmBean>();
						beans.add(bean);
						data.setBeans(beans);
					}
				}
				
				data.getHandler().save(data.getBeans());
				successCnt++;
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		logger.debug(String.format("Done. Success: %1$s, Failed: %2$s", successCnt, datas.size() - successCnt));
	}
	
	public void addUrl(final String url) {
		boolean urlExists = CollectionUtils.exists(datas, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				ProcessData d = (ProcessData) arg0;
				return d.getUrl().equals(url);
			}
		});
		if (!urlExists) {
			ProcessData d = new ProcessData();
			d.setUrl(url);
			datas.add(d);
		}
	}
	
	public void addUrls(List<String> urls) {
		for (String url : urls) {
			addUrl(url);
		}
	}
	
	public void setProcessHandler(final String url, ProcessHandler handler) {
		ProcessData d = (ProcessData) CollectionUtils.find(datas, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				ProcessData d = (ProcessData) arg0;
				return d.getUrl().equals(url);
			}
		});
		if (d != null)
			d.setHandler(handler);
	}
	
	public void setProcessHandler(final ProcessHandler handler) {
		CollectionUtils.forAllDo(datas, new Closure() {
			@Override
			public void execute(Object arg0) {
				ProcessData data = (ProcessData) arg0;
				data.setHandler(handler);
			}
		});
	}
	
	private HttpClient httpClient;
	private HttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}
		return httpClient;
	}
	
	private HttpGet httpGet;
	private HttpGet getHttpGet() {
		if (httpGet == null) {
			httpGet = new HttpGet();
		}
		return this.httpGet;
	}
	
	public void download(ProcessData data) throws URISyntaxException, ClientProtocolException, IOException {
		HttpClient http = getHttpClient();
		HttpGet get = getHttpGet();
		get.setURI(new URI(data.getUrl()));
		
		logger.debug("Using encoding: " + data.getHandler().getEncoding());
		HttpResponse resp = http.execute(get);
		HttpEntity entity = resp.getEntity();

		logger.debug("Begin download " + data.getUrl());
		String source = IOUtils.toString(entity.getContent(), data.getHandler().getEncoding());
		logger.debug(data.getUrl() + " download successful.");
		
		data.setSource(source);
	}
	
	public void cleanHtml(ProcessData data) throws IOException {
		TagNode tagNode = getHtmlCleaner().clean(data.getSource());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		getXmlSerializer().writeToStream(tagNode, out, data.getHandler().getEncoding());
		String cleaned = out.toString(data.getHandler().getEncoding());
		
		data.setSource(cleaned);
	}
}
