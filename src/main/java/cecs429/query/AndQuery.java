package cecs429.query;


import cecs429.index.Index;
import cecs429.index.KgramIndex;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		for(Query q : mChildren){
			if(q == mChildren.get(0)) {
				result = index.getPostings(q.toString());
				continue;
			}
			int i = 0;
			int j = 0;
			List<Posting> bufferList = new ArrayList<>();
			while(i < result.size() && j < index.getPostings(q.toString()).size()){
				if(result.get(i).getDocumentId() == index.getPostings(q.toString()).get(j).getDocumentId()) {
					bufferList.add(result.get(i));
					i++;
					j++;
				}
				else if(result.get(i).getDocumentId() < index.getPostings(q.toString()).get(j).getDocumentId())
					i++;
				else  if(result.get(i).getDocumentId() > index.getPostings(q.toString()).get(j).getDocumentId())
					j++;
			}
			result = new ArrayList<>(bufferList);
		}
		return result;
	}

	@Override
	public List<Posting> getPostings(Index index, KgramIndex index2){
		WildcardLiteral wildcardQuery = new WildcardLiteral();
		List<Posting> result = new ArrayList<>();
		List<Posting> qList;
		for(Query q: mChildren){
			if(q.toString().contains("*") && q == mChildren.get(0)) {
				wildcardQuery.setWildcardLiteral(q.toString());
				result = wildcardQuery.getPostings(index, index2);
				continue;
			}
			else if(!q.toString().contains("*") && q == mChildren.get(0)){
				result = index.getPostings(q.toString());
				continue;
			}
			List<Posting> bufferList = new ArrayList<>();
			int i = 0;
			int j = 0;
			if(q.toString().contains("*")) {
				wildcardQuery.setWildcardLiteral(q.toString());
				qList = wildcardQuery.getPostings(index, index2);
			}
			else
				qList = index.getPostings(q.toString());
			while(i < result.size() && j < qList.size()){
				if(result.get(i).getDocumentId() == qList.get(j).getDocumentId()) {
					bufferList.add(result.get(i));
					i++;
					j++;
				}
				else if(result.get(i).getDocumentId() < qList.get(j).getDocumentId())
					i++;
				else  if(result.get(i).getDocumentId() > qList.get(j).getDocumentId())
					j++;
			}
			result = new ArrayList<>(bufferList);
		}
		return  result;
	}
	@Override
	public String toString() {
		return
		 String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
