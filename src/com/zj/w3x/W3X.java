package com.zj.w3x;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	
	public final static String URL_TODAY            = "http://74.55.154.143/index1.html";
	public final static String URL_YESTERDAY        = "http://74.55.154.143/index2.htm";
	public final static String URL_BEFORE_YESTERDAY = "http://74.55.154.143/index3.htm";
	public final static String[] DEFAULT_URLS = new String[] {
		URL_TODAY, URL_YESTERDAY, URL_BEFORE_YESTERDAY
	};
	public final static String DEFAULT_ENCODING = "gbk";
	
	private CleanerProperties props;
	private PrettyXmlSerializer xmlSerializer;
	private HtmlCleaner cleaner;
	
	List<ProcessData> datas = new ArrayList<ProcessData>();
	
	public W3X() {

	}
	
	private CleanerProperties getCleanerProperties() {
		if (props == null) {
			props = new CleanerProperties();
			// 把js和css转换成CDATA
			props.setUseCdataForScriptAndStyle(true);
			// 过滤注释
			props.setOmitComments(true);
		}
		return this.props;
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
		int nSuccess = 0;
		for (ProcessData aDay : datas) {
			try {
				
				logger.debug("Start >>>>>>>>>>>> " + aDay.getUrl());
				
				download(aDay);
				cleanHtml(aDay);
			
				List<String> splited = aDay.getHandler().split(aDay.getSource());
				aDay.setSplited(splited);
				
				if (splited == null) {
					logger.debug(String.format("Splited result is nothing in %1$s, continue.", aDay.getUrl()));
					continue;
				}
				for (String sp : splited) {
					Film bean = aDay.getHandler().resolve(sp);
					bean.setUrl(aDay.getUrl());
					
					
					if (aDay.getBeans() != null) {
						aDay.getBeans().add(bean);
					}
					else {
						List<Film> beans = new ArrayList<Film>();
						beans.add(bean);
						aDay.setBeans(beans);
					}
				}
				logger.debug(String.format("%1$s film_beans created.", aDay.getBeans().size()));
				
				aDay.getHandler().beforeSave(aDay.getBeans());
				aDay.getHandler().save(aDay.getBeans());
				
				logger.debug("End >>>>>>>>>>>> " + aDay.getUrl());
				nSuccess++;
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		logger.debug(String.format("Total Done. Success: %1$s, Failed: %2$s", nSuccess, datas.size() - nSuccess));
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
	
	public void download(ProcessData data) throws URISyntaxException, ClientProtocolException, IOException {
		HttpClient http = getHttpClient();
		HttpGet request = new HttpGet();
		request.setURI(new URI(data.getUrl()));
		
		Map<String, String> userParam = new HashMap<String, String>();
		data.getHandler().beforeDownload(data.getUrl(), userParam);
		
		if (userParam.size() > 0) {
			HttpParams param = new BasicHttpParams();
			for (Entry<String, String> entry : userParam.entrySet()) {
				param.setParameter(entry.getKey(), entry.getValue());
			}
			request.setParams(param);
		}
		
		HttpResponse resp = http.execute(request);
		HttpEntity entity = resp.getEntity();

		logger.debug("Begin download " + data.getUrl());
		String source = IOUtils.toString(entity.getContent(), data.getHandler().getEncoding());
		logger.debug("End download " + data.getUrl());
		
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
