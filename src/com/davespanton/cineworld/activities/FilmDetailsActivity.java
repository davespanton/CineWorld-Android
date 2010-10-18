package com.davespanton.cineworld.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.jtmdb.Movie;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.services.TmdbService;
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
import android.widget.ImageView;
import android.widget.TextView;

public class FilmDetailsActivity extends Activity {

	private TmdbService tmdbService;
	
	private Movie movie;
	
	private TextView body;
	
	protected void onConnected() {
		tmdbService.search(getIntent().getStringExtra("title"));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView( R.layout.film_details );
		
		String stillUrl = getIntent().getStringExtra("still_url"); 
		
		//Drawable img = ImageOperations(this, stillUrl, "film.jpg");
		
		TextView text = (TextView) findViewById( R.id.film_title );
		text.setText( getIntent().getStringExtra("title"));
		
		WebImageView image = (WebImageView) findViewById( R.id.still_image );
		image.setImageUrl( stillUrl );
		image.loadImage();
		//ImageView image = (ImageView) findViewById( R.id.still_image );
		//image.setImageDrawable(img);
		
		//FetchImageTask fetch = new FetchImageTask();
		//fetch.target = image;
		//fetch.execute(stillUrl);
		
		TextView rating = (TextView) findViewById( R.id.film_rating );
		rating.setText( getString(R.string.rating) + ": " + getIntent().getStringExtra("rating") );
		
		//TextView advisory = (TextView) findViewById( R.id.film_advisory ); 
		//advisory.setText( getIntent().getStringExtra("advisory"));
		
		body = (TextView) findViewById( R.id.film_body );
		
		bindService( new Intent(this, TmdbService.class), service, BIND_AUTO_CREATE);
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
		
		registerReceiver(receiver, new IntentFilter(TmdbService.TMDB_DATA_LOADED));
	}
	
	private void updateImageView( ImageView target, Drawable image ) {
		target.setImageDrawable(image);
	}
	
	private ServiceConnection service = new ServiceConnection() {
		
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
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			boolean success = intent.getBooleanExtra("success", false);
			
			movie = tmdbService.getMovie( getIntent().getStringExtra("title") );
			
			if( success && movie != null)
				body.setText( movie.getOverview() );
			else
				body.setText( R.string.no_information );
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
				content = url.getContent();
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
