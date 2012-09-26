package com.zj.w3x;

import java.util.List;

public class AbstractHandler implements ProcessHandler {

	@Override
	public void beforeDownloadHtml() {
	}

	@Override
	public void afterDownloadHtml(String html) {
	}

	@Override
	public void beforeCleanHtml(String html) {
	}

	@Override
	public void afterCleanHtml(String xml) {
	}

	@Override
	public List<Movie> onParse(String xml) {
		return null;
	}

	@Override
	public void beforeSave(List<Movie> films) throws Exception {
	}

	@Override
	public void onSave(List<Movie> films) {
	}

	@Override
	public String getHtmlEncoding() {
		return null;
	}

	@Override
	public String getTempfileEncoding() {
		return null;
	}

}
