package main.java.cecs429.index;

import java.util.*;
/**
 *	Implements Positional Inverted Index 
 */

public class PositionalInvertedIndex implements Index {
	private Map<String,List<Posting>> mIndex= new HashMap<String, List<Posting>>();
	private List<String> mVocabulary = new ArrayList<String>();
	
	/**
	 * Associates the given documentId and position with the given term in the index.
	 */
	public void addTerm(String term, int documentId, int position) {
		//TO REVISE: something wrong when search for "body"
		ArrayList<Posting> postList = new ArrayList<Posting>();
		ArrayList<Integer> positionList = new ArrayList<Integer>();
		positionList.add(position);
		Posting post = new Posting(documentId, positionList);
		postList.add(post);
		if(mIndex.get(term)==null) {
			mVocabulary.add(term);
			mIndex.put(term, postList);
		}
		else if(mIndex.get(term).get(mIndex.get(term).size() - 1).getDocumentId()!= documentId) 
			mIndex.get(term).add(post);
		else if(mIndex.get(term).get(mIndex.get(term).size() - 1).getDocumentId()== documentId) 
			mIndex.get(term).get(mIndex.get(term).size() - 1).getPosition().add(position);
	}
	
	@Override
	public List<Posting> getPostings(String term) {
		return mIndex.get(term);
	}
	
	public List<String> getVocabulary() {
		Collections.sort(mVocabulary);
		return mVocabulary;
	}

}
