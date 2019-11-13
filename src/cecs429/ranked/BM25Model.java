package cecs429.ranked;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class BM25Model implements RankModel {
	
	private Index mIndex;
	private int mCorpusSize;
	private TokenProcessor mProcessor;
	
	public BM25Model(Index index, int corpusSize, TokenProcessor processor){
		mIndex = index;
		mCorpusSize = corpusSize;
		mProcessor = processor;
	}

	@Override
	/**Rank the documents based on the query**/
	public HashMap<Integer, Double> rank(String query) throws IOException {
		HashMap<Integer,Double> Ad = new HashMap<Integer,Double>(); //Accumulator
		String[] queryTerms = query.split(" ");
		for(String term: queryTerms){
			List<String> tokenList = mProcessor.enhancedProcessToken(term); //Normalize each term in the query into list of tokens
			for(String token: tokenList) {
				List<Posting> tokenResults = mIndex.getPostings(token); //Get postings for each token
				int dft = tokenResults.size();
				if(dft == 0) {
					continue;
				}
				double wqt = Math.max(0.1, Math.log((mCorpusSize - tokenResults.size() + 0.5) / (tokenResults.size() + 0.5)));
				
				for(Posting posting: tokenResults){
					int docId = posting.getDocumentId();
					double wdt = posting.getWdt(1); //get BM25 wdt  
					if(Ad.containsKey(docId)) {
						Ad.put(docId, Ad.get(docId)+ wdt*wqt);//if the document already exists in the accumulator
					}else {
						Ad.put(docId, wdt*wqt);
					}
				}
			}
			
		}
		return Ad;
	}

}
