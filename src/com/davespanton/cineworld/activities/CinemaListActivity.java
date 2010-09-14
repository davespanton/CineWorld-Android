package com.davespanton.cineworld.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.davespanton.cineworld.R;
import com.davespanton.cineworld.data.CinemaList;
import com.davespanton.cineworld.services.CineWorldService;

public class CinemaListActivity extends ListActivity {

	public static final int CONTEXT_VIEW_INFO = 0;
	
	public static final int INFO_DIALOG = 0;
	
	private int mSelectedIndex;
	
	private CineWorldService cineWorldService;
	
	private CinemaList mCinemaList;
	
	public void onConnected() {
		
		setListAdapter( new ArrayAdapter<String>( 
				this, 
				R.layout.list_layout, 
				cineWorldService.getCinemaNames()));
		
		mCinemaList = cineWorldService.getCinemaList();
		
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cinema);
		
		bindService( new Intent(this, CineWorldService.class), service, BIND_AUTO_CREATE);
		
		setResult( -1 );
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindService(service);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		setResult(position);
		finish();
	
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, v, menuInfo);
		
		mSelectedIndex = (int) ((AdapterContextMenuInfo) menuInfo).id;
		menu.add(0, CONTEXT_VIEW_INFO, 0, R.string.view_info);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);
		
		switch( item.getItemId() ) {
			case CONTEXT_VIEW_INFO:
				showDialog(mSelectedIndex);
			return true;
		}
		
		return result; 
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		
		String message = "";
		String title = "";
		
		title = mCinemaList.get(mSelectedIndex).getName();
			
		message += mCinemaList.get(mSelectedIndex).getAddress();
		message += ", ";
		message += mCinemaList.get(mSelectedIndex).getPostcode();
		message += "\n\nTel: ";
		message += mCinemaList.get(mSelectedIndex).getTelephone();
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
		.setMessage( message )
		.setNeutralButton(getString(R.string.okay), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog alert = builder.create();
		return alert;
	}
	
	
	
	private ServiceConnection service = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			cineWorldService = ((CineWorldService.LocalBinder)service).getService();
			onConnected();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			cineWorldService = null;
		}
	};
	
}
