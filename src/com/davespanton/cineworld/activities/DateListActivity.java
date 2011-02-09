package com.davespanton.cineworld.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.R.anim;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.data.MultiPerformanceList;
import com.davespanton.cineworld.data.Performance;
import com.davespanton.cineworld.data.PerformanceList;
import com.davespanton.util.DateUtils;
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
		
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mog.debug( "Parent item position: " + Integer.toString(position));
		super.onListItemClick(l, v, position, id);
	}
	
	private AdapterView.OnItemClickListener gridItemClickListener = new AdapterView.OnItemClickListener()	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String url = mMultiPerformanceList.get(position).get(((AdapterView<?>)parent.getParent().getParent()).getPositionForView(parent)).getBookingUrl();
			
			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
			startActivity(browserIntent);
		}
	};
	
	@SuppressWarnings("unchecked")
	class DateListAdapter extends ArrayAdapter<MultiPerformanceList> {

		SimpleDateFormat dateFormat = new SimpleDateFormat();
		Date dateHolder;
		
		public DateListAdapter( MultiPerformanceList list ) {
			super(DateListActivity.this, R.id.performance_date, R.id.list_text, (List) list );
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflator = getLayoutInflater();
			View row = inflator.inflate(R.layout.performace_times, parent, false);
			
			TextView label = (TextView) row.findViewById(R.id.performance_date);
			GridView grid = (GridView) row.findViewById(R.id.performance_grid);
			
			dateFormat.applyPattern("yyyyMMdd");
			try {
				dateHolder = dateFormat.parse(mMultiPerformanceList.get(position).getDate());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			dateFormat.applyPattern("EEEE dd MMMM");
			String d = dateFormat.format(dateHolder);
			int dI = d.indexOf("day");
			d = d.substring(0, dI+6) + DateUtils.getOrdinal(dateHolder.getDate()) + d.substring(dI+6);
			label.setText( d );
			grid.setAdapter( new PerformanceAdapter(mMultiPerformanceList.get(position)));
			grid.setOnItemClickListener(gridItemClickListener);
			return (row);
		}
	}
	
	class PerformanceAdapter<E extends List<?>> extends ArrayAdapter<E> {
		
		PerformanceList performances;
		
		public PerformanceAdapter( List<E> list ) {
			super(DateListActivity.this, R.id.list_text, list);
			
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflator = getLayoutInflater();
			View row = inflator.inflate(R.layout.list_layout, parent, false);
			TextView label = (TextView) row.findViewById(R.id.list_text);
			
			label.setText( ((Performance) getItem(position)).getTime() );
			
			return (row);
			
		}
	}

}
