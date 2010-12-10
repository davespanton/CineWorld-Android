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
	public static final String CINEWORLD_ERROR = "com.davespanton.cineworld.services.CineWorldErrorEvent";
	
	private static final Logger mog = LoggerFactory.getLogger(CineWorldService.class);
	
	private final Binder binder = new LocalBinder();
	
	public enum Ids { FILM, CINEMA, CINEMA_FILM, FILM_DATES, DATE_TIMES };
	
	// Cinema data
	//private ArrayList<String> mCinemaData;
	private CinemaList mPCinemaData;
		
	// Films data
	private FilmList mPFilmData;
	
	// Films for selected cinema data.
	private FilmList mPCinemaFilmData;
	
	// Film date data
	private JSONArray mFilmDates;
	private ArrayList<String> mFilmDatesData = null;
	
	// Performance data
	private PerformanceList mPFilmPerformanceData;
	
	// Current selection data
	
	private Cinema mCurrentCinema;
	private Film mCurrentFilm;
	
	// Flags indicating if Cinema and Film data are loaded. 
	
	private boolean cinemaDataReady = false;
	private boolean filmDataReady = false;
	
	public void requestCinemaList() {
		if( cinemaDataReady ) {
			broadcastDataLoaded(Ids.CINEMA, mPCinemaData);
		}
		//else
			//TODO	request for cinema data or wait. 
	}
		
	public void requestFilmList() {
		if( filmDataReady ) {
			broadcastDataLoaded(Ids.FILM, mPFilmData);
		}
		//else
			//TODO	request for film data or wait.
	}
		
	public void requestFilmListForCinema( String id ) {
		//TODO implement a hashmap of films-for-cinemas, check it and either broadcast data or request it
	}
		
	//TODO return a token from this request?
	public void requestPerformancesForFilmCinema( String date, String cinemaId, String filmId ) {
		//TODO	implement a hashmap of performances-for-filmcinema data, check it and broadcast data or request it
		
		updatePerformancesForFilm(date, cinemaId, filmId);
	}
	
	public boolean getCinemaDataReady() {
		return cinemaDataReady;
	}
	
	public boolean getFilmDataReady() {
		return filmDataReady;
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
		
		// TODO some cleanup
	}

	@Override
	public IBinder onBind(Intent arg0) {
		
		return binder;
	}
	
	protected void processResult( Ids id, HttpData result ) {
		
		Parcelable extraData = null;
		boolean error = false;;
		
		switch( id )
		{
			case CINEMA:
				JSONObject jsonObject = null;
                
				try {
        			jsonObject = (JSONObject) new JSONTokener(result.content).nextValue();
        			JSONArray cinemas = jsonObject.getJSONArray("cinemas");
        			mPCinemaData = new CinemaList();
        			
        			for( int i = 0; i < cinemas.length(); i++ ) {
        				if(  cinemas.getJSONObject(i) != null ) {
        					Cinema c = getCinemaFromJSONObject(cinemas.getJSONObject(i));
        					mPCinemaData.add(c);
        				}
        			}
        			extraData = mPCinemaData;
        		} catch( JSONException e ) {
                	e.printStackTrace();
                	mog.error( "JSONException for CINEMA. " + result.content );
                	error = true;
                } catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
				}
                
                if( !error )
                	cinemaDataReady = true;
                
				break;
				
			case FILM:
				try {
                	jsonObject = (JSONObject) new JSONTokener(result.content).nextValue();
        			JSONArray films = jsonObject.getJSONArray("films");
        			mPFilmData = new FilmList();
        			
        			for( int i = 0; i < films.length(); i++ ) {
        				if(  films.get(i) != null )
        				{
        					Film f = getFilmFromJSONObject(films.getJSONObject(i));
        					if( f.validate()) {
        						mPFilmData.add( f );
        					}
        				}
        			}
        			
        			extraData = mPFilmData;
        		} catch( JSONException e ) {
					e.printStackTrace();
					mog.error( "JSONException for FILM. " + result.content );
					error = true;
                } catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
				}
		
				if( !error )
					filmDataReady = true;
				
				break;
				
			case CINEMA_FILM:
				
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					JSONArray cinemaFilms = obj.getJSONArray("films");
					mPCinemaFilmData = new FilmList();
					for( int i = 0; i < cinemaFilms.length(); i++ ) {
        				if(  cinemaFilms.get(i) != null )
        				{
        					Film f = getFilmFromJSONObject(cinemaFilms.getJSONObject(i));
        					if( f.validate() ) {
        						
        						mPCinemaFilmData.add( f );
        					}
        				}
        			}
					extraData = mPCinemaFilmData;
				} catch (JSONException e) {
					e.printStackTrace();
					mog.error( "JSONException for CINEMA_FILM. " + result.content );
					error = true;
				} catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
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
					error = true;
				}
				
				break;
				
			case DATE_TIMES:
				
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					JSONArray filmPerformance = obj.getJSONArray("performances");
					mPFilmPerformanceData = new PerformanceList();
					for( int i = 0; i < filmPerformance.length(); i++ ) {
						if( filmPerformance.getJSONObject(i) != null ) {
							Performance p = getPerformanceFromJSONObject( filmPerformance.getJSONObject(i) );
							mPFilmPerformanceData.add(p);
						}
					}
					
					extraData = mPFilmPerformanceData;
				} catch (JSONException e) {
					e.printStackTrace();
					mog.error( "JSONException for DATE_TIMES. " + result.content );
					error = true;
				} catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
				}
				
				
				break;
				
		}
		
		if( !error ) 
			broadcastDataLoaded(id, extraData);
		else {
			Intent i = new Intent( CINEWORLD_ERROR );
			i.putExtra("id", id);
			sendBroadcast( i );
		}
			
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
	
	private void updatePerformancesForFilm( String date, String cinemaId, String filmId ) {
		if( mCurrentCinema == null || mCurrentFilm == null )
			return;
		
		mPFilmPerformanceData = null;
		
		//String cinemaId = mCurrentCinema.getId();
		//String filmId = mCurrentFilm.getEdi();
		
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


