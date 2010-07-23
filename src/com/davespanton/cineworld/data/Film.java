package com.davespanton.cineworld.data;

public class Film {
	
	private String title;
	private String rating;
	private String advisory;
	private String posterUrl;
	private String stillUrl;
	private String filmUrl;
	private String edi;
	
	public Film( )
	{
		
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getRating() {
		return rating;
	}

	public void setAdvisory(String advisory) {
		this.advisory = advisory;
	}

	public String getAdvisory() {
		return advisory;
	}

	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}

	public String getPosterUrl() {
		return posterUrl;
	}

	public void setStillUrl(String stillUrl) {
		this.stillUrl = stillUrl;
	}

	public String getStillUrl() {
		return stillUrl;
	}

	public void setFilmUrl(String filmUrl) {
		this.filmUrl = filmUrl;
	}

	public String getFilmUrl() {
		return filmUrl;
	}

	public void setEdi(String edi) {
		this.edi = edi;
	}

	public String getEdi() {
		return edi;
	}

	public boolean validate(){
		
		if( title != null && rating != null && posterUrl != null
				&& stillUrl != null && filmUrl != null && edi != null ) {
			return true;
		}
		else
			return false;
		
	}
	
}
