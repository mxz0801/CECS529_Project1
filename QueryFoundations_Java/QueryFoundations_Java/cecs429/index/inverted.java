package cecs429.index;

import java.util.*;

/**
 * Implements Inverted Index 
 */
public class inverted implements Index{
	private Map<String,ArrayList<Posting>> mIndex= new HashMap<String, ArrayList<Posting>>();
	private List<String> mVocabulary = new ArrayList<String>();
	
	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(String term, int documentId) {
		Posting docId = new Posting(documentId);
		ArrayList<Posting> postList = new ArrayList<Posting>();
		if(mIndex.get(term)==null) {
			postList.add(docId);
			mVocabulary.add(term);
			mIndex.put(term, postList);
		}
		else if(mIndex.get(term).get(mIndex.get(term).size() - 1).getDocumentId()!= docId.getDocumentId()) 
			mIndex.get(term).add(docId);
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
