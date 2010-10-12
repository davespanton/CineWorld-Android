package com.davespanton.cineworld;

import com.davespanton.cineworld.activities.CinemaListActivity;
import com.davespanton.cineworld.activities.FilmListActivity;
import com.davespanton.cineworld.services.CineWorldService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
	
	private ProgressDialog loaderDialog;
	
	protected void onConneted() {
		if( checkDataReady() )
			loaderDialog.dismiss();
	}
	
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
				startFilmActivity( FilmListActivity.Types.ALL );
			}
		
       });
        
       bindService( new Intent(this, CineWorldService.class), service, BIND_AUTO_CREATE);
       
       loaderDialog = ProgressDialog.show(Main.this, "", "Loading data. Please wait...");
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(service);
	}

	@Override
	protected void onPause() {
		
		super.onPause();
		
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		
		registerReceiver(receiver, new IntentFilter(CineWorldService.CINEWORLD_DATA_LOADED));
		
		if( checkDataReady() && loaderDialog.isShowing() )
			loaderDialog.dismiss();
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
				startFilmActivity( 	FilmListActivity.Types.ALL );
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	protected boolean checkDataReady() {
		if( cineWorldService == null )
			return false;
		else
			return cineWorldService.getCinemaDataReady() && cineWorldService.getFilmDataReady();
	}
	
	
	private void startCinemaActivity() {
		Intent i = new Intent(this, CinemaListActivity.class);
		startActivity( i );
	}
	
	private void startFilmActivity( FilmListActivity.Types type ) {
		Intent i = new Intent( this, FilmListActivity.class);
		
		i.putExtra( "type", type );
	
		startActivity(i);
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
				loaderDialog = ProgressDialog.show(Main.this, "", getString(R.string.loading_data));
			
				break;
			case FILMS_RESULT:
			
				//TODO	launch film details activity.
			
				break; 
			case CINEMA_FILMS_RESULT:
				
				//TODO	launch film details activity.
			
			break;
		}
		
	}
	
	//TODO	tie this into broadcasts received from the service.
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
			onConneted();
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
				case CINEMA:
				case FILM:
					if( checkDataReady()) 
						loaderDialog.dismiss();
					break;
				case CINEMA_FILM:
						loaderDialog.dismiss();
						startFilmActivity( 	FilmListActivity.Types.CINEMA );
					break;
			}
			
		}
	};
}