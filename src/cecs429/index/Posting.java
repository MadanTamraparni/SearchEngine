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
	
	public void addPosition(int position){
		mPositions.add(position);
	}
	public int getDocumentId() {
		return mDocumentId;
	}
	
	public List<Integer> getPositions(){
		return mPositions;
	}
}
