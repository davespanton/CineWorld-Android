package com.davespanton.util;

public class DateUtils {
	
	/**
	 * Returns the ordinal value for the passed day of the month, e.g. 'th', 'nd', 'st'. 
	 * 
	 * @param 	dayOfMonth	The day of the month to get the ordinal value for.
	 * @return	String		The ordinal value for the passed day of the month.
	 */
	public static final String getOrdinal( int dayOfMonth ) {
		
		if (dayOfMonth >= 11 && dayOfMonth <= 13) {
	        return "th";
	    }
	    switch (dayOfMonth % 10) {
	        case 1: return "st";
	        case 2: return "nd";
	        case 3: return "rd";
	        default: return "th";
	    }
	}
	
}
