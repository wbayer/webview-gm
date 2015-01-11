/*
 *    Copyright 2015 Richard Broker
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


package at.pardus.android.webview.gm.model;

import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import at.pardus.android.webview.gm.util.UnicodeReader;

public class XmlHttpRequest {

	private static final String TAG = XmlHttpRequest.class.getName();

	private boolean binary;
	private JSONObject context;
	private String data;
	private JSONObject headers;
	private String method;
	private String onAbort;
	private String onError;
	private String onLoad;
	private String onProgress;
	private String onReadyStateChange;
	private String onTimeout;
	private String overrideMimeType;
	private String password;
	private boolean synchronous;
	private int timeout;
	private JSONObject upload;
	private String url;
	private String user;
	private final WebView view;

	public XmlHttpRequest(WebView view, String jsonRequestString) {
		// Register the view so that we can execute JS callbacks (e.g. onload)
		this.view = view;

		JSONObject jsonObject;

		try {
			// Get all required object members.
			jsonObject = new JSONObject(jsonRequestString);
			this.method = jsonObject.getString("method");
			this.url = jsonObject.getString("url");
		} catch (JSONException e) {
			Log.e(TAG, "JSON parsing exception:" + e.getMessage());
			return;
		}

		// Get optional object members.
		this.binary = jsonObject.optBoolean("binary");
		this.context = jsonObject.optJSONObject("context");
		this.data = jsonObject.optString("data");
		this.headers = jsonObject.optJSONObject("headers");
		this.onAbort = jsonObject.optString("onabort");
		this.onError = jsonObject.optString("onerror");
		this.onLoad = jsonObject.optString("onload");
		this.onProgress = jsonObject.optString("onprogress");
		this.onReadyStateChange = jsonObject.optString("onreadystatechange");
		this.onTimeout = jsonObject.optString("ontimeout");
		this.overrideMimeType = jsonObject.optString("overrideMimeType");
		this.password = jsonObject.optString("password");
		this.synchronous = jsonObject.optBoolean("synchronous");
		this.timeout = jsonObject.optInt("timeout");
		this.upload = jsonObject.optJSONObject("upload");
		this.user = jsonObject.optString("user");
	}

	public byte[] getDataBytes() {
		if (this.data.length() == 0) {
			return null;
		}

		try {
			return this.data.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unable to get UTF-8 bytes from string data: " + this.data);
			return null;
		}
	}

	public Map<String,String> getHeaders() {
		if (this.headers == null) {
			return null;
		}

		Map<String,String> headers = new HashMap<String,String>();
		for (Iterator<String> keyIterator = this.headers.keys(); keyIterator.hasNext();) {
			String keyName = keyIterator.next();

			try {
				headers.put(keyName, this.headers.getString(keyName));
			} catch (JSONException e) {
				return null;
			}
		}

		return headers;
	}

	/**
	 * Initiates a cross-domain HTTP Request to the address
	 * specified by this object's "url" member.
	 */
	public XmlHttpResponse execute() {
		if (this.synchronous) {
			return executeHttpRequestSync();
		} else {
			return executeHttpRequestAsync();
		}
	}

	private XmlHttpResponse executeHttpRequestAsync() {
		XmlHttpResponse response = new XmlHttpResponse(this.context);
		Thread thread = new Thread() {
			@Override
			public void run() {
				executeHttpRequestSync();
			}
		};
		thread.start();

		return response;
	}

	private XmlHttpResponse executeHttpRequestSync() {
		XmlHttpResponse response = new XmlHttpResponse(this.context);
		StringBuilder out = new StringBuilder();
		URL url;
		int totalBytesRead = 0;
		int contentLength;
		byte[] outputData = this.getDataBytes();

		try {
			url = new URL(this.url);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Specified URL is malformed: " + this.url);
			executeOnErrorCallback(response);
			return response;
		}

		try {
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

			response.setReadyState(XmlHttpResponse.READY_STATE_OPENED);
			executeOnReadyStateChangeCallback(response);

			// Set connection properties for the GM_xmlhttpRequest
			if (outputData != null) {
				httpConn.setDoOutput(true);
				httpConn.setRequestProperty("Content-Length", Integer.toString(outputData.length));
			}

			if ((!this.user.equals("")) && (!this.password.equals(""))) {
				httpConn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((this.user + ":" + this.password).getBytes("UTF-8"), Base64.DEFAULT));
			}

			httpConn.setRequestMethod(this.method);

			Map<String,String> headers = this.getHeaders();
			if (this.headers != null) {
				for (String key: headers.keySet()) {
					httpConn.setRequestProperty(key, headers.get(key));
				}
			}

			if (!this.overrideMimeType.equals("")) {
				httpConn.setRequestProperty("Content-Type", this.overrideMimeType);
			}

			// #TODO #FIXME this makes the timeouts cumulative, but it seems most sensible
			// doesn't seem like we can set a timeout for the write either, so POST, PUT
			// wont ever timeout... seems like there should be a better way!
			httpConn.setConnectTimeout(this.timeout);
			httpConn.setReadTimeout(this.timeout);

			// Explicitly initiate connection.
			httpConn.connect();

			// Begin transmitting data if requested.
			if (outputData != null) {
				OutputStream outputStream = httpConn.getOutputStream();
				outputStream.write(outputData);
				outputStream.close();
			}

			response.setStatus(httpConn.getResponseCode());
			response.setStatusText(httpConn.getResponseMessage());

			// Adjust URL after "Location:" redirects, should be final now.
			response.setFinalUrl(httpConn.getURL().toString());

			response.setReadyState(XmlHttpResponse.READY_STATE_HEADERS_RECEIVED);
			executeOnReadyStateChangeCallback(response);

			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.e(TAG, "HTTP error from url: " + this.url + " HTTP Response " + httpConn.getResponseCode());
				httpConn.disconnect();
				executeOnErrorCallback(response);
				return response;
			}

			// Setup progress parameters if applicable.
			contentLength = httpConn.getContentLength();

			if (contentLength > 0) {
				response.setLengthComputable(true);
				response.setTotal(contentLength);
			}

			response.setReadyState(XmlHttpResponse.READY_STATE_LOADING);
			executeOnReadyStateChangeCallback(response);

			// Begin receiving any response data/
			InputStream inputStream = httpConn.getInputStream();
			int bytesRead;
			char[] buffer = new char[4096];
			Reader in = new UnicodeReader(inputStream, httpConn.getContentEncoding());

			while ((bytesRead = in.read(buffer, 0, 4096)) != -1) {
				if (bytesRead <= 0) {
					break;
				}

				// Progress events are always 1-step behind where we currently are.
				if ((totalBytesRead) > 0 && (contentLength > 0)) {
					response.setLoaded(totalBytesRead);
					executeOnProgressCallback(response);
				}

				out.append(buffer, 0, bytesRead);
				totalBytesRead += bytesRead;
			}

			// Clean up open resources & report completion.
			inputStream.close();
			httpConn.disconnect();

			response.setResponseText(out.toString());

			response.setReadyState(XmlHttpResponse.READY_STATE_DONE);
			executeOnReadyStateChangeCallback(response);

			executeOnLoadCallback(response);
		} catch (SocketTimeoutException e) {
			Log.e(TAG, "Timeout issuing GM_xmlhttpRequest for: "
					+ this.url + ": " + e.getMessage());
			executeOnTimeoutCallback(response);
		} catch (ConnectTimeoutException e) {
			Log.e(TAG, "Connection timeout issuing GM_xmlhttpRequest for: "
					+ this.url + ": " + e.getMessage());
			executeOnTimeoutCallback(response);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unable to get UTF-8 bytes for HTTP Basic Auth username/password");
			return null;
		} catch (IOException e) {
			Log.e(TAG, "Exception issuing GM_xmlhttpRequest for: "
					+ this.url + ": " + e.getMessage());
			executeOnErrorCallback(response);
		}

		return response;
	}

	private void executeOnAbortCallback(XmlHttpResponse response) {
		if (this.onAbort == null) {
			return;
		}

		view.loadUrl("javascript: (function() { unsafeWindow. " + this.onAbort
				+ "(JSON.parse(" + response.toJSONString() + ")); })()");
	}

	private void executeOnErrorCallback(XmlHttpResponse response) {
		if (this.onError == null) {
			return;
		}

		view.loadUrl("javascript: (function() { unsafeWindow. " + this.onError
				+ "(JSON.parse(" + response.toJSONString() + ")); })()");
	}

	private void executeOnLoadCallback(XmlHttpResponse response) {
		if (this.onLoad == null) {
			return;
		}

		view.loadUrl("javascript: (function() { unsafeWindow. " + this.onLoad
				+ "(JSON.parse(" + response.toJSONString() + ")); })()");
	}

	private void executeOnProgressCallback(XmlHttpResponse response) {
		if (this.onProgress == null) {
			return;
		}

		view.loadUrl("javascript: (function() { unsafeWindow. " + this.onProgress
				+ "(JSON.parse(" + response.toJSONString() + ")); })()");
	}

	private void executeOnReadyStateChangeCallback(XmlHttpResponse response) {
		if (this.onReadyStateChange == null) {
			return;
		}

		view.loadUrl("javascript: (function() { unsafeWindow. " + this.onReadyStateChange
				+ "(JSON.parse(" + response.toJSONString() + ")); })()");
	}

	private void executeOnTimeoutCallback(XmlHttpResponse response) {
		if (this.onTimeout == null) {
			return;
		}

		view.loadUrl("javascript: (function() { unsafeWindow. " + this.onTimeout
				+ "(JSON.parse(" + response.toJSONString() + ")); })()");
	}
}
