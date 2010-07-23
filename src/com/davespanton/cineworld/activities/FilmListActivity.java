package com.davespanton.cineworld.activities;

import java.util.ArrayList;

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
import com.davespanton.cineworld.data.Film;
import com.davespanton.cineworld.data.FilmList;

public class FilmListActivity extends ListActivity {

	public static final int CONTEXT_VIEW_INFO = 0;
	
	private int mSelectedIndex;

	private FilmList mFilmList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.films);
		
		ArrayList<String> data = getIntent().getStringArrayListExtra("data");
		
		Bundle b = getIntent().getBundleExtra("films");
		mFilmList = b.getParcelable("films");
		
		setListAdapter( new ArrayAdapter<String>( this, R.layout.list_layout, data));
		
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
				
				Film f = mFilmList.get(mSelectedIndex);
				i.putExtra( "poster_url", f.getPosterUrl() );
				i.putExtra( "title", f.getTitle() );
				i.putExtra( "rating", f.getRating() );
				i.putExtra( "advisory", f.getAdvisory() );
				
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
