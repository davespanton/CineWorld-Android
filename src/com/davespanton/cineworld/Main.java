package com.davespanton.cineworld;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.davespanton.cineworld.activities.CinemaListActivity;
import com.davespanton.cineworld.activities.FilmListActivity;
import com.davespanton.cineworld.data.Film;
import com.davespanton.cineworld.data.FilmList;

import moz.http.HttpData;
import moz.http.HttpRequest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Main extends Activity {
    
	private Handler mHandler = new Handler();
	
	private Runnable mResults = new Runnable() {
		public void run() {
			Log.d("RUNNABLE", "run");
			updateResults();
		}
	};
	
	private static final int VIEW_CINEMAS = 0;
	private static final int VIEW_FILMS = 1;
	
	private static final int CINEMAS_RESULT = 0;
	private static final int FILMS_RESULT = 1;
	private static final int CINEMA_FILMS_RESULT = 2;
	
	private TextView mMainText;
	
	private volatile HttpData mCinemaData;
	private volatile HttpData mFilmData;
	private volatile HttpData mCinemaFilmData;
	
	private volatile JSONArray mCinema;
	private volatile JSONArray mFilm;
	private volatile JSONArray mCinemaFilm;
	
	private volatile ArrayList<String> mCinemaList;
	private volatile ArrayList<String> mFilmList;
	private volatile ArrayList<String> mCinemaFilmList;
	
	private volatile FilmList mPFilmList;
	
	private JSONObject mCurrentCinema;
	private JSONObject mCurrentFilm;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mMainText = (TextView) findViewById(R.id.main_text);
        
        //TODO move all this web-service stuff to a data provider sort of place.
        Thread requestThread = new Thread() {
        	
			public void run() {
        		mCinemaData = HttpRequest.get( "http://www.cineworld.co.uk/api/quickbook/cinemas?key=" + ApiKey.KEY + "&full=true" );
                mFilmData = HttpRequest.get( "http://www.cineworld.co.uk/api/quickbook/films?key=" + ApiKey.KEY + "&full=true" );
        		
                JSONObject jsonObject = null;
                try {
        			jsonObject = (JSONObject) new JSONTokener(mCinemaData.content).nextValue();
        			mCinema = jsonObject.getJSONArray("cinemas");
        			mCinemaList = new ArrayList<String>();
        			
        			for( int i = 0; i < mCinema.length(); i++ ) {
        				if(  mCinema.get(i) != null )
        					mCinemaList.add(((JSONObject) mCinema.get(i)).getString("name"));
        			}
        		}
                catch( JSONException e ) {
                	Log.e("CineWorld", "error in cinema jsonObject", e);
                }
                
                jsonObject = null;
                try {
                	
        			jsonObject = (JSONObject) new JSONTokener(mFilmData.content).nextValue();
        			mFilm = jsonObject.getJSONArray("films");
        			mFilmList = new ArrayList<String>();
        			mPFilmList = new FilmList();
        			
        			for( int i = 0; i < mFilm.length(); i++ ) {
        				if(  mFilm.get(i) != null )
        				{
        					mFilmList.add(((JSONObject) mFilm.get(i)).getString("title"));
        					
        					//TODO Validate each film item, and move this somewhere else
        					Film f = new Film();
        					
        					if( ((JSONObject) mFilm.get(i)).has("title") )
        						f.setTitle( ((JSONObject) mFilm.get(i)).getString("title") );
        					if( ((JSONObject) mFilm.get(i)).has("classification") )
        						f.setRating( ((JSONObject) mFilm.get(i)).getString("classification") );
        					if( ((JSONObject) mFilm.get(i)).has("advisory") )
        						f.setAdvisory( ((JSONObject) mFilm.get(i)).getString("advisory") );
        					if( ((JSONObject) mFilm.get(i)).has("poster_url") )
        						f.setPosterUrl( ((JSONObject) mFilm.get(i)).getString("poster_url") );
        					if( ((JSONObject) mFilm.get(i)).has("still_url") )
        						f.setStillUrl( ((JSONObject) mFilm.get(i)).getString("still_url") );
        					if( ((JSONObject) mFilm.get(i)).has("film_url") )
        						f.setFilmUrl( ((JSONObject) mFilm.get(i)).getString("film_url") );
        					if( ((JSONObject) mFilm.get(i)).has("edi") )
        						f.setEdi( ((JSONObject) mFilm.get(i)).getString("edi") );
        					
        					mPFilmList.add(f);
        				}
        			}
        		    				
                }
                catch( JSONException e ) {
                	Log.e("CineWorld", "error in films jsonObject", e);
                }
                              
                mHandler.post(mResults);
        	}
        };
        updateMainText();
        requestThread.start();
        
       // Log.d( "test", mCinemaData.content );
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
				Intent i = new Intent(this, CinemaListActivity.class);
				i.putStringArrayListExtra("data", mCinemaList);
				i.putExtra( "raw", mCinemaData.content);
				
				startActivityForResult(i, CINEMAS_RESULT);
				return true;
			case VIEW_FILMS:
				startFilmActivity();
				return true;

			
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void startFilmActivity() {
		Intent i = new Intent( this, FilmListActivity.class);
		int request;
		if( mCurrentCinema == null || mCinemaFilmList == null ) {
			i.putStringArrayListExtra("data", mFilmList);
			i.putExtra("raw", mFilmData.content);
			
			Bundle b = new Bundle();
			b.putParcelable("films", mPFilmList);
			
			i.putExtra("films", b);
			
			request = FILMS_RESULT;
		}
		else {
			i.putStringArrayListExtra("data", mCinemaFilmList);
			i.putExtra("raw", mCinemaFilmData.content);
			request = CINEMA_FILMS_RESULT;
		}
		
		startActivityForResult(i, request);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		if( resultCode == -1 )
			return;
		
		switch( requestCode ) {
			case CINEMAS_RESULT:
			try {
				mCurrentCinema = mCinema.getJSONObject(resultCode);
				updateFilmsForCinema( );
				updateMainText();
			} catch (JSONException e) {
				e.printStackTrace();
			}	
				break;
			case FILMS_RESULT:
			try {
				mCurrentFilm = mFilm.getJSONObject(resultCode);
				updateMainText();
			} catch (JSONException e) {
				e.printStackTrace();
			}
				break; 
			case CINEMA_FILMS_RESULT:
			try{
				mCurrentFilm = mCinemaFilm.getJSONObject(resultCode);
				updateMainText();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
		
	}
	
	private void updateFilmsForCinema( ) {
		
		Thread thread = new Thread() {
			public void run() {
				String id = "";
				try {
					id = mCurrentCinema.getString("id");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				mCinemaFilmData = HttpRequest.get("https://www.cineworld.co.uk/api/quickbook/films?key=" + ApiKey.KEY + "&full=true&cinema=" + id);
			
				try {
					JSONObject obj = (JSONObject) new JSONTokener(mCinemaFilmData.content).nextValue();
					mCinemaFilm = obj.getJSONArray("films");
					mCinemaFilmList = new ArrayList<String>();
					
					for( int i = 0; i < mCinemaFilm.length(); i++ ) {
        				if(  mCinemaFilm.get(i) != null )
        					mCinemaFilmList.add(((JSONObject) mCinemaFilm.get(i)).getString("title"));
        			}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		thread.start();
		
	}

	private void updateResults() {
		
		updateMainText();
		
	}

	private void updateMainText( ) {
		
		String cinema = "";
		String film = "";
		
		if( mCurrentCinema != null ) {
			try {
				cinema = getString(R.string.current_cinema) + mCurrentCinema.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			cinema = getString(R.string.no_current_cinema);
		
		if( mCurrentFilm != null ) {
			try {
				film = getString(R.string.current_film) + mCurrentFilm.getString("title");
			} catch (JSONException e) {
				// TODO Auto-generated catch block 
				e.printStackTrace();
			}
		}
		else
			film = getString(R.string.no_current_film);
		
		
		mMainText.setText( cinema + "\n" + film );
		
	}
    
}