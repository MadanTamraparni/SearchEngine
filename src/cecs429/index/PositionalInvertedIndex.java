package cecs429.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class PositionalInvertedIndex implements Index {

	private HashMap<String, List<Posting>> mPostingMap;
	private HashSet<String> mVocabulary;
	
	public PositionalInvertedIndex()
	{
		mPostingMap = new HashMap<String, List<Posting>>();
		mVocabulary = new HashSet<String>(); 
	}
	
	//return a list of postings for a term
	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> postings = new LinkedList<Posting>();
		
		if(mPostingMap.containsKey(term))
		{
			postings = mPostingMap.get(term);
		}
		return postings;	
	}
	
	//return a list of vocabularies
	@Override
	public List<String> getVocabulary() {
		ArrayList<String> vocabulary = new ArrayList<String>(mVocabulary);
		Collections.sort(vocabulary);
		return vocabulary;
	}
	
	//add a term with its document ID and position 
	public void addTerm(String term, int documentId, int position)
	{
		//if the term already exists in the index
		if(mPostingMap.containsKey(term)){
			List<Posting> list = mPostingMap.get(term);
			if(documentId != list.get(list.size()-1).getDocumentId()){
				list.add(new Posting(documentId));
				list.get(list.size()-1).addPosition(position);
			}else{
				list.get(list.size()-1).addPosition(position);
			}
				
		}
		//if the term doesn't exist in the index
		else{
			List<Posting> postings = new LinkedList<Posting>();
			postings.add(new Posting(documentId));
			postings.get(postings.size()-1).addPosition(position);;
			mPostingMap.put(term,postings);
		}
		//if the term already exist in the vocabulary
		if(mVocabulary.contains(term)){
			return;
		}
		//if the term doesn't exist in the vocabulary
		else{
			mVocabulary.add(term);
		}
	}
}
