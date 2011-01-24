package com.davespanton.cineworld.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.data.MultiPerformanceList;
import com.davespanton.cineworld.data.Performance;
import com.davespanton.cineworld.data.PerformanceList;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class DateListActivity extends ListActivity {
	
	public static final Logger mog = LoggerFactory.getLogger(DateListActivity.class);
	
	MultiPerformanceList mMultiPerformanceList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.performances);
		
		mMultiPerformanceList = (MultiPerformanceList) getIntent().getParcelableExtra("data");
		mog.debug("creating datelist from: " + Integer.toString(mMultiPerformanceList.size()));
		setListAdapter(new DateListAdapter(mMultiPerformanceList));
	}
	
	@SuppressWarnings("unchecked")
	class DateListAdapter extends ArrayAdapter<MultiPerformanceList> {

		public DateListAdapter( MultiPerformanceList list ) {
			super(DateListActivity.this, R.id.performance_date, R.id.list_text, (List) list );
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflator = getLayoutInflater();
			View row = inflator.inflate(R.layout.performace_times, parent, false);
			TextView label = (TextView) row.findViewById(R.id.performance_date);
			GridView grid = (GridView) row.findViewById(R.id.performance_grid);
			
			if(mMultiPerformanceList.get(position) != null) {
				label.setText( mMultiPerformanceList.get(position).getDate() );
				grid.setAdapter( new PerformanceAdapter(mMultiPerformanceList.get(position)));
			}
			
			return (row);
			
		}
	}
	
	class PerformanceAdapter<T extends List<T>> extends ArrayAdapter<T> {
		
		PerformanceList performances;
		
		public PerformanceAdapter( T list ) {
			super(DateListActivity.this, R.id.list_text, list);
			
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflator = getLayoutInflater();
			View row = inflator.inflate(R.layout.list_layout, parent, false);
			TextView label = (TextView) row.findViewById(R.id.list_text);
			mog.debug( "setting up grid");
			label.setText( ((Performance) getItem(position)).getTime() );
			 
			return (row);
			
		}
	}

}
