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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Main extends Activity {
    
	private Handler mHandler = new Handler();
	
	private Runnable mResults = new Runnable() {
		public void run() {
			updateResults();
		}
	};
	
	private static final int VIEW_CINEMAS = 0;
	private static final int VIEW_FILMS = 1;
	
	private static final int CINEMAS_RESULT = 0;
	private static final int FILMS_RESULT = 1;
	private static final int CINEMA_FILMS_RESULT = 2;
	
	private TextView mMainText;
	private Button mCinemaButton;
	private Button mFilmButton;
	
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
	private volatile FilmList mPCinemaFilmList;
	
	private JSONObject mCurrentCinema;
	private JSONObject mCurrentFilm;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mMainText = (TextView) findViewById(R.id.main_text);
        mCinemaButton = (Button) findViewById(R.id.cinema_button);
        mFilmButton = (Button) findViewById(R.id.film_button);
        
        mCinemaButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		
        });
        
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
        					Film f = getFilmFromJSONObject(mFilm.getJSONObject(i));
        					if( f.validate()) {
        						mPFilmList.add( f );
        						mFilmList.add(((JSONObject) mFilm.get(i)).getString("title"));
        					}
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
		Bundle b = new Bundle();
		int request;
		if( mCurrentCinema == null || mCinemaFilmList == null ) {
			i.putStringArrayListExtra("data", mFilmList);
			
			b.putParcelable("films", mPFilmList);
			i.putExtra("films", b);
			
			request = FILMS_RESULT;
		}
		else {
			i.putStringArrayListExtra("data", mCinemaFilmList);
			
			b.putParcelable("films", mPCinemaFilmList);
			i.putExtra("films", b);
			
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
					mPCinemaFilmList = new FilmList();
					for( int i = 0; i < mCinemaFilm.length(); i++ ) {
        				if(  mCinemaFilm.get(i) != null )
        				{
        					Film f = getFilmFromJSONObject(mCinemaFilm.getJSONObject(i));
        					if( f.validate() ) {
        						mPCinemaFilmList.add( f );
        						mCinemaFilmList.add(((JSONObject) mCinemaFilm.get(i)).getString("title"));
        					}
        				}
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
	
	private Film getFilmFromJSONObject( JSONObject jsonObject ) {
		
		Film f = new Film();
		
		try {
			if( jsonObject.has("title") )
				f.setTitle( jsonObject.getString("title") );
			if( jsonObject.has("classification") )
				f.setRating( jsonObject.getString("classification") );
			if( jsonObject.has("advisory") )
				f.setAdvisory( jsonObject.getString("advisory") );
			if( jsonObject.has("poster_url") )
				f.setPosterUrl( jsonObject.getString("poster_url") );
			if( jsonObject.has("still_url") )
				f.setStillUrl( jsonObject.getString("still_url") );
			if( jsonObject.has("film_url") )
				f.setFilmUrl( jsonObject.getString("film_url") );
			if( jsonObject.has("edi") )
				f.setEdi( jsonObject.getString("edi") );
		}
		catch( JSONException error ) {
			//TODO something
		}
		
		return f;
	}
    
}