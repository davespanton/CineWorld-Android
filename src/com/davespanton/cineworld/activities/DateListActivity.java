package com.davespanton.cineworld.activities;

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

public class DateListActivity extends ListActivity {
	
	MultiPerformanceList mMultiPerformanceList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.performances);
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
			label.setText( "huge badger" );
			//grid.setAdapter(null);
			
			return (row);
			
		}
		
	}

}
