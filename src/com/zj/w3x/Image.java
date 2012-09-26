package com.zj.w3x;

import org.apache.commons.io.FilenameUtils;

public class Image extends Link {

	public Image(String neturl) {
		super(neturl);
	}

	public Image(String neturl, String localpath) {
		super(neturl, localpath);
	}

	@Override
	public String getFileName() {
		return FilenameUtils.getName(FilenameUtils.getName(neturl
				.replaceAll("&", ".")
				.replaceAll("\\?", ".")
				.replaceAll("=", ".")));
	}
	
	
}
