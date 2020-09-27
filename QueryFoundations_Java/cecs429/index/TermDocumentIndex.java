package cecs429.index;

import java.util.*;

/**
 * Implements an Index using a term-document matrix. Requires knowing the full corpus vocabulary and number of documents
 * prior to construction.
 */
public class TermDocumentIndex implements Index {
	private final boolean[][] mMatrix;
	private final List<String> mVocabulary;
	private int mCorpusSize;
	
	/**
	 * Constructs an empty index with with given vocabulary set and corpus size.
	 * @param vocabulary a collection of all terms in the corpus vocabulary.
	 * @param corpuseSize the number of documents in the corpus.
	 */
	public TermDocumentIndex(Collection<String> vocabulary, int corpuseSize) {
		mMatrix = new boolean[vocabulary.size()][corpuseSize];
		mVocabulary = new ArrayList<String>();
		mVocabulary.addAll(vocabulary);
		mCorpusSize = corpuseSize;
		
		Collections.sort(mVocabulary);
	}
	
	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(String term, int documentId) {
		int vIndex = Collections.binarySearch(mVocabulary, term);
		if (vIndex >= 0) {
			mMatrix[vIndex][documentId] = true;
		}
	}
	
	@Override
	public List<Posting> getPostings(String term) {// 1 2 3 7 9
		List<Posting> results = new ArrayList<>();
		
		// TODO: implement this method.
		// Binary search the mVocabulary array for the given term.
		// Walk down the mMatrix row for the term and collect the document IDs (column indices)
		// of the "true" entries.
		try {
			int vIndex = Collections.binarySearch(mVocabulary, term);
			for(int i = 0; i < mCorpusSize; i++) {
				if(mMatrix[vIndex][i] == true) {
					Posting id = new Posting(i);
					results.add(id);
				}
			}
		} catch (Exception e) {
			System.out.println("The word '"+ term + "' does not in the dictionary!");
		}
		return results;
	}
	
	public List<String> getVocabulary() {
		return Collections.unmodifiableList(mVocabulary);
	}
}
