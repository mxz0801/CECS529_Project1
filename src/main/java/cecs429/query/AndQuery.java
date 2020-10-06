package cecs429.query;



import java.util.*;
import java.util.stream.Collectors;

import cecs429.index.Index;
import cecs429.index.Posting;
import javafx.geometry.Pos;

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
	public String toString() {
		return
		 String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
