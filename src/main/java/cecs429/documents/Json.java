package cecs429.documents;


public interface Json extends FileDocument {
	/**
	 * The "title" entry in the json file
	 */
	String getJsonTitle();
	/**
	 * The "body" entry in the json file
	 */
	String getJsonBody();
}
