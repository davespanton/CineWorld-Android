package com.davespanton.cineworld.services;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import net.sf.jtmdb.GeneralSettings;
import net.sf.jtmdb.Movie;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TmdbService extends Service {

	private final Binder binder = new LocalBinder();
	
	private String pendingQuery = "";
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		GeneralSettings.setApiKey( "779c40d6ac22bacf3464f3256dc67ec4" );
	}

	public class LocalBinder extends Binder {
		public TmdbService getService() {
			return TmdbService.this; 
		}
	}
	
	public void search( String query ) {
		FetchMovieTask task = new FetchMovieTask();
		task.execute( query );
	}
	
	protected void processResult( List<Movie> result ) {
		
		for( Movie movie : result ) {
			
			Log.v( "TmdbService", "ENTRY " + movie.getName() );
			
			if( movie.getName() == pendingQuery )
				Log.v( "TmdbService", "MATCH " + movie.getName() );
				
		}
	}

	class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

		@Override
		protected List<Movie> doInBackground(String... query) {
			try {
				return Movie.search( query[0] );
			} catch (IOException e) {
				// TODO notify the user of error
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO notify the user of error
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Movie> result) {
			super.onPostExecute(result);
			
			processResult( result );
		}
		
	}
}
