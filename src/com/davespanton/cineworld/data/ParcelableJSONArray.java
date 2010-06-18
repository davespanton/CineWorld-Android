package com.davespanton.cineworld.data;

import org.json.JSONArray;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableJSONArray extends JSONArray implements Parcelable {

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub

	}

}
