package com.davespanton.cineworld;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import moz.http.HttpData;
import moz.http.HttpRequest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Main extends Activity {
    
	private static final int VIEW_CINEMAS = 0;
	private static final int VIEW_FILMS = 1;
	
	private HttpData mCinemaData; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        HttpData mCinemaData = HttpRequest.get( "http://www.cineworld.co.uk/api/quickbook/cinemas?key=C93Xff2P" );
        
        JSONObject jsonObject = null;
        try {
			jsonObject = (JSONObject) new JSONTokener(mCinemaData.content).nextValue();
        }
        catch( JSONException e ) {
        	
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
				Intent i = new Intent(this, CinemaActivity.class);
				startActivity( i );
				return true;
			case VIEW_FILMS:
				//do something film activity related
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	
    
}