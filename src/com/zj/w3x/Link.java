package com.zj.w3x;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class Link {
	protected String neturl = StringUtils.EMPTY;
	protected String localpath = StringUtils.EMPTY;
	protected byte[] binary;
	protected boolean download = false;
	protected String fileName = StringUtils.EMPTY;
	
	public Link(String neturl) {
		this.neturl = neturl;
	}
	
	public Link(String neturl, String localpath) {
		this.neturl = neturl;
		this.localpath = localpath;
	}
	
	public String getNeturl() {
		return neturl;
	}
	public void setNeturl(String neturl) {
		this.neturl = neturl;
	}
	public String getLocalpath() {
		return localpath;
	}
	public void setLocalpath(String localpath) {
		this.localpath = localpath;
	}
	public byte[] getBinary() {
		return binary;
	}
	public void setBinary(byte[] binary) {
		this.binary = binary;
	}
	public boolean isDownload() {
		return download;
	}
	public void setDownload(boolean download) {
		this.download = download;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public String getNameExtension() {
		return FilenameUtils.getExtension(neturl);
	}
	
	public void save(File folder) throws IOException {
		if (!download) return;
		FileUtils.writeByteArrayToFile(new File(folder, getFileName()), binary);
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return neturl + (download ? " -> " + localpath : StringUtils.EMPTY);
	}
}
