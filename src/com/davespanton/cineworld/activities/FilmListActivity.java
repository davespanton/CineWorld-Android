package com.davespanton.cineworld.activities;


import java.util.ArrayList;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import com.davespanton.cineworld.services.CineWorldService;

public class FilmListActivity extends ListActivity {

	public static final int CONTEXT_VIEW_INFO = 0;
	
	public enum Types { ALL, CINEMA };
	
	private int mSelectedIndex;

	private FilmList mFilmList;
	
	private CineWorldService cineWorldService;
	
	private Types type = Types.ALL;
	
	public void onConnected() {
		
		ArrayList<String> data = null;
		
		switch( type )
		{
			//TODO assumes data is ready. maybe better to put loader here than on previous activity?
			case ALL:
				data = cineWorldService.getFilmNames();
				mFilmList = cineWorldService.getFilmList();
				break;
			
			case CINEMA:
				data = cineWorldService.getFilmNamesForCurrentCinema();
				mFilmList = cineWorldService.getFilmListForCurrentCinema();
				break;
		}
		
		setListAdapter( new ArrayAdapter<String>( this, R.layout.list_layout, data));
		registerForContextMenu( getListView() );
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.films);
		
		bindService( new Intent(this, CineWorldService.class), service, BIND_AUTO_CREATE );
		
		type = (Types) getIntent().getSerializableExtra("type");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(service);
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
				startFilmDetailsActivity();
				return true;
		}
		
		return result; 
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		
		cineWorldService.setCurrentFilm(position);
		mSelectedIndex = position;
		
		startFilmDetailsActivity();
	}
	
	protected void startFilmDetailsActivity() {
		Intent i = new Intent(this, FilmDetailsActivity.class);
		
		Film f = mFilmList.get(mSelectedIndex);
		i.putExtra( "poster_url", f.getPosterUrl() );
		i.putExtra( "still_url", f.getStillUrl() );
		i.putExtra( "title", f.getTitle() );
		i.putExtra( "rating", f.getRating() );
		i.putExtra( "advisory", f.getAdvisory() );
		
		startActivity(i);
	}
	
	private ServiceConnection service = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			cineWorldService = ((CineWorldService.LocalBinder)service).getService();
			onConnected();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			cineWorldService = null;
		}
		
	};

}
