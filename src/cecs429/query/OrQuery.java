package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
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
	public List<Posting> getPostings(Index index, TokenProcessor processor) {		
		// TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
		// unioning the resulting postings.
		
		QueryComponent firstQueryComp = mComponents.get(0);		
		List<Posting> firstList = firstQueryComp.getPostings(index, processor);
		for(int j=1; j < mComponents.size(); j++)
		{
			List<Posting> secondList = mComponents.get(j).getPostings(index, processor);
			List<Posting> temp = new ArrayList<Posting>(); 
//			int len = Math.min(firstList.size(), secondList.size());
			int x=0, y=0;
			while(x<firstList.size() && y < secondList.size())
			{
				Posting firstPosting = firstList.get(x);
				Posting secondPosting = secondList.get(y);
				if(firstPosting.getDocumentId() > secondPosting.getDocumentId())
				{
					temp.add(secondPosting);	
					y++;
					if(firstPosting.getDocumentId() == secondPosting.getDocumentId())
						x++;
				}
				else
				{
					temp.add(firstPosting);
					x++;
					if(firstPosting.getDocumentId() == secondPosting.getDocumentId())
						y++;
				}
			}
			if(x == firstList.size())
			{
				while(y < secondList.size()) {
				
					temp.add(secondList.get(y));
					y++;
				}
					
			}
			else if(y == secondList.size())
			{
				while(x < firstList.size())
				{
					temp.add(firstList.get(x));
					x++;
				}
			}
			firstList = temp;
 		}
		return firstList;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}

	@Override
	public boolean isNegative() {
		// TODO Auto-generated method stub
		return false;
	}
}
