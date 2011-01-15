package com.davespanton.cineworld.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CineVO implements Parcelable{
	
	public static final Parcelable.Creator<CineVO> CREATOR = new Parcelable.Creator<CineVO>() {

		@Override
		public CineVO createFromParcel(Parcel in) {
			return new CineVO(in);
		}

		@Override
		public CineVO[] newArray(int size) {
			return new CineVO[size];
		}
	};
	
	private String cinemaId;
	private String filmId;
	private String date;
	
	public CineVO( Parcel in ) {
		readFromParcel(in);
	}
	
	public CineVO( String cinemaId ) {
		this( cinemaId, "" );
	}
	
	public CineVO( String cinemaId, String filmId ) {
		this( cinemaId, filmId, "" );
	}

	public CineVO( String cinemaId, String filmId, String date ) {
		this.cinemaId = cinemaId;
		this.filmId = filmId;
		this.date = date;
	}
	
	public String getCinemaId() {
		return cinemaId;
	}
	
	public String getFilmId() {
		return filmId;
	}
	
	public String getDate() {
		return date;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(cinemaId);
		dest.writeString(filmId);
		dest.writeString(date);
	}
	
	public void readFromParcel( Parcel in ) {
		cinemaId = in.readString();
		filmId = in.readString();
		date = in.readString();
	}
}
