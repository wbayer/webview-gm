/*
 *    Copyright 2012 Werner Bayer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.pardus.android.webview.gm.store.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.model.ScriptId;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.webview.gm.R;

/**
 * Includes the UI to list, enable/disable and delete scripts.
 */
public class ScriptList {

	class Listener implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
		
		ScriptAdapter scriptAdapter;
		
		Script script;
		
		View row;
		
		View delete;
		
		View edit;
		
		public Listener(ScriptAdapter scriptAdapter, Script script, View row) {
			this.scriptAdapter = scriptAdapter;
			this.script = script;
			this.row = row;
			
			CheckBox active;
			
			delete = row.findViewById(R.id.sli_delete);
			edit = row.findViewById(R.id.sli_edit);
			active = (CheckBox) row.findViewById(R.id.sli_active);
			
			delete.setOnClickListener(this);
			edit.setOnClickListener(this);
			active.setOnCheckedChangeListener(this);			
		}		
		
		public void update(Script script) {
			this.script = script;
		}
		
		@Override
		public void onClick (View view) {
			if (view == delete) {
				scriptStore.delete(script);
				scriptAdapter.refresh();
			} else if (view == edit)
				activity.openScriptEditor(script);				
			
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked)
				scriptStore.enable(script);
			else
				scriptStore.disable(script);
		}
	}
	
	static class Holder {
		
		Script script;
		
		CheckBox active;
		
		TextView name;
		
		TextView description;
		
		Listener listener;
		
		public Holder(View view, Listener listener) {
			this.listener = listener;

			active = (CheckBox) view.findViewById(R.id.sli_active);
			name = (TextView) view.findViewById(R.id.sli_name);
			description = (TextView) view.findViewById(R.id.sli_description);			
		}
		
		public void fill(ScriptStore scriptStore, Script script) {
			this.script = script;
			
			listener.update(script);
			active.setChecked(scriptStore.isEnabled(script));
			name.setText(script.getName());
			description.setText(script.getDescription());
		}
	}
	
	class ScriptAdapter extends BaseAdapter {

		/// The full (unfiltered) list of items.
		Script script [];
		
		/// The script store
		ScriptStore scriptStore;

		public ScriptAdapter(ScriptStore scriptStore) {
			this.scriptStore = scriptStore;
			
			script = scriptStore.getAll();
		}
		
		public void refresh ()
		{
			script = scriptStore.getAll();
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount () {
			return script.length;
		}
		
		@Override
		public Script getItem (int position) {
			return script [position];
		}
		
		@Override
		public long getItemId (int position) {
			return position;			
		}
	
		@Override
		public View getView (int position, View row, ViewGroup parent) {
			LayoutInflater inflater;
			Listener listener;
			Holder holder;
			Script script;

			script = getItem(position);
			holder = row != null ? (Holder) row.getTag() : null;
			if (holder == null) {
				inflater = activity.getLayoutInflater();			

				row = inflater.inflate (R.layout.script_list_item, parent, false);
				
				listener = new Listener(this, script, row);
				holder = new Holder (row, listener);
				row.setTag (holder);
			}

			holder.fill (scriptStore, script);
			
			return row;
		}		
	}
	
	
	protected ScriptManagerActivity activity;

	protected ScriptStore scriptStore;

	protected ListView scriptList;

	/**
	 * Returns the view listing all installed scripts.
	 * 
	 * @return the updated list view
	 */
	public View getScriptList() {
		scriptList.setAdapter(getScriptListAdapter ());
		scriptList.invalidate();
		return scriptList;
	}

	/**
	 * Returns a list adapter rendering all the installed scripts
	 * 
	 * @return the list adapter
	 */
	public ListAdapter getScriptListAdapter() {
		return new ScriptAdapter(scriptStore);
	}

	/**
	 * Returns the ScriptId object for a given position in the list.
	 * 
	 * @param position
	 *            the selected position
	 * @return the ScriptId object at that position
	 */
	public ScriptId getScriptId(int position) {
		return (ScriptId) scriptList.getItemAtPosition(position);
	}

	/**
	 * Inflates the ListView from XML and registers its OnItemClickListener and
	 * context menu.
	 */
	private void init() {
		scriptList = (ListView) activity.getLayoutInflater().inflate(
				R.layout.script_list, null);
		scriptList.setTextFilterEnabled(true);
		activity.registerForContextMenu(scriptList);
	}

	/**
	 * Constructor.
	 * 
	 * @param activity
	 *            the application's activity
	 * @param scriptStore
	 *            the database to use
	 */
	public ScriptList(ScriptManagerActivity activity, ScriptStore scriptStore) {
		this.activity = activity;
		this.scriptStore = scriptStore;
		init();
	}

}
