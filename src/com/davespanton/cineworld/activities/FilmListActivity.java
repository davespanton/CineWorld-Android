package com.davespanton.cineworld.activities;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.davespanton.cineworld.R;

public class FilmListActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.films);
		
		ArrayList<String> data = getIntent().getStringArrayListExtra("data");
		
		setListAdapter( new ArrayAdapter<String>( this, R.layout.list_layout, data));
		
		setResult(-1);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		
		setResult(position);
		finish();
	}

}
