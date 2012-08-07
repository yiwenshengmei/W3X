package com.zj.w3x;
import java.util.ArrayList;
import java.util.List;

public class FilmBean {
	private List<String> desc;
	private List<String> link;
	private List<String> imageSrcs;
	private String source;
	private String url;
	
	public FilmBean(List<String> desc, List<String> link, List<String> imageSrcs, String source, String url) {
		this.desc = desc;
		this.link = link;
		this.imageSrcs = imageSrcs;
		this.source = source;
		this.url = url;
	}
	
	public FilmBean() {
		desc = new ArrayList<String>();
		link = new ArrayList<String>();
		imageSrcs = new ArrayList<String>();
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
	public List<String> getImageSrcs() {
		return imageSrcs;
	}
	public void setImageSrcs(List<String> imageSrcs) {
		this.imageSrcs = imageSrcs;
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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String spliter = "\n------------------------------------";
		sb.append(spliter).append("\n");
		sb.append("from url: ").append(url).append("\n");
		for (String d : desc) {
			sb.append("desc: ").append(d).append("\n");
		}
		for (String src : imageSrcs) {
			sb.append("image: ").append(src).append("\n");
		}
		for (String l : link) {
			sb.append("link: ").append(l).append("\n");
		}
		sb.append("------------------------------------");
		return sb.toString();
	}
}
