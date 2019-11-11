package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapsulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPositions;
	private List<Double> mWdt;
	
	public Posting(int documentId)
	{
		mDocumentId = documentId;
		mPositions = new ArrayList<Integer>();
		mWdt = new ArrayList<Double>();
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
	
	public double getWdt(int scoreType)
	{
		return mWdt.get(scoreType);
	}
	
	public void addWdt(double wdt)
	{
		mWdt.add(wdt);
	}
	
	public List<Double> getAllWdt()
	{
		return mWdt;
	}
}
