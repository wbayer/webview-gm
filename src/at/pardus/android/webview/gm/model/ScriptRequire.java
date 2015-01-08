package at.pardus.android.webview.gm.model;

/**
 * Object containing one @require Metadata entry.
 *
 * @see <a href="http://wiki.greasespot.net/Metadata_Block">Metadata Block</a>
 */
public class ScriptRequire {

	private String url;
	private String content;

	public ScriptRequire(String url, String content) {
		this.url = url;
		this.content = content;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String newUrl) {
		this.url = newUrl;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String newContent) {
		this.content = newContent;
	}
}
