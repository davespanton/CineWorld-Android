package com.davespanton.cineworld.activities;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.services.CineWorldService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class DateListActivity extends Activity {
	
	private CineWorldService cineWorldService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.performances);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		bindService(new Intent(this, CineWorldService.class), service, BIND_AUTO_CREATE);
	}
	
	private ServiceConnection service = new ServiceConnection() {

				@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			cineWorldService = ((CineWorldService.LocalBinder)service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			cineWorldService = null;
		}
		
	};

}
