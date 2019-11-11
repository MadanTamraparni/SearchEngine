package cecs429.ranked;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class DefaultModel implements RankModel {
	
	private Index mIndex;
	private int mCorpusSize;
	private RandomAccessFile mDocWeightsRaf;
	private TokenProcessor mProcessor;
	
	public DefaultModel(Index index, int corpusSize, RandomAccessFile docWeightsRaf, TokenProcessor processor){
		mIndex = index;
		mCorpusSize = corpusSize;
		mDocWeightsRaf = docWeightsRaf;
		mProcessor = processor;
	}


	@Override
	public HashMap<Integer,Double> rank(String query) throws IOException {
		HashMap<Integer,Double> Ad = new HashMap<Integer,Double>();
		String[] queryTerms = query.split(" ");
		for(String term: queryTerms){
			List<String> tokenList = mProcessor.enhancedProcessToken(term);
			for(String token: tokenList) {
				List<Posting> tokenResults = mIndex.getPostings(token);
				int dft = tokenResults.size();
				if(dft == 0) {
					continue;
				}
				double wqt = Math.log(1+ (mCorpusSize/(double)dft));

				for(Posting posting: tokenResults){
					int docId = posting.getDocumentId();;
					double wdt = posting.getWdt(0); // get default wdt
					if(Ad.containsKey(docId)) {
						Ad.put(docId, Ad.get(docId)+ wdt*wqt);
					}else {
						Ad.put(docId, wdt*wqt);
					}
				}
			}
			
		}
		
		for(Map.Entry<Integer,Double>entry: Ad.entrySet()){
			mDocWeightsRaf.seek(entry.getKey() * 32);
			double docWeights= mDocWeightsRaf.readDouble();
			entry.setValue(entry.getValue()/docWeights);
		}
		return Ad;
	}

}
