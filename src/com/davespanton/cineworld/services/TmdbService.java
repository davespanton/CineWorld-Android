package com.davespanton.cineworld.services;

import java.io.IOException;
import java.util.Hashtable;
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
	
	public static final String TMDB_DATA_LOADED = "tmdbDataLoaded";
	
	private final Binder binder = new LocalBinder();
	
	private String pendingQuery = "";
	
	private Hashtable<String, Movie> movieTable = new Hashtable<String, Movie>();
	
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
		
		if( movieTable.containsKey(query) )
			broadcastDataLoaded( true );
		else {
			pendingQuery = query;
			FetchMovieTask task = new FetchMovieTask();
			task.execute( query );
		}
	}
	
	public Movie getMovie( String title ) {
		
		if( movieTable.containsKey( title ) )
			return movieTable.get(title);
		else
			return null;
	}
	
	public boolean hasMovie( String title ) {
		return movieTable.containsKey( title );
	}
	
	
	protected void processResult( List<Movie> result ) {
		
		if( result == null || result.size() == 0 ) {
			broadcastDataLoaded(false);
			return;
		}
		//TODO	refine the process criteria
		Movie selectedResult = null;
		
		//steps up as more suitable matches are found.
		int level = 0;
		for( Movie movie : result ) {
			
			if( movie.getName().contentEquals( pendingQuery ) ) {
				selectedResult = movie;
				level = 4;
				break;
			}
			
			if( movie.getName().contains( pendingQuery ) && level < 3 ) {
				selectedResult = movie;
				level = 3;
			}
			
			if( movie.getAlternativeName().contentEquals( pendingQuery ) && level < 2 ) {
				selectedResult = movie;
				level = 2;
			}
			
			if( movie.getAlternativeName().contains( pendingQuery ) && level == 0 ) {
				selectedResult = movie;
				level = 1;
			}
		}
		
		//TODO	something if no results were returned.
		if( selectedResult != null )
			movieTable.put(pendingQuery, selectedResult);
		
		broadcastDataLoaded( selectedResult != null );
	}
	
	protected void broadcastDataLoaded( boolean success ) {
		Intent i = new Intent( TMDB_DATA_LOADED );
		Log.v( "SRC", Boolean.toString(success) );
		i.putExtra("success", success );
		sendBroadcast( i );
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
