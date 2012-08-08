/*
 * (c) Copyright 2007 Bokesoft Co,Ltd. All Rights Reserved.
 * $Id: himalaya-codetemplates.xml 13967 2009-04-09 01:30:41Z xuel $
 */
package com.zj.w3x;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

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

public class W3XHelper {
	
	private static HttpClient httpClient;
	
	private static HttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}
		return httpClient;
	}
	
	public static byte[] downloadSingle(String url) throws FileNotFoundException, IOException {
		return IOUtils.toByteArray(new URL(url).openStream());
	}
	
	public static byte[] downloadSingle(String url, int sockTimeout, int connTimeout) throws URISyntaxException, ClientProtocolException, IOException {
		HttpClient http = getHttpClient();
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
	
	public static Map<String, byte[]> downloadMulti(List<String> urls, int sockTimeout, int connTimeout) {
		return null;
	}
}
