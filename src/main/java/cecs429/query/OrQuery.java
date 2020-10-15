package cecs429.query;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.index.Index;
import cecs429.index.KgramIndex;
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
		List<Posting> result = new ArrayList<>();

		// TODO: program the merge for an OrQuery, by gathering the postings of the composed Query children and
		// unionizing the resulting postings.
		for(Query q : mChildren){
			if(q == mChildren.get(0)) {
				result = q.getPostings(index);
				continue;
			}
			List<Posting> bufferList = new ArrayList<>();
			int i = 0;
			int j = 0;
			while(i <= result.size() && j <= q.getPostings(index).size()){
				if(i == result.size()){
					for(; j< index.getPostings(q.toString()).size(); j++)
						bufferList.add(index.getPostings(q.toString()).get(j));
					break;
				}
				else if(j == q.getPostings(index).size()){
					for(; i< result.size(); i++)
						bufferList.add(result.get(i));
					break;
				}

				if(result.get(i).getDocumentId() == q.getPostings(index).get(j).getDocumentId()) {
					bufferList.add(result.get(i));
					i++;
					j++;
				}
				else if(result.get(i).getDocumentId() < q.getPostings(index).get(j).getDocumentId()) {
					bufferList.add(result.get(i));
					i++;
				}
				else  if(result.get(i).getDocumentId() > q.getPostings(index).get(j).getDocumentId()) {
					bufferList.add(index.getPostings(q.toString()).get(j));
					j++;
				}
			}
			result = new ArrayList<>(bufferList);
		}
		return result;
	}
	@Override
	public List<Posting> getPostings(Index index, KgramIndex index2){
		return  null;
	}
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
