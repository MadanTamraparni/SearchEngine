package cecs429.ranked;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;

import cecs429.index.Index;
import cecs429.index.Posting;

public class BM25Model implements RankModel {
	
	private Index mIndex;
	private int mCorpusSize;
	private RandomAccessFile mDocWeightsRaf;
	
	public BM25Model(Index index, int corpusSize, RandomAccessFile docWeightsRaf){
		mIndex = index;
		mCorpusSize = corpusSize;
		mDocWeightsRaf = docWeightsRaf;
	}

	@Override
	public HashMap<Integer, Double> rank(String query) throws IOException {
		HashMap<Integer,Double> Ad = new HashMap<Integer,Double>();
		String[] queryTerms = query.split(" ");
		for(String term: queryTerms){
			List<Posting> termResults = mIndex.getPostings(term);
			double wqt = Math.max(0.1, Math.log((mCorpusSize - termResults.size() + 0.5) / (termResults.size() + 0.5)));
			
			for(Posting posting: termResults){
				int docId = posting.getDocumentId();
				double wdt = posting.getWdt(1); //get BM25 wdt  
				if(Ad.containsKey(docId)) {
					Ad.put(docId, Ad.get(docId)+ wdt*wqt);
				}else {
					Ad.put(docId, wdt*wqt);
				}
			}
		}
		return Ad;
	}

}
