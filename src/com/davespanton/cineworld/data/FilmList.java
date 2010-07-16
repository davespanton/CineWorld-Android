package com.davespanton.cineworld.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class FilmList extends ArrayList<Film> implements Parcelable {

	private static final long serialVersionUID = -3216366151872044360L;
	
	public FilmList() {
		 
	}
	
	public FilmList( Parcel in ) {
		readFromParcel(in);
	}
	
	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Object createFromParcel(Parcel in) {
			return new FilmList(in);
		}

		@Override
		public Object[] newArray(int size) {
			return null;
		}
		
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int size = this.size();
		
		dest.writeInt(size);
		
		for( int i = 0; i < size; i++ ) {
			Film f = this.get(i);
			
			dest.writeString(f.getTitle());
			dest.writeString(f.getRating());
			dest.writeString(f.getAdvisory());
			dest.writeString(f.getPosterUrl());
			dest.writeString(f.getStillUrl());
			dest.writeString(f.getFilmUrl());
			dest.writeString(f.getEdi());
		}

	}
	
	private void readFromParcel(Parcel in) {
		this.clear();
		
		int size = in.readInt();
		
		for( int i = 0; i < size; i++ ){
			Film f = new Film();
			
			f.setEdi(in.readString());
			f.setFilmUrl(in.readString());
			f.setStillUrl(in.readString());
			f.setPosterUrl(in.readString());
			f.setAdvisory(in.readString());
			f.setRating(in.readString());
			f.setTitle(in.readString());
			
			this.add(f);
		}
	}

}
