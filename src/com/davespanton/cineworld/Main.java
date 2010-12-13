package com.davespanton.cineworld;

import com.davespanton.cineworld.activities.CinemaListActivity;
import com.davespanton.cineworld.activities.FilmListActivity;
import com.davespanton.cineworld.services.CineWorldService;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.google.code.microlog4android.format.PatternFormatter;

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

public class Main extends Activity {
	
	private static final Logger mog = LoggerFactory.getLogger(Main.class);
    
	private static final int VIEW_CINEMAS = 0;
	private static final int VIEW_FILMS = 1;
	
	private Button mCinemaButton;
	private Button mFilmButton;
		
	private CineWorldService cineWorldService = null;
	
	private ProgressDialog loaderDialog;
	
	protected void onConneted() {
		if( checkDataReady() )
			loaderDialog.dismiss();
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       
    	super.onCreate(savedInstanceState);
    	
    	PropertyConfigurator propConfig = PropertyConfigurator.getConfigurator(this);
    	propConfig.configure();
    	
    	PatternFormatter patternFormatter = new PatternFormatter();
    	patternFormatter.setPattern("%c %d [%P] %m %T");
    	
    	FileAppender fileAppender = new FileAppender();
    	fileAppender.setFileName("cineworld.log");
    	fileAppender.setFormatter(patternFormatter);
    	mog.addAppender(fileAppender);
    	
    	mog.debug("Main onCreate");
    	
    	setContentView(R.layout.main);
        
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
       
    	loaderDialog = ProgressDialog.show( Main.this, "",  getString(R.string.loading_data) );
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(service);
		
		mog.debug("Main onDestroy");
	}

	@Override
	protected void onPause() {
		
		super.onPause();
		
		if( loaderDialog != null && loaderDialog.isShowing() )
			loaderDialog.dismiss();
		
		unregisterReceiver(dataReceiver);
		unregisterReceiver(errorReceiver);
	}

	@Override
	protected void onResume() {
		
		mog.debug( "Main onResume: data ready... " + Boolean.toString(checkDataReady()) );
		super.onResume();
		
		registerReceiver(dataReceiver, new IntentFilter(CineWorldService.CINEWORLD_DATA_LOADED));
		registerReceiver(errorReceiver, new IntentFilter(CineWorldService.CINEWORLD_ERROR));
		
		if( checkDataReady() && loaderDialog.isShowing() )
			loaderDialog.dismiss();
		else if( !checkDataReady() && !loaderDialog.isShowing()) {
			loaderDialog = ProgressDialog.show(this, "", getString(R.string.loading_data));
		}
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
	
	private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			CineWorldService.Ids id = (CineWorldService.Ids) intent.getSerializableExtra("id");
			
			switch( id ) {
				
				case CINEMA:
				case FILM:
					mog.debug( id.toString() + " received data ready " + Boolean.toString(checkDataReady()) );
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
	
	private BroadcastReceiver errorReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			//if( loaderDialog != null && loaderDialog.isShowing() )
			//	TODO	alert user of error
		}
		
	};
}