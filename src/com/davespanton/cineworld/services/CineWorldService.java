package com.davespanton.cineworld.services;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import moz.http.HttpData;
import moz.http.HttpRequest;

import com.davespanton.cineworld.ApiKey;
import com.davespanton.cineworld.data.Cinema;
import com.davespanton.cineworld.data.CinemaList;
import com.davespanton.cineworld.data.Film;
import com.davespanton.cineworld.data.FilmList;
import com.davespanton.cineworld.data.Performance;
import com.davespanton.cineworld.data.PerformanceList;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;

public class CineWorldService extends Service {
	
	public static final String CINEWORLD_DATA_LOADED = "com.davespanton.cineworld.services.CineWorldUpdateEvent";
	
	private static final Logger mog = LoggerFactory.getLogger(CineWorldService.class);
	
	private final Binder binder = new LocalBinder();
	
	public enum Ids { FILM, CINEMA, CINEMA_FILM, FILM_DATES, DATE_TIMES };
	
	// Cinema data
	private JSONArray mCinemas;
	private ArrayList<String> mCinemaData;
	private CinemaList mPCinemaData;
	
	
	// Films data
	
	private JSONArray mFilms;
	private ArrayList<String> mFilmData;
	private FilmList mPFilmData;
	
	// Films for selected cinema data.
	
	private JSONArray mCinemaFilms;
	private ArrayList<String> mCinemaFilmData;
	private FilmList mPCinemaFilmData;
	
	// Film date data
	
	private JSONArray mFilmDates;
	private ArrayList<String> mFilmDatesData = null;
	
	// Performance data
	
	private JSONArray mFilmPerformance;
	private PerformanceList mPFilmPerformanceData;
	
	// Current selection data
	
	private Cinema mCurrentCinema;
	private Film mCurrentFilm;
	
	// Flags indicating if Cinema and Film data are loaded. 
	
	private boolean cinemaDataReady = false;
	private boolean filmDataReady = false;
	
	public CinemaList getCinemaList() {
		return mPCinemaData;
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
	
	public ArrayList<String> getDatesForCurrentFilm() {
		return mFilmDatesData;
	}
	
	//TODO return a token from this request?
	public boolean requestPerformancesForCurrentFilm( String date ) {
		if( mCurrentFilm != null ) {
			updatePerformancesForFilm(date);
			return true;
		}
		
		return false;
	}
	
	public boolean getCinemaDataReady() {
		return cinemaDataReady;
	}
	
	public boolean getFilmDataReady() {
		return filmDataReady;
	}
	
	public Cinema getCurrentCinema() {
		return mCurrentCinema;
	}
	
	public void setCurrentCinema( int index ) {
		
		if( mPCinemaData.get(index) == mCurrentCinema ) {
			broadcastDataLoaded( Ids.CINEMA_FILM, null );
			return;
		}
		else if( index > (mPCinemaData.size()-1) ) {
			return;
		}
		
		mCurrentCinema = mPCinemaData.get(index);
		
		updateFilmsForCinema();
	}
	
	public void clearCurrentCinema() {
		mCurrentCinema = null;
	}
	
	public Film getCurrentFilm() {
		return mCurrentFilm;
	}
	
	
	public void setCurrentFilm( int index, boolean all ) {
		
		FilmList source = all ? mPFilmData : mPCinemaFilmData;
		
		if( source.get(index) == mCurrentFilm )
			return;
		else if( index > (source.size()-1))
			return;
		
		mCurrentFilm = source.get(index);
		
		//unused at the moment - using a date picker instead of constantly polling for availble dates
		//updateDatesForFilm();
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
	
	protected void processResult( Ids id, HttpData result ) {
		
		Parcelable extraData = null;
		
		switch( id )
		{
			case CINEMA:
				JSONObject jsonObject = null;
                
				try {
        			jsonObject = (JSONObject) new JSONTokener(result.content).nextValue();
        			mCinemas = jsonObject.getJSONArray("cinemas");
        			mCinemaData = new ArrayList<String>();
        			mPCinemaData = new CinemaList();
        			
        			for( int i = 0; i < mCinemas.length(); i++ ) {
        				if(  mCinemas.getJSONObject(i) != null ) {
        					Cinema c = getCinemaFromJSONObject(mCinemas.getJSONObject(i));
        					mPCinemaData.add(c);
        					mCinemaData.add(((JSONObject) mCinemas.get(i)).getString("name"));
        				}
        			}
        		}
                catch( JSONException e ) {
                	e.printStackTrace();
					mog.error(e.getMessage());
                }
                cinemaDataReady = true;
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
					e.printStackTrace();
					mog.error(e.getMessage());
                }
				filmDataReady = true;
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
					e.printStackTrace();
					mog.error(e.getMessage());
				}
				break;
				
			case FILM_DATES:
				
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					mFilmDates = obj.getJSONArray("dates");
					mFilmDatesData = new ArrayList<String>();
					for( int i = 0; i < mFilmDates.length(); i++ ) {
						mFilmDatesData.add( mFilmDates.getString(i) );
					}
				} catch (JSONException e) {
					e.printStackTrace();
					mog.error(e.getMessage());
				}
				
				break;
				
			case DATE_TIMES:
				
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					mFilmPerformance = obj.getJSONArray("performances");
					mPFilmPerformanceData = new PerformanceList();
					for( int i = 0; i < mFilmPerformance.length(); i++ ) {
						if( mFilmPerformance.getJSONObject(i) != null ) {
							Performance p = getPerformanceFromJSONObject( mFilmPerformance.getJSONObject(i) );
							mPFilmPerformanceData.add(p);
						}
					}
					
					extraData = mPFilmPerformanceData;
				} catch (JSONException e) {
					e.printStackTrace();
					mog.error(e.getMessage());
				}
				
