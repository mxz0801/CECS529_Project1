package cecs429.documents;

/**
 * The format of json file
 */
public class GsonDoc {
		private String body;
		private String title;
		private String url;


	public GsonDoc(String body, String title, String url) {
		this.body = body;
		this.title = title;
		this.url = url;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
