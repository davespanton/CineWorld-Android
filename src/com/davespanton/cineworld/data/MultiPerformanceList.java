package com.davespanton.cineworld.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import android.os.Parcel;
import android.os.Parcelable;

public class MultiPerformanceList extends ArrayList<PerformanceList>  implements Parcelable {
	
	private static final long serialVersionUID = 5983218982531042262L;

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
	private int maxSize;
	private int total = 0;
	
	public MultiPerformanceList(String id, int maxSize) {
		super();
		this.id = id;
		this.maxSize = maxSize;
	}
	
	public MultiPerformanceList(Parcel in) {
		readFromParcel(in);
	}

	public String getId() {
		return id;
	}
	
	public boolean isComplete() {
		return total == maxSize;
	}
	
	public int getTotal() {
		return total;
	}
	
	@Override
	public boolean add( PerformanceList performanceList ) {
		if( total == maxSize ) {
			throw new Error("MultiPerformanceList is already full");
		}
		
		if(performanceList != null)
			super.add(performanceList);
		
		total++;
		Collections.sort(this, new PerformanceListDateComparitor());
		return true;
	}
	

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(maxSize);
		dest.writeInt(size());
		for( int i = 0; i < size(); i++ ) {
			dest.writeParcelable(get(i), flags);
		}
		
		dest.writeString(id);
		
	}
	
	public void readFromParcel(Parcel in) {
		maxSize = in.readInt();
		
		int s = in.readInt();
		
		for( int i = 0; i < s; i++ )
			add( (PerformanceList) in.readParcelable(PerformanceList.class.getClassLoader()));
		
		total = size();
		id = in.readString();
		
		
		Collections.sort(this, new PerformanceListDateComparitor());
	}
	
	class PerformanceListDateComparitor implements Comparator<PerformanceList> {

		@Override
		public int compare(PerformanceList a, PerformanceList b) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			Date firstDate;
			Date secondDate;
			try {
				firstDate = df.parse(a.getDate());
				secondDate = df.parse(b.getDate());
			} catch (ParseException e) {
				return 0;
			}
			
			return firstDate.compareTo(secondDate);
		}
		
	}
}
