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

package at.pardus.android.webview.gm;

import java.util.EmptyStackException;
import java.util.Stack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import at.pardus.android.webview.gm.model.ScriptId;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.android.webview.gm.store.ScriptStoreSQLite;
import at.pardus.android.webview.gm.store.ui.ScriptBrowser;
import at.pardus.android.webview.gm.store.ui.ScriptEditor;
import at.pardus.android.webview.gm.store.ui.ScriptList;
import at.pardus.android.webview.gm.store.ui.ScriptManagerActivity;
import at.pardus.webview.gm.R;

/**
 * Implements all parts of the WebView GM library.
 */
public class WebViewGmImpl extends ScriptManagerActivity {

	private Stack<Integer> placeHistory = new Stack<Integer>();

	private SharedPreferences preferences;
	
	private boolean startBrowser;

	private static final Integer LIST = 1;

	private static final Integer BROWSER = 2;
	
	public WebViewGmImpl() {
		startBrowser=true;
	}
	
	public WebViewGmImpl(boolean startBrowser) {
		this.startBrowser=startBrowser;
	}

	@Override
	public void openScriptList() {
		getScriptList();
		setTitle(R.string.webviewgm_impl_app_name);
		setContentView(getScriptList ().getScriptList());
		placeHistory.push(LIST);
	}
	
	protected final ScriptList getScriptList() {
		if (scriptList == null)
			scriptList = createScriptList(); 
		return scriptList;
	}
	
	protected ScriptList createScriptList() {
		return new ScriptList(this, getScriptStore());
	}

	@Override
	public void openScriptEditor(ScriptId scriptId) {
		setContentView(getScriptEditor().getEditForm(scriptId));
		placeHistory.push(null);
	}
	
	protected final ScriptEditor getScriptEditor() {
		if (scriptEditor == null)
			scriptEditor = createScriptEditor ();
		return scriptEditor;
	}
	
	protected ScriptEditor createScriptEditor() {
		return new ScriptEditor(this, getScriptStore());
	}

	@Override
	public void openScriptBrowser() {
		setContentView(getScriptBrowser().getBrowser());
		placeHistory.push(BROWSER);
	}
	
	protected final ScriptBrowser getScriptBrowser() {
		if (scriptBrowser == null)
			scriptBrowser = createScriptBrowser();
		return scriptBrowser;
	}
	
	protected ScriptBrowser createScriptBrowser() {
		return new ScriptBrowser(this, getScriptStore(), 
				preferences.getString("lastUrl", "http://userscripts.org/"));
	}
	
	protected final ScriptStore getScriptStore() {
		if (scriptStore == null) {
			scriptStore = createScriptStore();
			scriptStore.open();
		}
		return scriptStore != null ? scriptStore : createScriptStore();
	}
	
	protected ScriptStoreSQLite createScriptStore() {
		return new ScriptStoreSQLite(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.impl_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_browse) {
			openScriptBrowser();
			return true;
		} else if (item.getItemId() == R.id.menu_list) {
			openScriptList();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		preferences = getSharedPreferences("P", Context.MODE_PRIVATE);
		if(startBrowser)
			openScriptBrowser();
	}

	@Override
	protected void onResume() {
		if (scriptStore != null) {
			scriptStore.open();
		}
		if (scriptBrowser != null) {
			scriptBrowser.resume();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (scriptBrowser != null) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("lastUrl", scriptBrowser.getUrl());
			editor.commit();
			scriptBrowser.pause();
		}
		if (scriptStore != null) {
			scriptStore.close();
		}
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		try {
			Integer thisPlace = placeHistory.pop();
			if (BROWSER.equals(thisPlace) && scriptBrowser.back()) {
				placeHistory.push(thisPlace);
			} else {
				while (true) {
					Integer prevPlace = placeHistory.pop();
					if (prevPlace == null || prevPlace.equals(thisPlace)) {
						continue;
					}
					if (LIST.equals(prevPlace)) {
						openScriptList();
						return;
					}
					if (BROWSER.equals(prevPlace)) {
						openScriptBrowser();
						return;
					}
				}
			}
		} catch (EmptyStackException e) {
			super.onBackPressed();
		}
	}

}
