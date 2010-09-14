package com.davespanton.cineworld;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.davespanton.cineworld.activities.CinemaListActivity;
import com.davespanton.cineworld.activities.FilmListActivity;
import com.davespanton.cineworld.services.CineWorldService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Main extends Activity {
    
	private static final int VIEW_CINEMAS = 0;
	private static final int VIEW_FILMS = 1;
	
	private static final int CINEMAS_RESULT = 0;
	private static final int FILMS_RESULT = 1;
	private static final int CINEMA_FILMS_RESULT = 2;
	
	private TextView mMainText;
	private Button mCinemaButton;
	private Button mFilmButton;
		
	private CineWorldService cineWorldService;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
        
       mMainText = (TextView) findViewById(R.id.main_text);
       mCinemaButton = (Button) findViewById(R.id.cinema_button);
       mFilmButton = (Button) findViewById(R.id.film_button);
        
       mCinemaButton.setOnClickListener( new OnClickListener() {
			
    	   @Override
			public void onClick(View v) {
				startCinemaActivity();
			}
		
       });
        
       mFilmButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startFilmActivity();
			}
		
       });
        
       bindService( new Intent(this, CineWorldService.class), service, BIND_AUTO_CREATE);
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(service);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		boolean result = super.onCreateOptionsMenu(menu);
		
		menu.add(0, VIEW_CINEMAS, 0, R.string.view_cinemas);
		menu.add(0, VIEW_FILMS, 0, R.string.view_films);
		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch( item.getItemId() ) {
			case VIEW_CINEMAS:
				startCinemaActivity();
				return true;
			case VIEW_FILMS:
				startFilmActivity();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void startCinemaActivity() {
		Intent i = new Intent(this, CinemaListActivity.class);
		startActivityForResult(i, CINEMAS_RESULT);
	}
	
	private void startFilmActivity() {
		Intent i = new Intent( this, FilmListActivity.class);
		Bundle b = new Bundle();
		int request;
		if( cineWorldService.getCurrentCinema() == null || cineWorldService.getFilmNamesForCurrentCinema() == null ) {
			
			i.putExtra( "type", FilmListActivity.Types.ALL );
			
			request = FILMS_RESULT; 
		}
		else {
			
			i.putExtra( "type", FilmListActivity.Types.CINEMA );
			
			request = CINEMA_FILMS_RESULT;
		}
		
		startActivityForResult(i, request);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		if( resultCode == -1 )
			return;
		
		switch( requestCode ) {
			case CINEMAS_RESULT:
			
				cineWorldService.setCurrentCinema( resultCode );
				updateMainText();
				startFilmActivity();
			
				break;
			case FILMS_RESULT:
			
				//TODO	launch film details activity.
			
				break; 
			case CINEMA_FILMS_RESULT:
				
				//TODO	launch film details activity.
			
			break;
		}
		
	}
	
	private void updateMainText( ) {
		
		String cinema = "";
		String film = "";
		
		if( cineWorldService.getCurrentCinema() != null )
				cinema = getString(R.string.current_cinema) + cineWorldService.getCurrentCinema().getName();
		else
			cinema = getString(R.string.no_current_cinema);
		
		//TODO	maybe add in current film... if it's going to be used.		
		
		mMainText.setText( cinema + "\n" + film );
	}
	
	private ServiceConnection service = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			cineWorldService = ((CineWorldService.LocalBinder)service).getService();
			updateMainText();
			Log.d("Main", "connected to service");
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			cineWorldService = null;
		}
	};
	
}