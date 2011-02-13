package com.davespanton.cineworld.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.data.Cinema;

public class CinemaAdapter<T extends Cinema> extends ArrayAdapter<T> {
	
	
	public CinemaAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflator = LayoutInflater.from(getContext());
		View row = inflator.inflate(R.layout.list_layout, parent, false);
		TextView label = (TextView) row.findViewById(R.id.list_text);
		
		label.setText( ((Cinema) getItem(position)).getName() );
		
		return (row);
		
	}
}
