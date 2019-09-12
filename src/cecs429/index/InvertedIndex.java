package cecs429.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InvertedIndex implements Index {

	private HashMap<String, List<Posting>> mPostingMap;
	private List<String> mVocabulary;
	
	public InvertedIndex()
	{
		mPostingMap = new HashMap<String, List<Posting>>();
		mVocabulary = new ArrayList<String>(); 
	}
	
	@Override
	public List<Posting> getPostings(String term) {
		System.out.println("Term = " + term);
		List<Posting> postings = new LinkedList<Posting>();
		
		if(mPostingMap.containsKey(term))
		{
			postings = mPostingMap.get(term);
		}
		return postings;	
	}

	@Override
	public List<String> getVocabulary() {
		
		Collections.sort(mVocabulary);
		return mVocabulary;
	}
	
	public void addTerm(String term, int documentId)
	{
		if(mPostingMap.containsKey(term))
		{
			List<Posting> list = mPostingMap.get(term);
			if(documentId > list.get(list.size()-1).getDocumentId())
				list.add(new Posting(documentId));		
		}
		else
		{
			List<Posting> postings = new LinkedList<Posting>();
			postings.add(new Posting(documentId));
			mPostingMap.put(term,postings);
		}
		if(mVocabulary.contains(term))
			return;
		else
			mVocabulary.add(term);
	}
}
