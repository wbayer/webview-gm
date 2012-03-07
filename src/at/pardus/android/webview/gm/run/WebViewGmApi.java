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

package at.pardus.android.webview.gm.run;

import android.util.Log;
import at.pardus.android.webview.gm.model.ScriptId;
import at.pardus.android.webview.gm.store.ScriptStore;

/**
 * Contains methods simulating GM functions that need access to the
 * app/database. Used as interface accessible from javascript code.
 */
public class WebViewGmApi {

	private static final String TAG = WebViewGmApi.class.getName();

	private ScriptStore scriptStore;

	private String secret;

	/**
	 * Constructor.
	 * 
	 * @param scriptStore
	 *            the database to query for values
	 * @param secret
	 *            the secret string to compare in each call
	 */
	public WebViewGmApi(ScriptStore scriptStore, String secret) {
		this.scriptStore = scriptStore;
		this.secret = secret;
	}

	/**
	 * Equivalent of GM_listValues.
	 * 
	 * @param scriptName
	 *            the name of the calling script
	 * @param scriptNamespace
	 *            the namespace of the calling script
	 * @param secret
	 *            the transmitted secret to validate
	 * @return a string consisting of the names of all found values separated by
	 *         commas
	 * @see <a href="http://wiki.greasespot.net/GM_listValues">GM_listValues</a>
	 */
	public String listValues(String scriptName, String scriptNamespace,
			String secret) {
		if (!this.secret.equals(secret)) {
			Log.e(TAG, "Call to \"listValues\" did not supply correct secret");
			return null;
		}
		String[] values = scriptStore.getValueNames(new ScriptId(scriptName,
				scriptNamespace));
		if (values == null || values.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String v : values) {
			sb.append(",");
			sb.append(v);
		}
		sb.deleteCharAt(0);
		return sb.toString();
	}

	/**
	 * Equivalent of GM_getValue.
	 * 
	 * @param scriptName
	 *            the name of the calling script
	 * @param scriptNamespace
	 *            the namespace of the calling script
	 * @param secret
	 *            the transmitted secret to validate
	 * @param name
	 *            the name of the value to get
	 * @param defaultValue
	 *            the value to return in case the one to retrieve does not exist
	 * @return the value of name or defaultValue if not found
	 * @see <a href="http://wiki.greasespot.net/GM_getValue">GM_getValue</a>
	 */
	public String getValue(String scriptName, String scriptNamespace,
			String secret, String name, String defaultValue) {
		if (!this.secret.equals(secret)) {
			Log.e(TAG, "Call to \"getValue\" did not supply correct secret");
			return null;
		}
		String v = scriptStore.getValue(new ScriptId(scriptName,
				scriptNamespace), name);
		return (v != null) ? v : defaultValue;
	}

	/**
	 * Equivalent of GM_setValue.
	 * 
	 * @param scriptName
	 *            the name of the calling script
	 * @param scriptNamespace
	 *            the namespace of the calling script
	 * @param secret
	 *            the transmitted secret to validate
	 * @param name
	 *            the name of the value to set
	 * @param value
	 *            the value to set
	 * @see <a href="http://wiki.greasespot.net/GM_setValue">GM_setValue</a>
	 */
	public void setValue(String scriptName, String scriptNamespace,
			String secret, String name, String value) {
		if (!this.secret.equals(secret)) {
			Log.e(TAG, "Call to \"setValue\" did not supply correct secret");
			return;
		}
		scriptStore.setValue(new ScriptId(scriptName, scriptNamespace), name,
				value);
	}

	/**
	 * Equivalent of GM_deleteValue.
	 * 
	 * @param scriptName
	 *            the name of the calling script
	 * @param scriptNamespace
	 *            the namespace of the calling script
	 * @param secret
	 *            the transmitted secret to validate
	 * @param name
	 *            the name of the value to delete
	 * @see <tt><a href="http://wiki.greasespot.net/GM_deleteValue">GM_deleteValue</a></tt>
	 */
	public void deleteValue(String scriptName, String scriptNamespace,
			String secret, String name) {
		if (!this.secret.equals(secret)) {
			Log.e(TAG, "Call to \"deleteValue\" did not supply correct secret");
			return;
		}
		scriptStore
				.deleteValue(new ScriptId(scriptName, scriptNamespace), name);
	}

	/**
	 * Equivalent of GM_log. Output in Android log.
	 * 
	 * @param scriptName
	 *            the name of the calling script
	 * @param scriptNamespace
	 *            the namespace of the calling script
	 * @param secret
	 *            the transmitted secret to validate
	 * @param message
	 *            the message to log
	 * @see <tt><a href="http://wiki.greasespot.net/GM_log">GM_log</a></tt>
	 */
	public void log(String scriptName, String scriptNamespace, String secret,
			String message) {
		if (!this.secret.equals(secret)) {
			Log.e(TAG, "Call to \"log\" did not supply correct secret");
			return;
		}
		Log.i(TAG, scriptName + ", " + scriptNamespace + ": " + message);
	}
}
