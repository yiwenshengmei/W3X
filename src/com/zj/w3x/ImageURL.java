/*
 * (c) Copyright 2007 Bokesoft Co,Ltd. All Rights Reserved.
 * $Id: himalaya-codetemplates.xml 13967 2009-04-09 01:30:41Z xuel $
 */
package com.zj.w3x;

public class ImageURL {
	public ImageURL(String netUrl, String localUrl) {
		this.netUrl = netUrl;
		this.localUrl = localUrl;
	}
	private String netUrl;
	private String localUrl;
	public String getNetUrl() {
		return netUrl;
	}
	public void setNetUrl(String netUrl) {
		this.netUrl = netUrl;
	}
	public String getLocalUrl() {
		return localUrl;
	}
	public void setLocalUrl(String localUrl) {
		this.localUrl = localUrl;
	}
}
