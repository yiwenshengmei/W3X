package com.zj.w3x;

import java.util.List;

public interface ProcessHandler {
	public void beforeDownloadHtml();
	public void afterDownloadHtml(String html);
	public void beforeCleanHtml(String html);
	public void afterCleanHtml(String xml);
	public List<Movie> onParse(String xml);
	public void beforeSave(List<Movie> films) throws Exception;
	public void onSave(List<Movie> films);
	public String getHtmlEncoding();
	public String getTempfileEncoding();
}
