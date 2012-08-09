package com.zj.w3x;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilmBean {
	private List<String> desc;
	private List<String> downloadLinks;
	private String filmSource;
	private String url;
	private List<ImageURL> imageURLs;
	
	public FilmBean() {
		desc = new ArrayList<String>();
		downloadLinks = new ArrayList<String>();
		imageURLs = new ArrayList<ImageURL>();
	}
	
	public List<String> getDesc() {
		return desc;
	}
	public void setDesc(List<String> desc) {
		this.desc = desc;
	}
	public List<String> getDownloadLinks() {
		return downloadLinks;
	}
	public void setDownloadLinks(List<String> links) {
		this.downloadLinks = links;
	}
	public String getFilmSource() {
		return filmSource;
	}
	public void setFilmSource(String source) {
		this.filmSource = source;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String spliter = "\n------------------------------------";
		sb.append(spliter).append("\n");
		sb.append("from url: ").append(url).append("\n");
		for (String d : desc) {
			sb.append("desc: ").append(d).append("\n");
		}
		for (String l : downloadLinks) {
			sb.append("link: ").append(l).append("\n");
		}
		sb.append("------------------------------------");
		return sb.toString();
	}

	public List<ImageURL> getImageURLs() {
		return imageURLs;
	}

	public void setImageURLs(List<ImageURL> imageURLs) {
		this.imageURLs = imageURLs;
	}

}
