package com.davespanton.cineworld.activities;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
	
	private String mCinemaId = null;
	
	private CineWorldService cineWorldService;
	
	private Types type = Types.ALL;

	private ProgressDialog mLoaderDialog;
	
	public void onConnected() {
		
		mCinemaId = getIntent().getStringExtra("cinemaId");
		
		switch( type )
		{
			case ALL:
				cineWorldService.requestFilmList();
				break;
			
			case CINEMA:
				cineWorldService.requestFilmListForCinema(mCinemaId);
				break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.films);
		
		type = (Types) getIntent().getSerializableExtra("type");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		mCinemaId = null;
		
		bindService( new Intent(this, CineWorldService.class), service, BIND_AUTO_CREATE );
		
		registerReceiver(receiver, new IntentFilter(CineWorldService.CINEWORLD_DATA_LOADED));
		registerReceiver(errorReceiver, new IntentFilter(CineWorldService.CINEWORLD_ERROR));
		
		mLoaderDialog = ProgressDialog.show(FilmListActivity.this, "", getString(R.string.loading_data) );
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		unbindService(service);
		unregisterReceiver(receiver);
		unregisterReceiver(errorReceiver);
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
		i.putExtra( "cinemaId", mCinemaId );
		i.putExtra( "filmId", f.getEdi() );
		
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
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			CineWorldService.Ids id = (CineWorldService.Ids) intent.getSerializableExtra("id");
			
			switch( id ) {
				case FILM:
				case CINEMA_FILM:
						if( mLoaderDialog != null && mLoaderDialog.isShowing())
							mLoaderDialog.dismiss();
					
						mFilmList = (FilmList) intent.getSerializableExtra("data");
						setListAdapter( new FilmAdapter() );
						registerForContextMenu(getListView());
					break;
			}
			
		}
	};
	
	private BroadcastReceiver errorReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if( mLoaderDialog != null && mLoaderDialog.isShowing() )
				mLoaderDialog.dismiss();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			
			builder.setMessage(getString(R.string.something_wrong) + " " + getString(R.string.try_again))
			.setPositiveButton(getString(R.string.okay), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();	
				}
			});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
		
	};
	
	@SuppressWarnings("unchecked")
	class FilmAdapter extends ArrayAdapter {

		public FilmAdapter() {
			super(FilmListActivity.this, R.id.list_text, mFilmList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflator = getLayoutInflater();
			View row = inflator.inflate(R.layout.list_layout, parent, false);
			TextView label = (TextView) row.findViewById(R.id.list_text);
			
			label.setText( mFilmList.get(position).getTitle() );
			
			return (row);
			
		}
	};
}
