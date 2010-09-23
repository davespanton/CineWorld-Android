package com.davespanton.cineworld.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.services.CineWorldService;
import com.davespanton.cineworld.services.TmdbService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.TextView;

public class FilmDetailsActivity extends Activity {

	private TmdbService tmdbservice;
	
	protected void onConnected() {
		
		tmdbservice.search(getIntent().getStringExtra("title"));
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView( R.layout.film_details );
		
		String stillUrl = getIntent().getStringExtra("poster_url");
		
		Drawable img = ImageOperations(this, stillUrl, "film.jpg"	);
		
		TextView text = (TextView) findViewById( R.id.film_title );
		text.setText( getIntent().getStringExtra("title"));
		
		ImageView image = (ImageView) findViewById( R.id.still_image );
		image.setImageDrawable(img);
		
		TextView rating = (TextView) findViewById( R.id.film_rating );
		rating.setText( getIntent().getStringExtra("rating"));
		
		TextView advisory = (TextView) findViewById( R.id.film_advisory );
		advisory.setText( getIntent().getStringExtra("advisory"));
		
		bindService( new Intent(this, TmdbService.class), service, BIND_AUTO_CREATE);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(service);
	}

	// Thanks to Agus Santoso: http://asantoso.wordpress.com/2008/03/07/download-and-view-image-from-the-web/
	private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
		try {
			InputStream is = (InputStream) this.fetch(url);
			Drawable d = Drawable.createFromStream(is, "src");
			return d;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object fetch(String address) throws MalformedURLException,IOException {
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
	}
	
	private ServiceConnection service = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			tmdbservice = ((TmdbService.LocalBinder)service).getService();
			onConnected();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			tmdbservice = null;
		}
		
	};
}
