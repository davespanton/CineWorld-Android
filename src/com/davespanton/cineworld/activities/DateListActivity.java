package com.davespanton.cineworld.activities;

import java.util.ArrayList;

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
import com.davespanton.cineworld.data.PerformanceList;

public class DateListActivity extends ListActivity {
	
	MultiPerformanceList mMultiPerformanceList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.performances);
		
		mMultiPerformanceList = (MultiPerformanceList) getIntent().getParcelableExtra("data");
	}
	
	@SuppressWarnings("unchecked")
	class DateListAdapter extends ArrayAdapter {

		public DateListAdapter() {
			super(DateListActivity.this, R.id.list_text  );
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflator = getLayoutInflater();
			View row = inflator.inflate(R.layout.performace_times, parent, false);
			TextView label = (TextView) row.findViewById(R.id.performance_date);
			GridView grid = (GridView) row.findViewById(R.id.performance_grid);
			
			//TODO	set title and grid up
			label.setText( mMultiPerformanceList.get(position).getDate() );
			//grid.setAdapter( new SomeSortOfPerformanceListAdapter());
			
			return (row);
			
		}
	}
	
	@SuppressWarnings("unchecked")
	class PerformanceAdapter extends ArrayAdapter {

		public PerformanceAdapter( ArrayList<PerformanceList> list ) {
			super(DateListActivity.this, R.id.list_text );
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflator = getLayoutInflater();
			View row = inflator.inflate(R.layout.list_layout, parent, false);
			TextView label = (TextView) row.findViewById(R.id.list_text);
			
			//label.setText( mPerformances.get(position).getTime() );
			 
			return (row);
			
		}
	}

}
