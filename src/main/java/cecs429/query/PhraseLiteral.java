package cecs429.query;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cecs429.index.Index;
import cecs429.index.KgramIndex;
import cecs429.index.Posting;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements Query {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}
	
	@Override
	public List<Posting> getPostings(Index index) {

		// TODO: program this method. Retrieve the postings for the individual terms in the phrase,
		// and positional merge them together.

		List<Posting> result = new ArrayList<>();
		List<Posting> bufferList = new ArrayList<>();
		int gapOfDoc = 1; // use it to find the position
		for (String s : mTerms) {
			if (s.equals(mTerms.get(0))) {
				bufferList = index.getPostings(s);
				continue;
			}
			int i = 0;
			int j = 0;

			// compare the docID in each two terms
			while (i < bufferList.size() && j < index.getPostings(s).size()) {
				//find the same docID
				if (bufferList.get(i).getDocumentId() == index.getPostings(s).get(j).getDocumentId()) {
					int m = 0;
					int n = 0;

					// compare the position
					while (m < bufferList.get(i).getPosition().size() && n < index.getPostings(s).get(j).getPosition().size()) {
						//find the position with phrase's order
						if (index.getPostings(s).get(j).getPosition().get(n) == bufferList.get(i).getPosition().get(m) + gapOfDoc) {
							result.add(bufferList.get(i));
							gapOfDoc++;
						} else if (index.getPostings(s).get(j).getPosition().get(n) <= bufferList.get(i).getPosition().get(m))
							n++;
						else if (index.getPostings(s).get(j).getPosition().get(n) > bufferList.get(i).getPosition().get(m))
							m++;
					}
					i++;
					j++;
				}
				else if (result.get(i).getDocumentId() < index.getPostings(s).get(j).getDocumentId())
					i++;
				else if (result.get(i).getDocumentId() > index.getPostings(s).get(j).getDocumentId())
					j++;
			}
			bufferList = result;
		}
		return result;
	}

	@Override
	public List<Posting> getPostings(Index index, KgramIndex index2){
		WildcardLiteral wildcardQuery = new WildcardLiteral();
		List<Posting> result = new ArrayList<>();
		List<Posting> bufferList = new ArrayList<>();
		List<Posting> qList = new ArrayList<>();
		int gapOfDoc = 1;
		for(String s : mTerms){
			if(s.contains("*") && s == mTerms.get(0)) {
				wildcardQuery.setWildcardLiteral(s);
				bufferList = wildcardQuery.getPostings(index, index2);
				continue;
			}
			else if(s.contains("*") == false && s == mTerms.get(0)){
				bufferList = index.getPostings(s);
				continue;
			}
			int i = 0;
			int j = 0;
			if(s.contains("*")) {
				wildcardQuery.setWildcardLiteral(s);
				qList = wildcardQuery.getPostings(index, index2);
			}
			else
				qList = index.getPostings(s);
			// compare the docID in each two terms
			while (i < bufferList.size() && j < qList.size()) {
				//find the same docID
				if (bufferList.get(i).getDocumentId() == qList.get(j).getDocumentId()) {
					int m = 0;
					int n = 0;

					// compare the position
					while (m < bufferList.get(i).getPosition().size() && n < qList.get(j).getPosition().size()) {
						//find the position with phrase's order
						if (index.getPostings(s).get(j).getPosition().get(n) == qList.get(j).getPosition().get(m) + gapOfDoc) {
							result.add(bufferList.get(i));
							gapOfDoc++;
						} else if (qList.get(j).getPosition().get(n) <= bufferList.get(i).getPosition().get(m))
							n++;
						else if (qList.get(j).getPosition().get(n) > bufferList.get(i).getPosition().get(m))
							m++;
					}
					i++;
					j++;
				}
				else if (result.get(i).getDocumentId() < qList.get(j).getDocumentId())
					i++;
				else if (result.get(i).getDocumentId() > qList.get(j).getDocumentId())
					j++;
			}
			bufferList = result;
		}
		return result;

	}

	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
