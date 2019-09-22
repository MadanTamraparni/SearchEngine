package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
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
	
		QueryComponent firstQueryComp = mComponents.get(0);		
		List<Posting> firstList = firstQueryComp.getPostings(index);
		for(int j=1; j < mComponents.size(); j++)
		{
			List<Posting> secondList = mComponents.get(j).getPostings(index);
			List<Posting> temp = new ArrayList<Posting>(); 
			int len = Math.min(firstList.size(), secondList.size());
			int x=0, y=0;
			while(x<len || y<len)
			{
				Posting firstPosting = firstList.get(x);
				Posting secondPosting = secondList.get(y);
				if(firstPosting.getDocumentId() == secondPosting.getDocumentId())
				{
					temp.add(firstPosting);
					x++;
					y++;
				}
				else if(firstPosting.getDocumentId() > secondPosting.getDocumentId())
					y++;
				else
					x++;
			}
			firstList = temp;
 		}
		return firstList;
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
