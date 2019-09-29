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
		List<Posting> firstList = index.getPostings(mTerms.get(0));
		int gap = 1;
		for(int j=1; j < mTerms.size(); j++)
		{
			List<Posting> secondList = index.getPostings(mTerms.get(j));
			List<Posting> temp = new ArrayList<Posting>(); 
			int firstListSize = firstList.size();
			int secondListSize = secondList.size();
			int firstListPtr=0, secondListPtr=0;
			while(firstListPtr<firstListSize && secondListPtr<secondListSize)
			{
				Posting firstPosting = firstList.get(firstListPtr);
				Posting secondPosting = secondList.get(secondListPtr);
				if(secondPosting.getDocumentId() == firstPosting.getDocumentId())
				{
					List<Integer> firstPos = firstPosting.getPositions();
					List<Integer> secondPos = secondPosting.getPositions();
					int firPosPtr = 0, secPosPtr = 0;
					while(firPosPtr < firstPos.size() && secPosPtr < secondPos.size())
					{
						int firPosValue = firstPos.get(firPosPtr);
						int secPosValue = secondPos.get(secPosPtr);
						if(secPosValue - firPosValue == gap)
						{	
							temp.add(firstPosting);
							break;
						}
						if(firPosValue == secPosValue)
						{
							firPosPtr++;
							secPosPtr++;
						}
						else if(firPosValue > secPosValue)
							secPosPtr++;
						else
							firPosPtr++;
					}	
					firstListPtr++;
					secondListPtr++;
				}
				else if(firstPosting.getDocumentId() > secondPosting.getDocumentId())
					secondListPtr++;
				else
					firstListPtr++;
			}	
			firstList = temp;
			gap++;
		}
		return firstList;
	}
	
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
