package com.zj.w3x.main;
import java.util.List;
import java.util.Map;

public class FilmInfo {
	private List<String> desc;
	private List<String> link;
	private List<String> pics;
	private Map<String, byte[]> picsBinary;
	private String source;
	private String url;
	
	public FilmInfo(List<String> desc, List<String> link, List<String> pics, String source, String url) {
		this.desc = desc;
		this.link = link;
		this.pics = pics;
		this.source = source;
		this.url = url;
	}
	
	public FilmInfo() {
		
	}
	
	public List<String> getDesc() {
		return desc;
	}
	public void setDesc(List<String> desc) {
		this.desc = desc;
	}
	public List<String> getLink() {
		return link;
	}
	public void setLink(List<String> link) {
		this.link = link;
	}
	public List<String> getPics() {
		return pics;
	}
	public void setPics(List<String> pics) {
		this.pics = pics;
	}
	public Map<String, byte[]> getPicsBinary() {
		return picsBinary;
	}
	public void setPicsBinary(Map<String, byte[]> picsBinary) {
		this.picsBinary = picsBinary;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
