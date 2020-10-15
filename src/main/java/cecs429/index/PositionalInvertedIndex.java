package cecs429.index;

import java.util.*;
/**
 *	Implements Positional Inverted Index 
 */

public class PositionalInvertedIndex implements Index {
	private Map<String,ArrayList<Posting>> mIndex= new HashMap<>();
	private List<String> mVocabulary = new ArrayList<>();
	
	/**
	 * Associates the given documentId and position with the given term in the index.
	 */
	public void addTerm(String term, int documentId, int position) {
		ArrayList<Posting> postList = new ArrayList<>();
		ArrayList<Integer> positionList = new ArrayList<>();
		positionList.add(position);
		Posting post = new Posting(documentId, positionList);
		postList.add(post);
		if(mIndex.get(term)==null) {
			mVocabulary.add(term);
			mIndex.put(term, postList);
		}
		else if(mIndex.get(term).get(mIndex.get(term).size() - 1).getDocumentId() != documentId)
			mIndex.get(term).add(post);
		else if(mIndex.get(term).get(mIndex.get(term).size() - 1).getDocumentId() == documentId)
			mIndex.get(term).get(mIndex.get(term).size() - 1).getPosition().add(position);
	}
	
	@Override
	public List<Posting> getPostings(String term) {
		ArrayList<Posting> p = new ArrayList<>();
		if(mIndex.get(term) == null)
			return  p;
		else
			return mIndex.get(term);
	}

	public List<String> getVocabulary() {
		Collections.sort(mVocabulary);
		return mVocabulary;
	}


}
