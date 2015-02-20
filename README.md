WebView-GM
==========

This library for Android projects adds Greasemonkey-compatible user
script support to standard WebView components (version 2.2+).

Usage
-----

* Download the source and import it in Eclipse.
* Add it as library in your Android projects (Properties -> Android).
* Create a new ScriptStoreSQLite object for script and value
  persistence, or implement your own ScriptStore.
* Make the WebView class that you want to run scripts in extend
  WebViewGm, or use the ScriptBrowser class in the UI package.
* Handle the management of scripts using the classes in the UI package,
  or extend them, or implement your own.

The WebViewGmImpl class contains an implementation of the library and
all its components. It uses a single WebView for both the download of
user scripts and running them.

### Apps using it
[Pardus Android](https://play.google.com/store/apps/details?id=at.pardus.android.browser)

ToDo
----

Missing GM functions:
GM_registerMenuCommand, GM_openInTab, GM_info, GM_setClipboard
See http://wiki.greasespot.net/Greasemonkey_Manual:API

Missing metablock header interpretation/implementation:
@noframes, @grant
See http://wiki.greasespot.net/Metadata_Block

Improvement of the user interface.
Update checks of the installed user scripts.

Limitations
-----------

GM_xmlhttpRequest implementation is missing an "abort()" callback.

Contributing
------------

Fork the repository and send a pull request:
http://help.github.com/send-pull-requests/