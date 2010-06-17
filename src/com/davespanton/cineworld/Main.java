package com.davespanton.cineworld;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.davespanton.cineworld.activities.CinemaListActivity;
import com.davespanton.cineworld.activities.FilmListActivity;

import moz.http.HttpData;
import moz.http.HttpRequest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Main extends Activity {
    
	private static final int VIEW_CINEMAS = 0;
	private static final int VIEW_FILMS = 1;
	
	private static final int CINEMAS_RESULT = 0;
	private static final int FILMS_RESULT = 1;
	
	private TextView mMainText;
	
	private HttpData mCinemaData;
	private HttpData mFilmData;
	
	private JSONArray mCinema;
	private JSONArray mFilm;
	
	private ArrayList<String> mCinemaList;
	private ArrayList<String> mFilmList;
	
	private JSONObject mCurrentCinema;
	private JSONObject mCurrentFilm;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mMainText = (TextView) findViewById(R.id.main_text);
        
        //TODO move all this web-service stuff to a data provider sort of place.
        mCinemaData = HttpRequest.get( "http://www.cineworld.co.uk/api/quickbook/cinemas?key=" + ApiKey.KEY + "&full=true" );
        mFilmData = HttpRequest.get( "http://www.cineworld.co.uk/api/quickbook/films?key=" + ApiKey.KEY + "&full=true" );
        
        updateMainText();
        
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
			
			for( int i = 0; i < mFilm.length(); i++ ) {
				if(  mFilm.get(i) != null )
					mFilmList.add(((JSONObject) mFilm.get(i)).getString("title"));
			}
        }
        catch( JSONException e ) {
        	Log.e("CineWorld", "error in films jsonObject", e);
        }
        
        Log.d( "test", mCinemaData.content );
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
				startActivityForResult(i, CINEMAS_RESULT);
				return true;
			case VIEW_FILMS:
				i = new Intent( this, FilmListActivity.class);
				i.putStringArrayListExtra("data", mFilmList);
				startActivityForResult(i, FILMS_RESULT);
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		switch( requestCode ) {
			case CINEMAS_RESULT:
			try {
				mCurrentCinema = mCinema.getJSONObject(resultCode);
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
		}
		
	}

	private void updateMainText( ) {
		
		String cinema = "";
		String film = "";
		
		if( mCurrentCinema != null ) {
			try {
				cinema = "Current cinema is: " + mCurrentCinema.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			cinema = "You have not selected a cinema";
		
		if( mCurrentFilm != null ) {
			try {
				film = "Current film is: " + mCurrentFilm.getString("title");
			} catch (JSONException e) {
				// TODO Auto-generated catch block 
				e.printStackTrace();
			}
		}
		else
			film = "You have not selected a film";
		
		
		mMainText.setText( cinema + "\n" + film );
		
	}
    
}