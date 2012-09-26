package com.zj.w3x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessHandlerDefaultImpl implements ProcessHandler {
	
	private Logger logger = LoggerFactory.getLogger(ProcessHandlerDefaultImpl.class);

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