				break;
				
		}
		
		broadcastDataLoaded(id, extraData);
	}
	
	protected void broadcastDataLoaded( Ids id, Parcelable data ) {
		Intent i = new Intent( CINEWORLD_DATA_LOADED );
		i.putExtra("id", id );
		
		if( data != null ) {
			i.putExtra( "data", data);
		}
		
		sendBroadcast( i );
	}
	
	private void updateFilmsForCinema() {
		String id = mCurrentCinema.getId();
		
		FetchDataTask fdt = new FetchDataTask();
		fdt.id = Ids.CINEMA_FILM;
		fdt.execute( "https://www.cineworld.co.uk/api/quickbook/films?key=" + ApiKey.KEY + "&full=true&cinema=" + id );
		mog.debug( "https://www.cineworld.co.uk/api/quickbook/films?key=" + ApiKey.KEY + "&full=true&cinema=" + id );
	}
	
	/*private void updateDatesForFilm() {
		if( mCurrentCinema == null || mCurrentFilm == null )
			return;
		
		mFilmDates = null;
		mFilmDatesData = null;
		
		String cinemaId = mCurrentCinema.getId();
		String filmId = mCurrentFilm.getEdi();
		
		FetchDataTask fdt = new FetchDataTask();
		fdt.id = Ids.FILM_DATES;
		fdt.execute( "https://www.cineworld.co.uk/api/quickbook/dates?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId);
		//Log.d( "Cineworld Request", "https://www.cineworld.co.uk/api/quickbook/dates?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId);
	}*/
	
	private void updatePerformancesForFilm( String date ) {
		if( mCurrentCinema == null || mCurrentFilm == null )
			return;
		
		mFilmPerformance = null;
		mPFilmPerformanceData = null;
		
		String cinemaId = mCurrentCinema.getId();
		String filmId = mCurrentFilm.getEdi();
		
		FetchDataTask fdt = new FetchDataTask();
		fdt.id = Ids.DATE_TIMES;
		fdt.execute( "https://www.cineworld.co.uk/api/quickbook/performances?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId + "&date=" + date);
		mog.debug( "https://www.cineworld.co.uk/api/quickbook/performances?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId + "&date=" + date);
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
	
	private Cinema getCinemaFromJSONObject( JSONObject jsonObject ) {
		Cinema c = new Cinema();
		
		try {
			if( jsonObject.has("name"))
				c.setName( jsonObject.getString("name") );
			if( jsonObject.has("address"))
				c.setAddress( jsonObject.getString("address") );
			if( jsonObject.has("postcode"))
				c.setPostcode( jsonObject.getString("postcode") );
			if( jsonObject.has("telephone"))
				c.setTelephone( jsonObject.getString("telephone") );
			if( jsonObject.has("id"))
				c.setId( jsonObject.getString("id") );
		}
		catch( JSONException error ) {
			//TODO more validation
			error.printStackTrace();
		}
		
		return c;
	}
	
	private Performance getPerformanceFromJSONObject( JSONObject jsonObject ) {
		Performance p = new Performance();
		
		try {
			if( jsonObject.has("time"))
				p.setTime( jsonObject.getString("time") );
			if( jsonObject.has("available"))
				p.setAvailable( jsonObject.getBoolean("available") );
			if( jsonObject.has("type"))
				p.setType( jsonObject.getString("type"));
			if( jsonObject.has("ad"))
				p.setAd( jsonObject.getBoolean("ad"));
			if( jsonObject.has("subtitled"))
				p.setSubtitled( jsonObject.getBoolean("subtitled"));
			if( jsonObject.has("booking_url"))
				p.setBookingUrl( jsonObject.getString("booking_url"));
		}
		catch( JSONException error ) {
			//TODO more validation
			error.printStackTrace();
		}
			
		return p;
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


