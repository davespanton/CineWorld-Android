package com.davespanton.cineworld.data;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public class MultiPerformanceList implements Parcelable {
	
	public static final Parcelable.Creator<MultiPerformanceList> CREATOR = new Parcelable.Creator<MultiPerformanceList>() {

		@Override
		public MultiPerformanceList createFromParcel(Parcel in) {
			return new MultiPerformanceList(in);
		}

		@Override
		public MultiPerformanceList[] newArray(int size) {
			return new MultiPerformanceList[size];
		}
	}; 
	
	private String id;
	private int size;
	
	private HashMap<String, PerformanceList> performanceLists = new HashMap<String, PerformanceList>();
	
	public MultiPerformanceList(String id, int size) {
		super();
		this.id = id;
		this.size = size;
	}
	
	public MultiPerformanceList(Parcel in) {
		readFromParcel(in);
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(performanceLists.size());
		for( String s: performanceLists.keySet() ) {
			dest.writeString(s);
			dest.writeParcelable(performanceLists.get(s), flags);
		}
		
		dest.writeString(id);
		dest.writeInt(size);
	}
	
	public void readFromParcel(Parcel in) {
		int mapSize = in.readInt();
		
		for( int i = 0; i < mapSize; i++ )
			performanceLists.put(in.readString(), (PerformanceList) in.readParcelable(PerformanceList.class.getClassLoader()));
		
		id = in.readString();
		size = in.readInt();
	}
	
	
}
