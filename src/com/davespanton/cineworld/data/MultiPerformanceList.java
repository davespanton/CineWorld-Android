package com.davespanton.cineworld.data;

import java.util.HashMap;

public class MultiPerformanceList {
	
	private String id;
	private int size;
	
	private HashMap<String, PerformanceList> performanceLists = new HashMap<String, PerformanceList>();
	
	public MultiPerformanceList(String id, int size) {
		super();
		this.id = id;
		this.size = size;
	}

	public String getId() {
		return id;
	}
	
	public PerformanceList get(Object key) {
		return performanceLists.get(key);
	}

	public int size() {
		return performanceLists.size();
	}

	public boolean isComplete() {
		return performanceLists.size() == size;
	}
	
	public void putPerformaceList( String performanceId, PerformanceList performanceList ) {
		if( size == performanceLists.size() ) {
			throw new Error("MultiPerformanceList is already full");
		}
		
		performanceLists.put(performanceId, performanceList);
	}
	
	
}
