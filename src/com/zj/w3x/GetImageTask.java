package com.zj.w3x;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetImageTask implements Callable<GetImageTask> {
	
	private static Logger logger = LoggerFactory.getLogger(GetImageTask.class);
	
	private Image image;
	private int readTimeout = 10 * 1000;
	private int connTimeout = 10 * 1000;

	public GetImageTask(Image image, int readTimeout, int connTimeout) {
		this.image = image;
		this.readTimeout = readTimeout;
		this.connTimeout = connTimeout;
	}
	
	public GetImageTask(Image image) {
		this.image = image;
	}

	@Override
	public GetImageTask call() throws Exception {
		try {
			URL url = new URL(image.getNeturl());
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(connTimeout);
			conn.setReadTimeout(readTimeout);
			byte[] binary = IOUtils.toByteArray(conn.getInputStream());
			
			image.setBinary(binary);
			image.setDownload(true);
			logger.info(image.getNeturl() + " -> ¡Ì");
		}
		catch (Exception ex) {
			logger.info(image.getNeturl() + " -> x");
			logger.debug(ex.getMessage());
		}
		return this;
	}

	public Image getImage() {
		return image;
	}
}