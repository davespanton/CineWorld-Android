package com.davespanton.cineworld.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.jtmdb.Movie;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.services.CineWorldService;
import com.davespanton.cineworld.services.TmdbService;
import com.davespanton.cineworld.services.CineWorldService.Ids;
import com.github.droidfu.widgets.WebImageView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class FilmDetailsActivity extends Activity {

	private TmdbService tmdbService;
	private CineWorldService cineworldService;
	
	private Movie movie;
	
	//private TextView body;
	
	protected void onConnected() {
		//tmdbService.search(getIntent().getStringExtra("title"));
		
	}
	
	protected void onCineworldServiceConnected() {
		if( cineworldService.getDatesForCurrentFilm() != null )
			Log.v( "DATE CONN", cineworldService.getDatesForCurrentFilm().toString() );
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView( R.layout.film_details );
		
		String stillUrl = getIntent().getStringExtra("poster_url"); 
				
		TextView text = (TextView) findViewById( R.id.film_title );
		text.setText( getIntent().getStringExtra("title"));
		
		//WebImageView image = (WebImageView) findViewById( R.id.still_image );
		//image.setImageUrl( stillUrl );
		//image.loadImage();
		
		ImageView image = (ImageView) findViewById( R.id.still_image );
				
		FetchImageTask fetch = new FetchImageTask();
		fetch.target = image;
		fetch.execute(stillUrl);
		
		TextView rating = (TextView) findViewById( R.id.film_rating );
		rating.setText( getString(R.string.rating) + ": " + getIntent().getStringExtra("rating") );
				
		//body = (TextView) findViewById( R.id.film_body );
		
		bindService( new Intent(this, TmdbService.class), serviceConn, BIND_AUTO_CREATE);
		bindService( new Intent(this, CineWorldService.class), cineworldServiceConn, BIND_AUTO_CREATE);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(serviceConn);
		unbindService(cineworldServiceConn);
	}
	
	@Override
	protected void onPause() {
		
		super.onPause();
		
		unregisterReceiver(tmdbReceiver);
		unregisterReceiver(cineworldReceiver);
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		
		registerReceiver(tmdbReceiver, new IntentFilter(TmdbService.TMDB_DATA_LOADED));
		registerReceiver(cineworldReceiver, new IntentFilter(CineWorldService.CINEWORLD_DATA_LOADED));
	}
	
	private void updateImageView( ImageView target, Drawable image ) {
		target.setImageDrawable(image);
	}
	
	private ServiceConnection serviceConn = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			tmdbService = ((TmdbService.LocalBinder)service).getService();
			onConnected();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			tmdbService = null;
		}
		
	};
	
	private ServiceConnection cineworldServiceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			cineworldService = ((CineWorldService.LocalBinder)service).getService();
			onCineworldServiceConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			cineworldService = null;
			
		}
		
	};
	
	private BroadcastReceiver tmdbReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			//boolean success = intent.getBooleanExtra("success", false);
			
			movie = tmdbService.getMovie( getIntent().getStringExtra("title") );
			
			/*if( success && movie != null)
				body.setText( movie.getOverview() );
			else
				body.setText( R.string.no_information );*/
		}
	};
	
	private BroadcastReceiver cineworldReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			switch( (Ids) intent.getSerializableExtra("id") ) {
				case FILM_DATES:
					//TODO enable dates/times button
					Log.v("DATES REC", cineworldService.getDatesForCurrentFilm().toString() );
					break;
			}
		}
		
	};
	
	class FetchImageTask extends AsyncTask< String, Void, Object > {
		
		public ImageView target;
		
		@Override
		protected Object doInBackground(String... address) {
			
			URL url = null;
			Object content = null;
			
			try {
				url = new URL(address[0]);
				content = url.getContent(); //can cause a null pointer exception here
			} 
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return content;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			InputStream is = (InputStream) result;
			Drawable d = Drawable.createFromStream(is, "src");
			updateImageView(target, d);
		}
		
	}
	
	
}
