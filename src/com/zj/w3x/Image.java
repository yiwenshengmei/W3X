package com.zj.w3x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class Image {
	private String neturl = StringUtils.EMPTY;
	private String localpath = StringUtils.EMPTY;
	private byte[] binary;
	private boolean download = false;
	
	public Image(String neturl) {
		this.neturl = neturl;
	}
	
	public Image(String neturl, String localpath) {
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
		return FilenameUtils.getName(FilenameUtils.getName(neturl
				.replaceAll("&", ".")
				.replaceAll("\\?", ".")
				.replaceAll("=", ".")));
	}
	
	public String getNameExtension() {
		return FilenameUtils.getExtension(neturl);
	}
	
	public void save(File folder) throws IOException {
		if (!isDownload()) return;
		FileOutputStream fos = new FileOutputStream(new File(folder, getFileName()));
		fos.write(binary, 0, binary.length);
		IOUtils.closeQuietly(fos);
	}
}
