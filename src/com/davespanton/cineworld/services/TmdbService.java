package com.davespanton.cineworld.services;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;

import com.davespanton.cineworld.ApiKey;

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
	
	private FetchMovieTask task = null;
	
	private String pendingQuery = "";
	
	private Hashtable<String, Movie> movieTable = new Hashtable<String, Movie>();
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		GeneralSettings.setApiKey( ApiKey.TMDB_KEY );
	}

	public class LocalBinder extends Binder {
		public TmdbService getService() {
			return TmdbService.this; 
		}
	}
	
	public void search( String query ) {
		
		if( movieTable.containsKey(query) )
			broadcastDataLoaded( true );
		else 
			initiateSearch(query);
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
	
	protected void initiateSearch( String query ) {
		pendingQuery = query;
		
		if( task != null )
			task.cancel(true);
		
		task = new FetchMovieTask();
		task.execute( query );
	}
	
	
	protected void processResult( List<Movie> result ) {
		Log.v( "TmdbService", pendingQuery );
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
		
		if( selectedResult != null )
			movieTable.put(pendingQuery, selectedResult);
		
		broadcastDataLoaded( selectedResult != null );
	}
	
	protected void broadcastDataLoaded( boolean success ) {
		Intent i = new Intent( TMDB_DATA_LOADED );
		i.putExtra("success", success );
		sendBroadcast( i );
	}
	
	/* ******************************************************
	 *	Part of an Experiment into multi-stage searching,	*
	 *	trying to ignore certain combinations found in 		*
	 *	queries. Wasn't really accurate enough.				*
	 ********************************************************/
	
	//private String[] multiStageStrings = { "in 3D", "in 2D", "3D", "2D" };
	//private boolean multiStageTask = false;
	//private int taskStage = 0;
	
	/*protected void nextStage() {
		taskStage++;
		String newQuery = removeMultiPartStrings(pendingQuery);
		initiateSearch(newQuery);
	}
	
	protected boolean isMultiStageQuery( String query ) {
		
		for( String str : multiStageStrings ) {
			if( query.contains(str) )
					return true;
		}
				
		return false;
	}
	
	protected String removeMultiPartStrings( String query ) {
		
		String newQuery = null;
		
		for( String str : multiStageStrings ) {
			if( query.contains(str)) {
				newQuery = query.replace(str, "");
				break;
			}
		}
		
		return newQuery == null ? query : newQuery;
	}*/
	
	// **********************************************************

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
