package main.java.cecs429.index;

import java.util.ArrayList;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private ArrayList<Integer> mPosition;
	public Posting(int documentId, ArrayList<Integer> position) {
		mDocumentId = documentId;
		mPosition = position;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
	
	public ArrayList<Integer> getPosition(){
		return mPosition;
	}
}
