package cecs429.query;

import java.util.List;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class ParanthesisLiteral implements QueryComponent {
    private String query;
    private boolean mIsNegative;
    public ParanthesisLiteral(String queryIn, boolean isNegative){
    	System.out.println("IN QUERY = " + queryIn);
    	mIsNegative = isNegative;
    	int endIndex = queryIn.length();
    	endIndex--;
    	while(queryIn.charAt(endIndex) == ')')
    		endIndex--;
        query = queryIn.substring(0, endIndex+1);
        System.out.println("Q = " + query);
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        BooleanQueryParser queryParser = new BooleanQueryParser();
        QueryComponent queryComponent = queryParser.parseQuery(query);
        for (Posting p : queryComponent.getPostings(index, processor)) {
            //int doc = p.getDocumentId();
            //doc++;
            //System.out.println("Document ID \"article" + doc + ".json\"");
            // Below print line only for tracing the index
            System.out.println(p.getDocumentId());
            //System.out.println("Title: " + corpus.getDocument(p.getDocumentId()).getTitle());
        }
        return queryComponent.getPostings(index, processor);
    }

    @Override
    public boolean isNegative() {
        // TODO Auto-generated method stub
        return mIsNegative;
    }
    
}