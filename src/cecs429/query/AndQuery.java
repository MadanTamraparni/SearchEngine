package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;
	
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		
		
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		TermLiteral firstLiteral = (TermLiteral) mComponents.get(0);
		List<Posting> result = index.getPostings(firstLiteral.getTerm());
		for(int j=1; j < mComponents.size(); j++)
		{
			
			TermLiteral secondLiteral = (TermLiteral) mComponents.get(j);	
			List<Posting> nextPosting = index.getPostings(firstLiteral.getTerm());
			
			for(Posting p : result)
			{
				if(!nextPosting.contains(p.getDocumentId()))
				{
					result.remove(p);
				}
			}
			
 		}
		return result;
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
