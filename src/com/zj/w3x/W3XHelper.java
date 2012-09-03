package com.zj.w3x;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class W3XHelper {
	
	private static HttpClient httpClient;
	private static Logger logger = LoggerFactory.getLogger(W3XHelper.class);
	
	private static HttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}
		return httpClient;
	}
	
	public static byte[] downloadSingle(String url) throws FileNotFoundException, IOException {
		return IOUtils.toByteArray(new URL(url).openStream());
	}
	
	public static byte[] downloadSingle(String url, int sockTimeout, int connTimeout, boolean isMultiThread) throws URISyntaxException, ClientProtocolException, IOException {
		HttpClient http = isMultiThread ? new DefaultHttpClient() : getHttpClient();
		HttpGet request = new HttpGet();
		request.setURI(new URI(url));
		
		HttpParams param = new BasicHttpParams();
		param.setParameter(CoreConnectionPNames.SO_TIMEOUT, sockTimeout);
		param.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connTimeout);
		
		request.setParams(param);
		HttpResponse resp = http.execute(request);
		HttpEntity entity = resp.getEntity();
		
		return IOUtils.toByteArray(entity.getContent());
	}
	
	public static byte[] downloadSingle(String url, int connTimeout, int readTimeout) throws IOException {
		URL _url = new URL(url);
		URLConnection conn = _url.openConnection();
		conn.setConnectTimeout(connTimeout);
		conn.setReadTimeout(readTimeout);
		
		ByteArrayOutputStream ret = new ByteArrayOutputStream();
		InputStream inStream = conn.getInputStream();
		IOUtils.copy(inStream, ret);
		IOUtils.closeQuietly(inStream);
		
		return ret.toByteArray();
	}
	
	private static class DownloadTask implements Callable<DownloadTask> {
		
		private Image image;
		private int sockTimout;
		private int connTimeout;

		public DownloadTask(Image image, int sockTimeout, int connTimeout) {
			this.image = image;
			this.sockTimout = sockTimeout;
			this.connTimeout = connTimeout;
		}

		@Override
		public DownloadTask call() throws Exception {
			this.image.setBinary(downloadSingle(image.getNeturl(), sockTimout, connTimeout, true));
			return this;
		}

		public Image getImage() {
			return image;
		}
	}
	
	public static List<Image> summaryImages(List<Film> beans) {
		List<Image> totalImgs = new ArrayList<Image>();
		for (Film bean : beans) {
			totalImgs.addAll(bean.getImages());
		}
		return totalImgs;
	}
	
	public static void multiDownload(List<Film> beans, int sockTimeout, int connTimeout) {
		List<Image> totalImgs = summaryImages(beans);
		logger.debug("Start download images, total: " + totalImgs.size());
		ExecutorService thPool = getExecutor();
		List<Future<DownloadTask>> futures = new ArrayList<Future<DownloadTask>>();
		
		// Add task to ThreadPool.
		for (Film bean : beans) {
			for (Image img : bean.getImages())
				futures.add(thPool.submit(new DownloadTask(img, sockTimeout, connTimeout)));
		}
		
		// Get result from task.
		int nSuccess = 0;
		for (Future<DownloadTask> f : futures) {
			try {
				DownloadTask tsk = f.get();
				tsk.getImage().setDownload(true);				
				W3XHelper.logger.debug("¡Ì");
				nSuccess++;
			}
			catch (Exception ex) {
				W3XHelper.logger.debug("X " + ex.getMessage());
//				W3XHelper.logger.error(ex.getMessage(), ex);
			}
		}
		
		logger.debug(String.format("Done. %1$s success, %2$s failed.", nSuccess, totalImgs.size() - nSuccess));
		
		@SuppressWarnings("unchecked")
		Collection<Image> fails = CollectionUtils.select(totalImgs, new Predicate() {
			
			@Override
			public boolean evaluate(Object arg0) {
				return !((Image) arg0).isDownload();
			}
		});
		
		if (fails.size() > 0) {
			logger.debug("Fails are: ");
			String prefix = " * ";
			for (Image img : fails) {
				logger.debug(prefix + img.getNeturl());
			}
		}
	}
	
	private static ExecutorService getExecutor() {
		return Executors.newFixedThreadPool(80);
	}
}
