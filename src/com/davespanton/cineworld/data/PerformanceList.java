package com.davespanton.cineworld.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class PerformanceList extends ArrayList<Performance> implements
		Parcelable {
	
	private static final long serialVersionUID = 8451767216059387721L;

	public PerformanceList() {
		
	}
	
	public PerformanceList( Parcel in ) {
		readFromParcel(in);
	}
	
	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Object createFromParcel(Parcel in) {
			return new PerformanceList(in);
		}

		@Override
		public Object[] newArray(int size) {
			return null;
		}
		
	};
	
	private void readFromParcel(Parcel in) {
		this.clear();
		
		int size = in.readInt();
		
		for( int i = 0; i < size; i++ ){
			Performance p = new Performance(); 
			
			p.setAd(in.readInt()==1);
			p.setAvailable(in.readInt()==1);
			p.setBookingUrl(in.readString());
			p.setSubtitled(in.readInt()==1);
			p.setTime(in.readString());
			p.setType(in.readString());
			
			this.add(p);
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int size = this.size();
		
		dest.writeInt(size);
		
		for( int i = 0; i < size; i++ ) {
			Performance p = this.get(i);
			
			dest.writeInt( p.isAd() ? 1 : 0 );
			dest.writeInt( p.isAvailable() ? 1 : 0 );
			dest.writeString( p.getBookingUrl() );
			dest.writeInt( p.isSubtitled() ? 1 : 0 );
			dest.writeString( p.getTime() );
			dest.writeString( p.getType() );
			
		}

	}
	
	@Override
	public int describeContents() {
		return 0;
	}

}
