package com.davespanton.cineworld.services;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException; 
import org.json.JSONObject;
import org.json.JSONTokener;

import moz.http.HttpData;
import moz.http.HttpRequest;

import com.davespanton.cineworld.ApiKey;
import com.davespanton.cineworld.data.CineVO;
import com.davespanton.cineworld.data.Cinema;
import com.davespanton.cineworld.data.CinemaList;
import com.davespanton.cineworld.data.Film;
import com.davespanton.cineworld.data.FilmList;
import com.davespanton.cineworld.data.MultiPerformanceList;
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
		
	private static final String BASE_URL = "https://www.cineworld.co.uk/api/quickbook/";
	
	private static final int DATE_RANGE = 7;
	
	private static final Logger mog = LoggerFactory.getLogger(CineWorldService.class);
	
	private final Binder binder = new LocalBinder();
	
	public enum Ids { FILM, CINEMA, CINEMA_FILM, FILM_DATES, DATE_TIMES, WEEK_TIMES };
	
	// Cinema data
	private CinemaList mPCinemaData;
		
	// Films data
	private FilmList mPFilmData;
	
	// Films for selected cinema data.
	private HashMap<String, FilmList> mCinemaFilmData = new HashMap<String, FilmList>();
	
	// Film date data
	private JSONArray mFilmDates;
	
	private HashMap<String, ArrayList<String>> mFilmDatesData = new HashMap<String, ArrayList<String>>(); 
			
	// Performance lists for film-cinema combinations. 
	private HashMap<String, PerformanceList> mPerformanceData = new HashMap<String, PerformanceList>();
	
	private HashMap<String, MultiPerformanceList> mMultiPerformanceData = new HashMap<String, MultiPerformanceList>();
	
	// Flags indicating if Cinema and Film data are loaded. 
	private boolean cinemaDataReady = false;
	private boolean filmDataReady = false;
	
	public void requestCinemaList() {
		if( cinemaDataReady ) {
			Intent i = new Intent( CINEWORLD_DATA_LOADED );
			i.putExtra("id", Ids.CINEMA);
			i.putExtra("data", (Parcelable) mPCinemaData);
			sendBroadcast(i);
		} 
	}
		
	public void requestFilmList() {
		if( filmDataReady ) {
			Intent i = new Intent( CINEWORLD_DATA_LOADED );
			i.putExtra("id", Ids.FILM);
			i.putExtra("data", (Parcelable) mPFilmData);
			sendBroadcast(i);
		}
	}
	
	public void requestFilmListForCinema( String id ) {
		if( mCinemaFilmData.containsKey(id) ) {
			Intent i = new Intent( CINEWORLD_DATA_LOADED );
			i.putExtra("id", Ids.CINEMA_FILM);
			i.putExtra("data", (Parcelable) mCinemaFilmData.get(id));
			sendBroadcast(i);
		}
		else {
			FetchDataTask fdt = new FetchDataTask();
			fdt.id = Ids.CINEMA_FILM;
			fdt.data = id;
			fdt.dataVO = new CineVO(id);
			fdt.execute( BASE_URL + "films?key=" + ApiKey.KEY + "&full=true&cinema=" + id );
			mog.debug( BASE_URL + "films?key=" + ApiKey.KEY + "&full=true&cinema=" + id );
		}
	}
	
	public void requestPerformancesForFilmCinema( String cinemaId, String filmId )	{
		
		String data = cinemaId + filmId;
		
		if( mMultiPerformanceData.containsKey(data) ) {
			if( mMultiPerformanceData.get(data).isComplete() ) {
				Intent i = new Intent(CINEWORLD_DATA_LOADED);
				i.putExtra("id", Ids.WEEK_TIMES);
				i.putExtra("data", (Serializable) mMultiPerformanceData.get(data));
				sendBroadcast(i);
			}
			// else request is outstanding... so wait
		}
		else {
			Calendar target = (Calendar) Calendar.getInstance();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			mMultiPerformanceData.put(data, new MultiPerformanceList(data, DATE_RANGE));
			for( int i = 0; i < DATE_RANGE; i++ ) {
				String date = dateFormat.format(target.getTime());
				
				FetchDataTask fdt = new FetchDataTask();
				fdt.id = Ids.WEEK_TIMES;
				fdt.data = date + "," + cinemaId + "," + filmId;
				fdt.dataVO = new CineVO(cinemaId, filmId, date);
				fdt.execute( BASE_URL + "performances?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId + "&date=" + date);
				
				target.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
	}
		
	public void requestPerformancesForFilmCinema( String date, String cinemaId, String filmId ) {
		String data = date + cinemaId + filmId;
		
		if( mPerformanceData.containsKey(data)) {
			Intent i = new Intent(CINEWORLD_DATA_LOADED);
			i.putExtra("id", Ids.DATE_TIMES);
			i.putExtra("data", (Parcelable) mPerformanceData.get(data));
			sendBroadcast(i);
		}
		else {
			FetchDataTask fdt = new FetchDataTask();
			fdt.id = Ids.DATE_TIMES;
			fdt.data = data;
			fdt.dataVO = new CineVO(cinemaId, filmId, date);
			fdt.execute( BASE_URL + "performances?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId + "&date=" + date);
			mog.debug( BASE_URL + "performances?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId + "&date=" + date);
		}
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
	}

	@Override
	public IBinder onBind(Intent arg0) {
		
		return binder;
	}
	
	protected void processResult( FetchDataTask fetch, HttpData result ) {
		
		Intent intent = new Intent( CINEWORLD_DATA_LOADED );
		boolean error = false;
		
		switch( fetch.id )
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
        		} catch( JSONException e ) {
                	e.printStackTrace();
                	mog.error( "JSONException for CINEMA. " + result.content );
                	error = true;
                } catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
				}
                
                if( !error ) {
                	cinemaDataReady = true;
                	intent.putExtra("data", (Parcelable) mPCinemaData);
                }
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
        		} catch( JSONException e ) {
					e.printStackTrace();
					mog.error( "JSONException for FILM. " + result.content );
					error = true;
                } catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
				}
		
				if( !error ) {
					intent.putExtra("data", (Parcelable) mPFilmData);
					filmDataReady = true;
				}
				break;
				
			case CINEMA_FILM:
				
				FilmList cinemaFilmData = new FilmList();
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					JSONArray cinemaFilms = obj.getJSONArray("films");
					for( int i = 0; i < cinemaFilms.length(); i++ ) {
        				if(  cinemaFilms.get(i) != null )
        				{
        					Film f = getFilmFromJSONObject(cinemaFilms.getJSONObject(i));
        					if( f.validate() ) {
        						cinemaFilmData.add( f );
        					}
        				}
        			}
					
				} catch (JSONException e) {
					e.printStackTrace();
					mog.error( "JSONException for CINEMA_FILM. " + result.content );
					error = true;
				} catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
				}
				
				if( !error ) {
					intent.putExtra("data", (Parcelable) cinemaFilmData);
					mCinemaFilmData.put(fetch.data.toString(), cinemaFilmData);
				}
				
				break;
				
			case FILM_DATES:
				ArrayList<String> filmDates = new ArrayList<String>();
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					mFilmDates = obj.getJSONArray("dates");
					
					for( int i = 0; i < mFilmDates.length(); i++ ) {
						filmDates.add( mFilmDates.getString(i) );
					}
				} catch (JSONException e) {
					e.printStackTrace();
					mog.error(e.getMessage());
					error = true;
				}
				
				if( !error ) {
					intent.putStringArrayListExtra("data", filmDates);
					mFilmDatesData.put(fetch.data.toString(), filmDates);
				}
				
				break;
				
			case DATE_TIMES:
				PerformanceList filmPerformanceData = new PerformanceList( fetch.getDataVO().getDate() );
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					JSONArray filmPerformance = obj.getJSONArray("performances");
					
					for( int i = 0; i < filmPerformance.length(); i++ ) {
						if( filmPerformance.getJSONObject(i) != null ) {
							Performance p = getPerformanceFromJSONObject( filmPerformance.getJSONObject(i) );
							filmPerformanceData.add(p);
						}
					}
					
					
				} catch (JSONException e) {
					e.printStackTrace();
					mog.error( "JSONException for DATE_TIMES. " + result.content );
					error = true;
				} catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
				}
				
				if( !error ) {
					intent.putExtra("data", (Parcelable) filmPerformanceData);
					mPerformanceData.put(fetch.data, filmPerformanceData);
				}
				
				break;
			
			case WEEK_TIMES:
				
				filmPerformanceData = new PerformanceList( fetch.getDataVO().getDate() );
				try {
					JSONObject obj = (JSONObject) new JSONTokener(result.content).nextValue();
					JSONArray filmPerformance = obj.getJSONArray("performances");
					
					for( int i = 0; i < filmPerformance.length(); i++ ) {
						if( filmPerformance.getJSONObject(i) != null ) {
							Performance p = getPerformanceFromJSONObject( filmPerformance.getJSONObject(i) );
							filmPerformanceData.add(p);
						}
					}
				//TODO	iron out error handling here...
				} catch (JSONException e) {
					e.printStackTrace();
					mog.error( "JSONException for DATE_TIMES. " + result.content );
					error = true;
				} catch (NullPointerException e) {
					mog.error( "NullPointer in CineworldService." + result.content);
					error = true;
				}
				
				String[] idData = fetch.data.toString().split(",");
				String parentId = idData[1] + idData[2];
				MultiPerformanceList multiPerformanceList = mMultiPerformanceData.get(parentId);
				if( error || filmPerformanceData.size() == 0 )
					multiPerformanceList.add(null);
				else
					multiPerformanceList.add(filmPerformanceData);
				
				if( mMultiPerformanceData.get(parentId).isComplete() )
					intent.putExtra("data", (Parcelable) multiPerformanceList); 
				else
					return;
				
				break;
		}
		
		if( !error ) {
			intent.putExtra("id", fetch.id);
			sendBroadcast(intent);
		}
		else {
			Intent i = new Intent( CINEWORLD_ERROR );
			i.putExtra("id", fetch.id);
			sendBroadcast( i );
		}
			
	}
	
	/*private void updateFilmsForCinema() {
		String id = mCurrentCinema.getId();
		
		FetchDataTask fdt = new FetchDataTask();
		fdt.id = Ids.CINEMA_FILM;
		fdt.execute( BASE_URL + "films?key=" + ApiKey.KEY + "&full=true&cinema=" + id );
		mog.debug( BASE_URL + "films?key=" + ApiKey.KEY + "&full=true&cinema=" + id );
	}*/
	
	/*private void updateDatesForFilm() {
		if( mCurrentCinema == null || mCurrentFilm == null )
			return;
		
		mFilmDates = null;
		mFilmDatesData = null;
		
		String cinemaId = mCurrentCinema.getId();
		String filmId = mCurrentFilm.getEdi();
		
		FetchDataTask fdt = new FetchDataTask();
		fdt.id = Ids.FILM_DATES;
		fdt.execute( BASE_URL + "dates?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId);
		//Log.d( "Cineworld Request", BASE_URL + "dates?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId);
	}*/
	
	/*private void updatePerformancesForFilm( String date, String cinemaId, String filmId ) {
		if( mCurrentCinema == null || mCurrentFilm == null )
			return;
		
		mPFilmPerformanceData = null;
		
		//String cinemaId = mCurrentCinema.getId();
		//String filmId = mCurrentFilm.getEdi();
		
		FetchDataTask fdt = new FetchDataTask();
		fdt.id = Ids.DATE_TIMES;
		fdt.execute( BASE_URL + "performances?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId + "&date=" + date);
		mog.debug( BASE_URL + "performances?key=" + ApiKey.KEY + "&cinema=" + cinemaId + "&film=" + filmId + "&date=" + date);
	}*/
	
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
		
		/**
		 * Identifies the type of request.  
		 */
		public Ids id;
		
		/**
		 * A string that, combined with the above id can be used to identify the hashmap value to save results to.
		 */
		public String data;
		
		private CineVO dataVO;
		
		public CineVO getDataVO() {
			return dataVO;
		}
		
		public FetchDataTask(  ) {
			super();
			this.dataVO = new CineVO("");
		}
		
		public FetchDataTask( CineVO dataVO ) {
			super();
			this.dataVO = dataVO;
		}

		@Override
		protected HttpData doInBackground(String... url) {

			HttpRequest.timeout = 20000;
			HttpData data = HttpRequest.get( url[0] );
			
			return data;
		}

		@Override
		protected void onPostExecute(HttpData result) {
			
			super.onPostExecute(result);
			
			processResult( this, result );
		}
		
		
	}
}


