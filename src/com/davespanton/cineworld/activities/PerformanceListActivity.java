package com.davespanton.cineworld.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.data.PerformanceList;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class PerformanceListActivity extends ListActivity {
	
	@SuppressWarnings("unused")
	private Logger mog = LoggerFactory.getLogger( PerformanceListActivity.class );
	
	private PerformanceList mPerformances;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		mPerformances = (PerformanceList) getIntent().getSerializableExtra("data");
		
		setContentView(R.layout.performances);
		setListAdapter( new PerformanceAdapter() );
	}
	
	

	@SuppressWarnings("unchecked")
	class PerformanceAdapter extends ArrayAdapter {

		public PerformanceAdapter() {
			super(PerformanceListActivity.this, R.id.list_text, mPerformances );
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflator = getLayoutInflater();
			View row = inflator.inflate(R.layout.list_layout, parent, false);
			TextView label = (TextView) row.findViewById(R.id.list_text);
			
			label.setText( mPerformances.get(position).getTime() );
			
			return (row);
			
		}
		
	}
	
}
