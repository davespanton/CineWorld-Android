package com.davespanton.cineworld.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.jtmdb.Movie;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.adapters.CinemaAdapter;
import com.davespanton.cineworld.data.Cinema;
import com.davespanton.cineworld.data.CinemaList;
import com.davespanton.cineworld.data.MultiPerformanceList;
import com.davespanton.cineworld.services.CineWorldService;
import com.davespanton.cineworld.services.TmdbService;
import com.davespanton.cineworld.services.CineWorldService.Ids;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class FilmDetailsActivity extends Activity {
	
	private Logger mog = LoggerFactory.getLogger( FilmDetailsActivity.class );
	
	private TmdbService tmdbService;
	private CineWorldService cineworldService;
	
	//private Movie movie;
    
    private ProgressDialog mLoaderDialog;
    
	//private TextView body;
	
	protected void onConnected() {
		//tmdbService.search(getIntent().getStringExtra("title"));
		
	}
	
	protected void onCineworldServiceConnected() {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView( R.layout.film_details );
		
		// Bind services.
		bindService( new Intent(this, TmdbService.class), serviceConn, BIND_AUTO_CREATE);
		bindService( new Intent(this, CineWorldService.class), cineworldServiceConn, BIND_AUTO_CREATE);
		
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
		
		// Setup dates/times button.
		Button showDates = (Button) findViewById( R.id.date_times );
		//showDates.setEnabled( hasCinemaId() );
		showDates.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				if( hasCinemaId() ) 
					requestDateTimes();
				else
					cineworldService.requestCinemaListForFilm(getIntent().getStringExtra("filmId"));
			}
		});
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
		unregisterReceiver(cineWorldErrorReceiver);
		
		if( mLoaderDialog != null && mLoaderDialog.isShowing() )
			mLoaderDialog.dismiss();
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		
		registerReceiver(tmdbReceiver, new IntentFilter(TmdbService.TMDB_DATA_LOADED));
		registerReceiver(cineworldReceiver, new IntentFilter(CineWorldService.CINEWORLD_DATA_LOADED));
		registerReceiver(cineWorldErrorReceiver, new IntentFilter(CineWorldService.CINEWORLD_ERROR));
		
		//TODO	resume progress dialog?
	}
	
	protected boolean hasCinemaId() {
		return getIntent().getStringExtra("cinemaId") != null;
	}
	
	protected void updateImageView( ImageView target, Drawable image ) {
		target.setImageDrawable(image);
	}
	
	protected void showLoaderDialog() {
		mLoaderDialog = ProgressDialog.show( this, "", getString(R.string.loading_data) );
	}
	
	protected void showCinemasDialog() {
		
	}
	
	protected void requestDateTimes() {
				
		cineworldService.requestPerformancesForFilmCinema(
				getIntent().getStringExtra("cinemaId"), 
				getIntent().getStringExtra("filmId")
		);
			
		showLoaderDialog();
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
			
			//movie = tmdbService.getMovie( getIntent().getStringExtra("title") );
			
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
					
					//	currently unused
					
					break;
		
				case WEEK_TIMES:
					
					MultiPerformanceList mpl = (MultiPerformanceList) intent.getParcelableExtra("data");
					Intent i = new Intent(getBaseContext(), DateListActivity.class);
					i.putExtra("data", (Parcelable) mpl);
					
					if( mLoaderDialog != null && mLoaderDialog.isShowing() )
						mLoaderDialog.dismiss();
						
					startActivity(i);
					break;
					
				case FILM_CINEMA:
					
					mog.debug("received film cinema data");
					CinemaList cinemaList = (CinemaList) intent.getSerializableExtra("data");
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Choose a cinema"); //TODO	- move this string
					builder.setAdapter(new CinemaAdapter<Cinema>(getBaseContext(), R.layout.popup_list_layout, R.id.popup_list_text, cinemaList ), new DialogInterface.OnClickListener() {
							
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							
						}
						
					});
					AlertDialog alert = builder.create();
					alert.show();
				break;
			}
		}
	};
	
	private BroadcastReceiver cineWorldErrorReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if( mLoaderDialog != null && mLoaderDialog.isShowing() )
				mLoaderDialog.dismiss();
			mog.debug("An error has occured.");
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			
			builder.setMessage(getString(R.string.something_wrong) + " " + getString(R.string.try_again))
			.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				} 
			});
			
			AlertDialog alert = builder.create();
			alert.show();
			
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
