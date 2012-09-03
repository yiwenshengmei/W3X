/*
 * (c) Copyright 2007 Bokesoft Co,Ltd. All Rights Reserved.
 * $Id: himalaya-codetemplates.xml 13967 2009-04-09 01:30:41Z xuel $
 */
package com.zj.w3x;

import java.util.List;
import java.util.Map;

public interface ProcessHandler {
	public List<String> split(String source);
	public Film resolve(String splited);
	public void save(List<Film> beans);
	public String getEncoding();
	public void beforeSave(List<Film> beans) throws Exception;
	public void beforeDownload(String url, Map<String, String> param);
}
