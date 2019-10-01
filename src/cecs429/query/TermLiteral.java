package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	private boolean mIsNegative;
	
	public TermLiteral(String term, boolean isNegative) {
		mTerm = term;
		mIsNegative = isNegative;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
		return index.getPostings(processor.enhancedProcessToken(mTerm).get(0));
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
	
	@Override
	public boolean isNegative(){
		return mIsNegative;
	}
}
