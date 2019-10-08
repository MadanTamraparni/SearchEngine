package cecs429.query;

import java.util.ArrayList;
import java.util.List;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class NearLiteral implements QueryComponent {
    private String term1, term2;
    private int k;
    private boolean imNegative;
    public NearLiteral(String term1, int k, String term2, boolean imNegative){
        this.term1 = term1;
        this.term2 = term2;
        this.k = k;
        this.imNegative = imNegative;
    }



    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        // TODO Auto-generated method stub
        
        // Creating two posting list for each terms
        List<Posting> postingTerm1 = index.getPostings(processor.enhancedProcessToken(term1).get(0));
        List<Posting> postingTerm2 = index.getPostings(processor.enhancedProcessToken(term2).get(0));
        System.out.println(processor.enhancedProcessToken(term1).get(0));
        System.out.println(processor.enhancedProcessToken(term2).get(0));

        // finding a union list that both doc appear
        List<Posting> Union = new ArrayList<Posting>();
        int pt1 = 0, pt2 = 0, size1 = postingTerm1.size(), size2 = postingTerm2.size();
        while(pt1 < size1 && pt2 < size2){
            Posting tempPosting1 = postingTerm1.get(pt1);
            Posting tempPosting2 = postingTerm2.get(pt2);
            // check if term1 doc equal term2 doc
            if(tempPosting1.getDocumentId() == tempPosting2.getDocumentId()){
                // nearPosition check if term1 and term2 posion equal to k
                if(nearPosition(tempPosting1.getPositions(), tempPosting2.getPositions(), k)) Union.add(tempPosting1);
                pt1++;
                pt2++;
            }
            // Increase smaller document ID
            else if(tempPosting1.getDocumentId() < tempPosting2.getDocumentId()) pt1++;
            else pt2++;
        }
        return Union;
    }

    // Check for up to k position of two terms with same document
    private static boolean nearPosition(List<Integer> termPosition1, List<Integer> termPosition2, int k){
        int lo1 = 0, lo2 = 0, hi1 = termPosition1.size(), hi2 = termPosition2.size();
        while(lo1 < hi1 && lo2 < hi2){
            int pt1 = termPosition1.get(lo1), pt2 = termPosition2.get(lo2);
            int diff = pt2 - pt1;
            if(diff <= k) return true;
            else lo1++;
        }
        return false;
    }



	@Override
	public boolean isNegative() {
		// TODO Auto-generated method stub
		return imNegative;
	}

}