package com.davespanton.cineworld.services;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import moz.http.HttpData;
import moz.http.HttpRequest;

import com.davespanton.cineworld.ApiKey;
import com.davespanton.cineworld.data.Film;
import com.davespanton.cineworld.data.FilmList;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class CineWorldService extends Service {
	
	private final Binder binder = new LocalBinder();
	
	public enum Ids { FILM, CINEMA, CINEMA_FILM };
	
	private JSONArray mCinemas;
	private ArrayList<String> mCinemaData;
	private JSONArray mFilms;
	private ArrayList<String> mFilmData;
	private FilmList mPFilmData;
	private JSONArray mCinemaFilms;
	private ArrayList<String> mCinemaFilmData;
	private FilmList mPCinemaFilmData;
	
	private JSONObject mCurrentCinema;
	
	public JSONArray getCinemaList() {
		return mCinemas;
	}
	
	public ArrayList<String> getCinemaNames() {
		return mCinemaData;
	}
	
	public FilmList getFilmList() {
		return mPFilmData;
	}
	
	public ArrayList<String> getFilmNames() {
		return mFilmData;
	}
	
	public FilmList getFilmListForCurrentCinema() {
		return mPCinemaFilmData;
	}
	
	public ArrayList<String> getFilmNamesForCurrentCinema() {
		return mCinemaFilmData;
	}
	
	public JSONObject getCurrentCinema() {
		return mCurrentCinema;
	}
	
	public void setCurrentCinema( JSONObject cinema ) {
		if( cinema == mCurrentCinema )
			return;
		
		mCurrentCinema = cinema;
		
		updateFilmsForCinema();
	}
	
	@Override
	public void onCreate() {
		
		super.onCreate();
		
		FetchDataTask cinemaData = new FetchDataTask();
		cinemaData.id = Ids.CINEMA;
		cinemaData.execute( "http://www.cineworld.co.uk/api/quickbook/cinemas?key=" + ApiKey.KEY + "&full=true" );
		
		FetchDataTask filmData = new FetchDataTask();
		filmData.id = Ids.FILM;
		filmData.execute( "http://www.cineworld.co.uk/api/quickbook/films?key=" + ApiKey.KEY + "&full=true" );
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();
		
		// TODO cleanup
	}

	@Override
	public IBinder onBind(Intent arg0) {
		
		return binder;
	}
	
	public void processResult( Ids id, HttpData result ) {
		
		switch( id )
		{
			case CINEMA:
				JSONObject jsonObject = null;
                
				try {
        			jsonObject = (JSONObject) new JSONTokener(result.content).nextValue();
        			mCinemas = jsonObject.getJSONArray("cinemas");
        			mCinemaData = new ArrayList<String>();
        			
        			for( int i = 0; i < mCinemas.length(); i++ ) {
        				if(  mCinemas.get(i) != null )
        					mCinemaData.add(((JSONObject) mCinemas.get(i)).getString("name"));
        			}
        		}
                catch( JSONException e ) {
                	Log.e("CineWorld", "error in cinema jsonObject", e);
                }
				break;
				
			case FILM:
				try {
                	
        			jsonObject = (JSONObject) new JSONTokener(result.content).nextValue();
        			mFilms = jsonObject.getJSONArray("films");
        			mFilmData = new ArrayList<String>();
        			mPFilmData = new FilmList();
        			
        			for( int i = 0; i < mFilms.length(); i++ ) {
        				if(  mFilms.get(i) != null )
        				{
        					Film f = getFilmFromJSONObject(mFilms.getJSONObject(i));
        					if( f.validate()) {
        						mPFilmData.add( f );
        						mFilmData.add(((JSONObject) mFilms.get(i)).getString("title"));
        					}
        				}
        			}
        		}
                catch( JSONException e ) {
                	Log.e("CineWorld", "error in films jsonObject", e);
                }
				break;
			case CINEMA_FILM:
				
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					mCinemaFilms = obj.getJSONArray("films");
					mCinemaFilmData = new ArrayList<String>();
					mPCinemaFilmData = new FilmList();
					for( int i = 0; i < mCinemaFilms.length(); i++ ) {
        				if(  mCinemaFilms.get(i) != null )
        				{
        					Film f = getFilmFromJSONObject(mCinemaFilms.getJSONObject(i));
        					if( f.validate() ) {
        						mPCinemaFilmData.add( f );
        						mCinemaFilmData.add(((JSONObject) mCinemaFilms.get(i)).getString("title"));
        					}
        				}
        			}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
		}
		
		Log.d("CineWorldService", "processed " + id.toString());
		
	}
	
	private void updateFilmsForCinema() {
		String id = "";
		try {
			id = mCurrentCinema.getString("id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FetchDataTask fdt = new FetchDataTask();
		fdt.id = Ids.CINEMA_FILM;
		fdt.execute( "https://www.cineworld.co.uk/api/quickbook/films?key=" + ApiKey.KEY + "&full=true&cinema=" + id );
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
			//TODO Film object gets validated above, but prob be good to double check it here.
		}
		
		return f;
	}

	public class LocalBinder extends Binder {
		public CineWorldService getService() {
			return(CineWorldService.this);
		}
	}
	
	class FetchDataTask extends AsyncTask<String, Void, HttpData> {

		public Ids id;
		
		@Override
		protected HttpData doInBackground(String... url) {

			HttpData data = HttpRequest.get( url[0] );
			
			return data;
		}

		@Override
		protected void onPostExecute(HttpData result) {
			
			super.onPostExecute(result);
			
			processResult( id, result );
		}
		
	}
}


