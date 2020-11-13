package cecs429.query;


import cecs429.index.Index;
import cecs429.index.KgramIndex;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

		int gapOfDoc = 1; // use it to find the position
		for (String s : mTerms) {
			if (s.equals(mTerms.get(0))) {
				result = index.getPostings(s);
				continue;
			}
			int i = 0;
			int j = 0;

			List<Posting> bufferList = new ArrayList<>();
			// compare the docID in each two terms
			ArrayList<Posting> sPosting = new ArrayList<>(index.getPostings(s));
			while (i < result.size() && j < sPosting.size()) {
				//find the same docID
				if (result.get(i).getDocumentId() == sPosting.get(j).getDocumentId()) {
					int m = 0;
					int n = 0;
					// compare the position
					ArrayList<Integer> positionList = new ArrayList<>();
					Posting p = new Posting(result.get(i).getDocumentId(), positionList);
					while (m < result.get(i).getPosition().size() && n < sPosting.get(j).getPosition().size()) {
						//find the position with phrase's order
						if (sPosting.get(j).getPosition().get(n) == result.get(i).getPosition().get(m) + gapOfDoc) {
							positionList.add(result.get(i).getPosition().get(m));
							break;
						}
						else if (sPosting.get(j).getPosition().get(n) <= result.get(i).getPosition().get(m))
							n++;
						else if (sPosting.get(j).getPosition().get(n) > result.get(i).getPosition().get(m))
							m++;
					}
					if(positionList.size() != 0)
						bufferList.add(p);
					i++;
					j++;
				}
				else if (result.get(i).getDocumentId() < sPosting.get(j).getDocumentId())
					i++;
				else if (result.get(i).getDocumentId() > sPosting.get(j).getDocumentId())
					j++;
			}
			gapOfDoc++;
			result = new ArrayList<>(bufferList);
		}
		return result;
	}

	@Override
	public List<Posting> getPostings(Index index, KgramIndex index2){
		WildcardLiteral wildcardQuery = new WildcardLiteral();
		List<Posting> result = new ArrayList<>();
		List<Posting> qList;
		int gapOfDoc = 1;
		for(String s : mTerms){
			if(s.contains("*") && s.equals(mTerms.get(0))) {
				wildcardQuery.setWildcardLiteral(s);
				result = wildcardQuery.getPostings(index, index2);
				continue;
			}
			else if(!s.contains("*") && s.equals(mTerms.get(0))){
				result = index.getPostings(s);
				continue;
			}
			int i = 0;
			int j = 0;
			List<Posting> bufferList = new ArrayList<>();
			if(s.contains("*")) {
				wildcardQuery.setWildcardLiteral(s);
				qList = wildcardQuery.getPostings(index, index2);
			}
			else
				qList = index.getPostings(s);
			// compare the docID in each two terms
			while (i < result.size() && j < qList.size()) {
				//find the same docID
				if (result.get(i).getDocumentId() == qList.get(j).getDocumentId()) {
					int m = 0;
					int n = 0;
					// compare the position
					ArrayList<Integer> positionList = new ArrayList<>();
					Posting p = new Posting(result.get(i).getDocumentId(), positionList);
					while (m < result.get(i).getPosition().size() && n < qList.get(j).getPosition().size()) {
						//find the position with phrase's order
						if (qList.get(j).getPosition().get(n) == result.get(i).getPosition().get(m) + gapOfDoc) {
							positionList.add(result.get(i).getPosition().get(m));
							m++;
							n++;
						}
						else if (qList.get(j).getPosition().get(n) <= result.get(i).getPosition().get(m))
							n++;
						else if (qList.get(j).getPosition().get(n) > result.get(i).getPosition().get(m))
							m++;
					}
					if(positionList.size() != 0)
						bufferList.add(p);
					i++;
					j++;

				}
				else if (result.get(i).getDocumentId() < qList.get(j).getDocumentId())
					i++;
				else if (result.get(i).getDocumentId() > qList.get(j).getDocumentId())
					j++;
			}
			gapOfDoc++;
			result = new ArrayList<>(bufferList);
		}
		return result;

	}

	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
