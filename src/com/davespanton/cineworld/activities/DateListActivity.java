package com.davespanton.cineworld.activities;

import java.util.ArrayList;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.data.PerformanceList;
import com.davespanton.cineworld.services.CineWorldService;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class DateListActivity extends ListActivity {
	
	private CineWorldService cineWorldService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.performances);
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		bindService(new Intent(this, CineWorldService.class), service, BIND_AUTO_CREATE);
	}
	
	private ServiceConnection service = new ServiceConnection() {

				@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			cineWorldService = ((CineWorldService.LocalBinder)service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			cineWorldService = null;
		}
	};
	
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
