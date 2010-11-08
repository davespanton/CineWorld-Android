package com.davespanton.cineworld.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class CinemaList extends ArrayList<Cinema> implements Parcelable {

	private static final long serialVersionUID = -3834484660924113824L;

	public CinemaList() {
		
	}
	
	public CinemaList( Parcel in ) {
		readFromParcel( in );
	}
	
	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() { 

		@Override
		public Object createFromParcel(Parcel in) {
			return new CinemaList(in);
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
			Cinema c = new Cinema();
			
			c.setName(in.readString());
			c.setAddress(in.readString());
			c.setPostcode(in.readString());
			c.setTelephone(in.readString());
			c.setId(in.readString());
			
			this.add(c);
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) { 
		int size = this.size();
		
		dest.writeInt(size);
		
		for( int i = 0; i < size; i++ ) {
			Cinema c = this.get(i);
			
			dest.writeString( c.getName() );
			dest.writeString( c.getAddress() );
			dest.writeString( c.getPostcode() );
			dest.writeString( c.getTelephone() );
			dest.writeString( c.getId());
		}
		
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
