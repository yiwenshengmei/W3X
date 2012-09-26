/*
 * (c) Copyright 2007 Bokesoft Co,Ltd. All Rights Reserved.
 * $Id: himalaya-codetemplates.xml 13967 2009-04-09 01:30:41Z xuel $
 */
package com.zj.w3x;

import java.util.List;

public class MetaData {
	private String url;
	private String source;
	private List<String> splited;
	private List<Movie> beans;
	private ProcessHandler handler;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public List<String> getSplited() {
		return splited;
	}
	public void setSplited(List<String> splited) {
		this.splited = splited;
	}
	public List<Movie> getBeans() {
		return beans;
	}
	public void setBeans(List<Movie> beans) {
		this.beans = beans;
	}
	public ProcessHandler getHandler() {
		return handler;
	}
	public void setHandler(ProcessHandler handler) {
		this.handler = handler;
	}
	
	
}
