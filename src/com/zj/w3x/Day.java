package com.zj.w3x;

import java.util.List;


public class Day {
	private String name;
	private List<Movie> movies;
	private String daylink;
	
	public Day(String name, List<Movie> movies, String daylink) {
		this.name = name;
		this.movies = movies;
		this.daylink = daylink;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Movie> getMovies() {
		return movies;
	}

	public void setMovies(List<Movie> movies) {
		this.movies = movies;
	}

	public String getDaylink() {
		return daylink;
	}

	public void setDaylink(String daylink) {
		this.daylink = daylink;
	}
}
