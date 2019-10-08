package cecs429.query;

import java.util.ArrayList;
import java.util.List;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class NearLiteral implements QueryComponent {
    private String term1, term2;
    private int k;
    private boolean mNegative;
    public NearLiteral(String term1, int k, String term2, boolean mNegative){
        this.term1 = term1;
        this.term2 = term2;
        this.k = k;
        this.mNegative = mNegative;
    }



    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        // TODO Auto-generated method stub
        
        // Creating two posting list for each terms
        List<Posting> postingTerm1 = index.getPostings(processor.enhancedProcessToken(term1).get(0));
        List<Posting> postingTerm2 = index.getPostings(processor.enhancedProcessToken(term2).get(0));

        
        List<Posting> Union = new ArrayList<Posting>();
        // pointer to go through posting list of two given terms
        int pt1 = 0, pt2 = 0, size1 = postingTerm1.size(), size2 = postingTerm2.size();
        
        // finding a union list that both doc appear
        while(pt1 < size1 && pt2 < size2){
            Posting tempPosting1 = postingTerm1.get(pt1);
            Posting tempPosting2 = postingTerm2.get(pt2);
            // check if term1 doc equal term2 doc
            if(tempPosting1.getDocumentId() == tempPosting2.getDocumentId()){
                // nearPosition check if term1 and term2 posion equal to k
                if(nearPosition(tempPosting1.getPositions(), tempPosting2.getPositions(), k)) {
                	Union.add(tempPosting1);
                }
                pt1++;
                pt2++;
            }
            // Increase smaller document ID of two posting list
            else if(tempPosting1.getDocumentId() < tempPosting2.getDocumentId()) pt1++;
            else pt2++;
        }
        return Union;
    }

    // Check for up to k position of two terms with same document
    private boolean nearPosition(List<Integer> termPosition1, List<Integer> termPosition2, int k){
        int lo1 = 0, lo2 = 0, hi1 = termPosition1.size(), hi2 = termPosition2.size(), diff =-1;
        while(lo1 < hi1 && lo2 < hi2){
            int pt1 = termPosition1.get(lo1), pt2 = termPosition2.get(lo2);
           
            if(pt1 > pt2)
            {
            	lo2++;
            	continue;
            }
            else
            	diff = pt2 - pt1;
            
            if(diff >= 0 && diff <= k) return true;
            else if(pt1 < pt2)
            	lo1++;
            else
            	lo2++;
        
        }
        return false;
        
    }



	@Override
	public boolean isNegative() {
		// TODO Auto-generated method stub
		return mNegative;
	}

}