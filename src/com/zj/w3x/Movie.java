package com.zj.w3x;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Movie {
	
	private static Logger logger = LoggerFactory.getLogger(Movie.class);
	
	private List<String> desc;
	private List<Link> downloadLinks;
	private String filmSource;
	private String url;
	private List<Image> images;
	
	public Movie() {
		desc = new ArrayList<String>();
		downloadLinks = new ArrayList<Link>();
		images = new ArrayList<Image>();
	}
	
	public List<String> getDesc() {
		return desc;
	}
	public void setDesc(List<String> desc) {
		this.desc = desc;
	}
	public List<Link> getDownloadLinks() {
		return downloadLinks;
	}
	public void setDownloadLinks(List<Link> links) {
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
//		sb.append(spliter).append("\n");
		for (String d : desc) {
			sb.append(d).append("\n");
		}
		for (Link l : downloadLinks) {
			sb.append("link: ").append(l).append("\n");
		}
		sb.append(spliter);
		return sb.toString();
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> imageURLs) {
		this.images = imageURLs;
	}
	
	public String toJSONString() {
		try {
			JSONObject j = new JSONObject();

			j.put("Url", this.url);
			j.put("desc", StringUtils.join(desc, "\n"));
			
			JSONArray links = new JSONArray();
			for (Link l : this.downloadLinks) links.put(l.toString());
			j.put("Links", links);
			
			JSONArray imgs = new JSONArray();
			for (Image img : this.images) imgs.put(img.getNeturl());
			j.put("Images", imgs);
			
			return j.toString(4);
		}
		catch (JSONException e) {
			logger.error("转换对象为JSON字符串时发生错误", e);
			return "An Error Occured.";
		}
	}

}
