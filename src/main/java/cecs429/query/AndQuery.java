package cecs429.query;



import java.util.*;
import java.util.stream.Collectors;

import cecs429.index.Index;
import cecs429.index.KgramIndex;
import cecs429.index.Posting;

/**
 * An AndQuery composes other Query objects and merges their postings in an intersection-like operation.
 */
public class AndQuery implements Query {
	private List<Query> mChildren;
	
	public AndQuery(Collection<Query> children) {
		mChildren = new ArrayList<>(children);
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<>();
		List<Posting> bufferList = new ArrayList<>();
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		for(Query q : mChildren){
			if(q == mChildren.get(0)) {
				bufferList = index.getPostings(q.toString());
				continue;
			}
			int i = 0;
			int j = 0;
			while(i < bufferList.size() && j < index.getPostings(q.toString()).size()){
				if(bufferList.get(i).getDocumentId() == index.getPostings(q.toString()).get(j).getDocumentId()) {
					result.add(bufferList.get(i));
					i++;
					j++;
				}
				else if(bufferList.get(i).getDocumentId() < index.getPostings(q.toString()).get(j).getDocumentId())
					i++;
				else  if(bufferList.get(i).getDocumentId() > index.getPostings(q.toString()).get(j).getDocumentId())
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
		for(Query q: mChildren){
			if(q.toString().contains("*") && q == mChildren.get(0)) {
				wildcardQuery.setWildcardLiteral(q.toString());
				bufferList = wildcardQuery.getPostings(index, index2);
				continue;
			}
			else if(q.toString().contains("*") == false && q == mChildren.get(0)){
				bufferList = index.getPostings(q.toString());
				continue;
			}
			int i = 0;
			int j = 0;
			if(q.toString().contains("*")) {
				wildcardQuery.setWildcardLiteral(q.toString());
				qList = wildcardQuery.getPostings(index, index2);
			}
			else
				qList = index.getPostings(q.toString());
			while(i < bufferList.size() && j < qList.size()){
				if(bufferList.get(i).getDocumentId() == qList.get(j).getDocumentId()) {
					result.add(bufferList.get(i));
					i++;
					j++;
				}
				else if(bufferList.get(i).getDocumentId() < qList.get(j).getDocumentId())
					i++;
				else  if(bufferList.get(i).getDocumentId() > qList.get(j).getDocumentId())
					j++;
			}
			bufferList = result;

		}
		return  result;
	}
	@Override
	public String toString() {
		return
		 String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
