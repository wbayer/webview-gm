WebView-GM
==========

This library for Android projects adds Greasemonkey-compatible user
script support to standard WebView components (version 2.2+).

Usage
-----

##### Gradle
        compile 'at.pardus.android:webview-gm-lib:1.2.0'
        compile 'at.pardus.android:webview-gm-ui:1.2.0'
##### Maven
        <dependency>
            <groupId>at.pardus.android</groupId>
            <artifactId>webview-gm-lib</artifactId>
            <version>1.2.0</version>
        </dependency>
        <dependency>
            <groupId>at.pardus.android</groupId>
            <artifactId>webview-gm-ui</artifactId>
            <version>1.2.0</version>
        </dependency>
* Create a new **ScriptStoreSQLite** object for script and value
  persistence, or implement your own ScriptStore.
* Make the WebView class that you want to run scripts in extend
  **WebViewGm**, or use the ScriptBrowser class in the ***UI module***.
* Handle the management of scripts using the classes in the ***UI module***,
  or extend them, or implement your own.

The ***demo module*** contains an implementation of the library and
all its components. It uses a single WebView for both the download of
user scripts and running them.

#### Apps using it
[Pardus Android](https://play.google.com/store/apps/details?id=at.pardus.android.browser)

Limitations
-----------

GM_xmlhttpRequest implementation is missing an "abort()" callback.

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

Contributing
------------

Fork the repository and send a pull request:
http://help.github.com/send-pull-requests/