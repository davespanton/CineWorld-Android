package com.davespanton.cineworld.data;

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;

//TODO	think about structure for retrieving information: need sorting by date,  
//		adapters need numeric access.
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
	
	private ArrayList<PerformanceList> performanceLists = new ArrayList<PerformanceList>();;
	
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
	
	public int size() {
		return performanceLists.size();
	}

	public boolean isComplete() {
		return performanceLists.size() == size;
	}
	
	public void addPerformaceList( PerformanceList performanceList ) {
		if( size == performanceLists.size() ) {
			throw new Error("MultiPerformanceList is already full");
		}
		
		performanceLists.add(performanceList);
	}
	
	public PerformanceList get(int index) {
		return performanceLists.get(index);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(performanceLists.size());
		for( int i = 0; i < performanceLists.size(); i++ ) {
			dest.writeParcelable(performanceLists.get(i), flags);
		}
		
		dest.writeString(id);
		dest.writeInt(size);
	}
	
	public void readFromParcel(Parcel in) {
		int s = in.readInt();
		
		//TODO	fix bug here. null object reference at 86.
		for( int i = 0; i < s; i++ )
			performanceLists.add( (PerformanceList) in.readParcelable(PerformanceList.class.getClassLoader()));
		
		id = in.readString();
		size = in.readInt();
	}
}
