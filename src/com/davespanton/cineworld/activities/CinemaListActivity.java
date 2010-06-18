package com.davespanton.cineworld.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.davespanton.cineworld.R;

public class CinemaListActivity extends ListActivity {

	public static final int CONTEXT_VIEW_INFO = 0;
	
	public static final int INFO_DIALOG = 0;
	
	public String mSelected;
	
	private JSONArray rawData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cinema);
		
		ArrayList<String> data = getIntent().getStringArrayListExtra("data");
		
		setListAdapter( new ArrayAdapter<String>( this, R.layout.list_layout, data));
		
		try {
			rawData = (JSONArray) ((JSONObject) new JSONTokener(getIntent().getStringExtra("raw")).nextValue()).getJSONArray("cinemas");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setResult( -1 );
		
		registerForContextMenu(getListView());
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
		
		menu.add(0, CONTEXT_VIEW_INFO, 0, R.string.view_info);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = super.onContextItemSelected(item);
		
		switch( item.getItemId() ) {
			case CONTEXT_VIEW_INFO:
				mSelected = (String) item.getTitle();
				showDialog(INFO_DIALOG);
			return true;
		}
		
		return result; 
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(mSelected)
		.setMessage("this is a message about something")
		.setNeutralButton(getString(R.string.okay), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog alert = builder.create();
		return alert;
	}
	
	
	
}
