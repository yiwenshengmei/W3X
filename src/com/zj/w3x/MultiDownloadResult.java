/*
 * (c) Copyright 2007 Bokesoft Co,Ltd. All Rights Reserved.
 * $Id: himalaya-codetemplates.xml 13967 2009-04-09 01:30:41Z xuel $
 */
package com.zj.w3x;

public class MultiDownloadResult {
	private String url;
	private byte[] binary;
	private Throwable exception;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public byte[] getBinary() {
		return binary;
	}
	public void setBinary(byte[] binary) {
		this.binary = binary;
	}
	public Throwable getException() {
		return exception;
	}
	public void setException(Throwable exception) {
		this.exception = exception;
	}
	public MultiDownloadResult(String url, byte[] binary, Throwable exception) {
		this.url = url;
		this.binary = binary;
		this.exception = exception;
	}
	public static MultiDownloadResult newInstanceforSuccess(String url, byte[] binary) {
		return new MultiDownloadResult(url, binary, null);
	}
	public static MultiDownloadResult newInstanceforFail(String url, Throwable ex) {
		return new MultiDownloadResult(url, null, ex);
	}
}
