package com.davespanton.cineworld.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.data.FilmList;

public class FilmListActivity extends ListActivity {

	public static final int CONTEXT_VIEW_INFO = 0;
	
	private int mSelectedIndex;

	private JSONArray rawData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.films);
		
		ArrayList<String> data = getIntent().getStringArrayListExtra("data");
		
		//TODO something useful with this :) Its a work in progress at the moment
		Bundle b = getIntent().getBundleExtra("films");
		FilmList f = b.getParcelable("films");
		
		setListAdapter( new ArrayAdapter<String>( this, R.layout.list_layout, data));
		
		//TODO tidy up
		try {
			rawData = (JSONArray) ((JSONObject) new JSONTokener(getIntent().getStringExtra("raw")).nextValue()).getJSONArray("films");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setResult(-1);
		
		registerForContextMenu( getListView() );
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, v, menuInfo);
		
		mSelectedIndex = (int) ((AdapterContextMenuInfo) menuInfo).id;
		menu.add(0, CONTEXT_VIEW_INFO, 0, R.string.view_info);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);
		
		switch( item.getItemId() ) {
			case CONTEXT_VIEW_INFO:
				Intent i = new Intent(this, FilmDetailsActivity.class);
				try {
					JSONObject film = (JSONObject) rawData.get(mSelectedIndex);
					i.putExtra( "poster_url", film.getString("poster_url"));
					i.putExtra( "title", film.getString("title"));
					i.putExtra( "rating", film.getString("classification"));
					i.putExtra( "advisory", film.getString("advisory"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startActivity(i);
				return true;
		}
		
		return result; 
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		
		setResult(position);
		finish();
	}

}
