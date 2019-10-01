package cecs429.query;

import java.util.List;

import cecs429.documents.DirectoryCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class NotQuery implements QueryComponent{
	private QueryComponent mComponent;
	public NotQuery(QueryComponent component){
		mComponent = component;
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
		QueryComponent literal = mComponent;
		List<Posting> result = literal.getPostings(index, processor);
		return result;
	}

	@Override
	public boolean isNegative() {
		return true;
	}

}
