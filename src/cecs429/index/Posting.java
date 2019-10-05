package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapsulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPositions;
	
	public Posting(int documentId) {
		
		mDocumentId = documentId;
		mPositions = new ArrayList<Integer>();
	}
	
	//add a position 
	public void addPosition(int position){
		mPositions.add(position);
	}
	
	//return the document ID
	public int getDocumentId() {
		return mDocumentId;
	}
	
	//return the positions
	public List<Integer> getPositions(){
		return mPositions;
	}
	
	@Override
	public boolean equals(Object p)
	{
		return (this.mDocumentId == ((Posting)p).mDocumentId);
	}
}
