package com.davespanton.cineworld.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import net.sf.jtmdb.Movie;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.data.PerformanceList;
import com.davespanton.cineworld.services.CineWorldService;
import com.davespanton.cineworld.services.TmdbService;
import com.davespanton.cineworld.services.CineWorldService.Ids;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

public class FilmDetailsActivity extends Activity {

	private static final int DATE_DIALOG_ID = 0;
	
	private Logger mog = LoggerFactory.getLogger( FilmDetailsActivity.class );
	
	private TmdbService tmdbService;
	private CineWorldService cineworldService;
	
	private Movie movie;
	
	private int mYear;
    private int mMonth;
    private int mDay;
    
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
		
		Button showDates = (Button) findViewById( R.id.date_times );
		
		showDates.setEnabled( getIntent().getStringExtra("cinemaId") != null );
		
		showDates.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
			
		});
		
		// get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
		
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
		
		if( mLoaderDialog != null && mLoaderDialog.isShowing() )
			mLoaderDialog.dismiss();
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		
		registerReceiver(tmdbReceiver, new IntentFilter(TmdbService.TMDB_DATA_LOADED));
		registerReceiver(cineworldReceiver, new IntentFilter(CineWorldService.CINEWORLD_DATA_LOADED));
		
		//TODO	resume progress dialog?
	}
	
	private void updateImageView( ImageView target, Drawable image ) {
		target.setImageDrawable(image);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		switch( id ) {
			
			case DATE_DIALOG_ID:
				return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
		}
		
		return null;
	}
	
	protected void showLoaderDialog() {
		mLoaderDialog = ProgressDialog.show( this, "", getString(R.string.loading_data) );
	}
	
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			
			Log.d("DATE", Integer.toString(year) + " " + Integer.toString(monthOfYear) + " " + Integer.toString(dayOfMonth));
			
			String day;
			if( dayOfMonth < 10 )
				day = "0" + Integer.toString(dayOfMonth);
			else
				day = Integer.toString(dayOfMonth);
			
			
			//TODO	check that the extra values exist in the below call.
			cineworldService.requestPerformancesForFilmCinema( 
					Integer.toString(year) + Integer.toString(monthOfYear+1) + day, 
					getIntent().getStringExtra("cinemaId"), 
					getIntent().getStringExtra("filmId") 
			);
			
			showLoaderDialog();
		}
		
	};
	
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
	
	private void startPerformancesActivity(PerformanceList performances) {
		Intent i = new Intent(this, PerformanceListActivity.class);
		i.putExtra( "data", (Parcelable) performances );
		startActivity(i);
	}
	
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
					
					//	currently unused
					
					break;
				case DATE_TIMES:
					if( mLoaderDialog != null && mLoaderDialog.isShowing() ) {
						mLoaderDialog.dismiss();
						PerformanceList performances = (PerformanceList) intent.getSerializableExtra("data");
						mog.debug( "Performance data received: " + Integer.toString(performances.size()) );
						startPerformancesActivity( performances );
					}
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
