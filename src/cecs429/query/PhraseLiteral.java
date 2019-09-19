package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
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
		List<Posting> postingList = new ArrayList<Posting>();
		List<Posting> firstList = index.getPostings(mTerms.get(0));
		Iterator firstIterator = firstList.iterator();
		for(int gap=1; gap < mTerms.size(); gap++)
		{
			List<Posting> compareList = index.getPostings(mTerms.get(gap));
			Iterator compIterator = compareList.iterator();
			while(firstIterator.hasNext() && compIterator.hasNext())
			{
				Posting first = (Posting) firstIterator.next();
				Posting compare = (Posting) compIterator.next();
				List<Integer> firstPositions = first.getPositions();
				List<Integer> compPositions = compare.getPositions();
				int len = Math.min(firstPositions.size(), compPositions.size());
				for(int i = 0, j=0; i < len && j < len; i++, j++)
				{
					if(compPositions.get(j) - firstPositions.get(i) == gap)
					{
						postingList.add(first);
					}
				}
			}
			firstList = postingList;
		}
		return postingList;
	}
	//private void phraseMerge(List<Posting> resList, List<Posting> currList, int gap)
	
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
