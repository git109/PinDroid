/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */
package com.pindroid.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.pindroid.R;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.TagContent.Tag;

public class BrowseTagsFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private String sortfield = Tag.Name + " ASC";
	private SimpleCursorAdapter mAdapter;
	
	private String mAccount = "";
	
	private OnTagSelectedListener tagSelectedListener;
	private OnItemClickListener clickListener;
	
	public interface OnTagSelectedListener {
		public void onTagSelected(String tag);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		clickListener = viewListener;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		mAdapter = new SimpleCursorAdapter(this.getActivity(), 
				R.layout.tag_view, null, 
				new String[] {Tag.Name, Tag.Count}, new int[] {R.id.tag_name, R.id.tag_count}, 0);
		
		setListAdapter(mAdapter);	
		
		getLoaderManager().initLoader(0, null, this);
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		lv.setOnItemClickListener(clickListener);
	}
	
	public void setAccount(String account) {
		mAccount = account;
	}
	
	public void setAction(String action) {
		if(action.equals("pick")) {
			clickListener = pickListener;
		} else clickListener = viewListener;
	}
	
	private OnItemClickListener viewListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    	String tagName = ((TextView)view.findViewById(R.id.tag_name)).getText().toString();
	    	
	    	tagSelectedListener.onTagSelected(tagName);
	    }
	};
	
	private OnItemClickListener pickListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    	String tagName = ((TextView)view.findViewById(R.id.tag_name)).getText().toString();
	    	
	    	Intent i = new Intent();
	    	i.putExtra("tagname", tagName);
	    	
	    	getActivity().setResult(Activity.RESULT_OK, i);
	    	getActivity().finish();
	    }
	};
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.browse_tag_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		boolean result = false;

	    switch (item.getItemId()) {
		    case R.id.menu_tag_sort_name_asc:
		    	sortfield = Tag.Name + " ASC";
				result = true;
				break;
		    case R.id.menu_tag_sort_name_desc:			
		    	sortfield = Tag.Name + " DESC";
		    	result = true;
		    	break;
		    case R.id.menu_tag_sort_count_asc:			
		    	sortfield = Tag.Count + " ASC";
		    	result = true;
		    	break;
		    case R.id.menu_tag_sort_count_desc:			
		    	sortfield = Tag.Count + " DESC";
		    	result = true;
		    	break;
	    }
	    
	    if(result) {
	    	getLoaderManager().restartLoader(0, null, this);
	    } else result = super.onOptionsItemSelected(item);
	    
	    return result;
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(mAccount != null && !mAccount.equals("")) {
			if(Intent.ACTION_SEARCH.equals(getActivity().getIntent().getAction())) { 		
				String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
				return TagManager.SearchTags(query, mAccount, this.getActivity());
			} else {
				return TagManager.GetTags(mAccount, sortfield, this.getActivity());
			}
		}
		else return null;
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    mAdapter.swapCursor(data);
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
	    mAdapter.swapCursor(null);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			tagSelectedListener = (OnTagSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTagSelectedListener");
		}
	}
}