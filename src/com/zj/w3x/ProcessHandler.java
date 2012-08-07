/*
 * (c) Copyright 2007 Bokesoft Co,Ltd. All Rights Reserved.
 * $Id: himalaya-codetemplates.xml 13967 2009-04-09 01:30:41Z xuel $
 */
package com.zj.w3x;

import java.util.List;

public interface ProcessHandler {
	public List<String> split(String source);
	public FilmBean resolve(String splited);
	public void save(List<FilmBean> beans);
	public String getEncoding();
}
