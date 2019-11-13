package cecs429.ranked;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class TfidfModel implements RankModel {
	
	private Index mIndex;
	private int mCorpusSize;
	private RandomAccessFile mDocWeightsRaf;
	private TokenProcessor mProcessor;
	
	public TfidfModel(Index index, int corpusSize, RandomAccessFile docWeightsRaf, TokenProcessor processor){
		mIndex = index;
		mCorpusSize = corpusSize;
		mDocWeightsRaf = docWeightsRaf;
		mProcessor = processor;
	}

	@Override
	/**Rank the documents based on the query**/
	public HashMap<Integer, Double> rank(String query) throws IOException {
		String[] queryTerms = query.split(" ");
		HashMap<Integer,Double> Ad = new HashMap<Integer,Double>(); //Accumulator
		
		for(String term: queryTerms){
			List<String> tokenList = mProcessor.enhancedProcessToken(term);//Normalize each term in the query into list of tokens
			for(String token: tokenList) {
				List<Posting> tokenResults = mIndex.getPostingsWithPositions(token);//Get postings for each token
				int dft = tokenResults.size();
				if(dft == 0) {
					continue;
				}
				double wqt = Math.log(mCorpusSize/(double)tokenResults.size());
				
				for(Posting posting: tokenResults){
					int docId = posting.getDocumentId();
					double wdt = posting.getPositions().size(); //get tftd as wdt
					if(Ad.containsKey(docId)) {
						Ad.put(docId, Ad.get(docId)+ wdt*wqt);//if the document already exists in the accumulator
					}else {
						Ad.put(docId, wdt*wqt);
					}
				}
			}
			
		}
		for(Map.Entry<Integer,Double>entry: Ad.entrySet()){
			mDocWeightsRaf.seek(entry.getKey() * 32);
			double docWeights= mDocWeightsRaf.readDouble();//Get Ld
			entry.setValue(entry.getValue()/docWeights);//Divide accumulaotr by Ld
		}
		return Ad;
	}

}
