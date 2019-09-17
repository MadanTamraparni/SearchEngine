package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		
		
		// TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
		// unioning the resulting postings.
		
		TermLiteral firstLiteral = (TermLiteral) mComponents.get(0);
		List<Posting> result = index.getPostings(firstLiteral.getTerm());
		
		for(int i=1; i < mComponents.size(); i++)
		{
			
			TermLiteral secondLiteral = (TermLiteral) mComponents.get(i);	
			List<Posting> nextPosting = index.getPostings(firstLiteral.getTerm());
			
			for(Posting p : nextPosting)
			{
				if(!result.contains(p))
					result.add(p);
			}			
 		}
		return result;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
