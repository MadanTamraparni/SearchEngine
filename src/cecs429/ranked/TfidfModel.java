package cecs429.ranked;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cecs429.index.Index;
import cecs429.index.Posting;

public class TfidfModel implements RankModel {
	
	private Index mIndex;
	private int mCorpusSize;
	private RandomAccessFile mDocWeightsRaf;
	
	public TfidfModel(Index index, int corpusSize, RandomAccessFile docWeightsRaf){
		mIndex = index;
		mCorpusSize = corpusSize;
		mDocWeightsRaf = docWeightsRaf;
	}

	@Override
	public HashMap<Integer, Double> rank(String query) throws IOException {
		String[] queryTerms = query.split(" ");
		HashMap<Integer,Double> Ad = new HashMap<Integer,Double>();
		for(String term: queryTerms){
			
			List<Posting> termResults = mIndex.getPostings(term);
			double wqt = Math.log(mCorpusSize/termResults.size());
			
			for(Posting posting: termResults){
				int docId = posting.getDocumentId();
				double wdt = posting.getPositions().size(); //get tftd as wdt
				if(Ad.containsKey(docId)) {
					Ad.put(docId, Ad.get(docId)+ wdt*wqt);
				}else {
					Ad.put(docId, wdt*wqt);
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