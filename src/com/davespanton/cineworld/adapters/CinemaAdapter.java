package com.davespanton.cineworld.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.davespanton.cineworld.data.Cinema;

public class CinemaAdapter<T extends Cinema> extends ArrayAdapter<T> {
	
	private int mResource;
	private int mTextViewResource;
	
	public CinemaAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		mResource = resource;
		mTextViewResource = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflator = LayoutInflater.from(getContext());
		View row = inflator.inflate(mResource, parent, false);
		TextView label = (TextView) row.findViewById(mTextViewResource);
		
		label.setText( ((Cinema) getItem(position)).getName() );
		
		return (row);
		
	}
}
