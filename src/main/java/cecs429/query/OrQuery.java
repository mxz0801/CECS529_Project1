package cecs429.query;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.index.Index;
import cecs429.index.Posting;

/**
 * An OrQuery composes other Query objects and merges their postings with a union-type operation.
 */
public class OrQuery implements Query {
	// The components of the Or query.
	private List<Query> mChildren;
	
	public OrQuery(Collection<Query> children) {
		mChildren = new ArrayList<>(children);
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = null;

		// TODO: program the merge for an OrQuery, by gathering the postings of the composed Query children and
		// unioning the resulting postings.

		for(Query q : mChildren){
			if(q == mChildren.get(0)) {
				result = index.getPostings(q.toString());
				continue;
			}
			List<Posting> bufferList = new ArrayList<>(result);
			result.clear();
			int i = 0;
			int j = 0;
			while(i <= bufferList.size() && j <= index.getPostings(q.toString()).size()){
				if(i == bufferList.size()){
					for(; j< index.getPostings(q.toString()).size(); j++)
						result.add(index.getPostings(q.toString()).get(j));
					break;
				}
				else if(j == index.getPostings(q.toString()).size()){
					for(; i< bufferList.size(); i++)
						result.add(bufferList.get(i));
					break;
				}

				if(bufferList.get(i).getDocumentId() == index.getPostings(q.toString()).get(j).getDocumentId()) {
					result.add(bufferList.get(i));
					i++;
					j++;
				}
				else if(bufferList.get(i).getDocumentId() < index.getPostings(q.toString()).get(j).getDocumentId()) {
					result.add(bufferList.get(i));
					i++;
				}
				else  if(bufferList.get(i).getDocumentId() > index.getPostings(q.toString()).get(j).getDocumentId()) {
					result.add(index.getPostings(q.toString()).get(j));
					j++;
				}
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
