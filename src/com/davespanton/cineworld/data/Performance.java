package com.davespanton.cineworld.data;

public class Performance {

	private String time; // start time in hh:mm
	private boolean available; // if the performance is available for booking
	private String type; // performance type code: reg - Regular; vip - VIP; del - Deluxe; digital - Digital; m4j - Movies for Juniors
	private boolean ad; // whether the performance is audio-described
	private boolean subtitled; // whether the performance is subtitled
	private String bookingUrl; // url to book for this performance
	
	/**
	 * Set the start time of the performance. 
	 * 
	 * @param time	Should be in 'hh:mm' format.
	 */
	public void setTime(String time) {
		this.time = time;
	}
	/**
	 * The start time of the performance in 'hh:mm' format
	 * 
	 * @return Start time in 'hh:mm' format.
	 */
	public String getTime() {
		return time;
	}
	
	/**
	 * Set whether this performance is available to book.
	 * 
	 * @param available
	 */
	public void setAvailable(boolean available) {
		this.available = available;
	}
	/**
	 * Whether this performance is available to book.
	 * 
	 * @return
	 */
	public boolean isAvailable() {
		return available;
	}
	
	/**
	 *  The performance type code.
	 *  
	 *  Should be: reg - Regular; vip - VIP; del - Deluxe; digital - Digital; m4j - Movies for Juniors.
	 *
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * The performance type code.
	 *  
	 * Should be: reg - Regular; vip - VIP; del - Deluxe; digital - Digital; m4j - Movies for Juniors.
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Set whether this performance is audio-described
	 * 
	 */
	public void setAd(boolean ad) {
		this.ad = ad;
	}
	/**
	 * Whether this performance is audio-described
	 * @return
	 */
	public boolean isAd() {
		return ad;
	}
	
	/**
	 * Set whether this performance is subtitled
	 *  
	 * @param subtitled
	 */
	public void setSubtitled(boolean subtitled) {
		this.subtitled = subtitled;
	}
	/**
	 * Whether this performance is subtitled
	 * 
	 * @return
	 */
	public boolean isSubtitled() {
		return subtitled;
	}
	
	/**
	 * Set the url to go to to book tickets for this performance.
	 * 
	 * @param bookingUrl
	 */
	public void setBookingUrl(String bookingUrl) {
		this.bookingUrl = bookingUrl;
	}
	/**
	 * The url to go to to book tickets for this performance.
	 * 
	 * @return
	 */
	public String getBookingUrl() {
		return bookingUrl;
	}
}
