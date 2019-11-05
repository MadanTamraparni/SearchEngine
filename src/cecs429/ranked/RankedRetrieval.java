package cecs429.ranked;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.TokenProcessor;

public class RankedRetrieval {
	
	private static Index mIndex;
	private static TokenProcessor mProcessor;
	private static int mCorpusSize;
	private static String mPath; 
	public RankedRetrieval(Index index, TokenProcessor processor, int corpusSize, String path){
		mIndex = index;
		mProcessor = processor;
		mCorpusSize = corpusSize;
		mPath = path + "/docWeights.bin";
	}
	
	public List<Posting> getResults(String query) throws IOException{
		BooleanQueryParser queryParser = new BooleanQueryParser();
		HashMap<Integer,Double> Ad = new HashMap<Integer,Double>();
		File docWeightsFile = new File(mPath);
		RandomAccessFile docWeightsRaf = new RandomAccessFile(docWeightsFile, "r");
		String[] queryTerms = query.split(" ");
		
		
		return null;

	}
	
	private void rankDefault(HashMap<Integer,Double>Ad, BooleanQueryParser queryParser, RandomAccessFile docWeightsRaf, String[] queryTerms) throws IOException{
		for(String term: queryTerms){
			QueryComponent queryComponent = queryParser.parseQuery(term);
			List<Posting> termResults = queryComponent.getPostings(mIndex, mProcessor);
			double wqt = Math.log(1+ (mCorpusSize/termResults.size()));
			
			for(Posting posting: termResults){
				int docId = posting.getDocumentId();;
				double wdt = 0; // NEEDS IMPLEMENTATION
				if(Ad.containsKey(docId)) {
					Ad.put(docId, Ad.get(docId)+ wdt*wqt);
				}else {
					Ad.put(docId, wdt*wqt);
				}
			}
		}
		
		for(Map.Entry<Integer,Double>entry: Ad.entrySet()){
			docWeightsRaf.seek(entry.getKey() * 32);
			double docWeights= docWeightsRaf.readDouble();
			entry.setValue(entry.getValue()/docWeights);
		}
	}
	
	private void rankTfIdf(HashMap<Integer,Double>Ad, BooleanQueryParser queryParser, RandomAccessFile docWeightsRaf, String[] queryTerms) throws IOException{
		for(String term: queryTerms){
			QueryComponent queryComponent = queryParser.parseQuery(term);
			List<Posting> termResults = queryComponent.getPostings(mIndex, mProcessor);
			double wqt = Math.log(mCorpusSize/termResults.size());
			
			for(Posting posting: termResults){
				int docId = posting.getDocumentId();
				double wdt = posting.getPositions().size();
				if(Ad.containsKey(docId)) {
					Ad.put(docId, Ad.get(docId)+ wdt*wqt);
				}else {
					Ad.put(docId, wdt*wqt);
				}
			}
		}
		for(Map.Entry<Integer,Double>entry: Ad.entrySet()){
			docWeightsRaf.seek(entry.getKey() * 32);
			double docWeights= docWeightsRaf.readDouble();
			entry.setValue(entry.getValue()/docWeights);
		}
	}
	
	private void rankBm25(HashMap<Integer,Double>Ad, BooleanQueryParser queryParser, RandomAccessFile docWeightsRaf, String[] queryTerms) throws IOException{
		for(String term: queryTerms){
			QueryComponent queryComponent = queryParser.parseQuery(term);
			List<Posting> termResults = queryComponent.getPostings(mIndex, mProcessor);
			double wqt = Math.max(0.1, Math.log((mCorpusSize - termResults.size() + 0.5) / (termResults.size() + 0.5)));
			
			for(Posting posting: termResults){
				int docId = posting.getDocumentId();
				double wdt = 0; //NEEDS IMPLEMENTATION
				if(Ad.containsKey(docId)) {
					Ad.put(docId, Ad.get(docId)+ wdt*wqt);
				}else {
					Ad.put(docId, wdt*wqt);
				}
			}
		}
	}
	
	private void rankWacky(HashMap<Integer,Double>Ad, BooleanQueryParser queryParser, RandomAccessFile docWeightsRaf, String[] queryTerms) throws IOException{
		for(String term: queryTerms){
			QueryComponent queryComponent = queryParser.parseQuery(term);
			List<Posting> termResults = queryComponent.getPostings(mIndex, mProcessor);
			int dft = termResults.size();
			double wqt = Math.max(0, Math.log((mCorpusSize - dft) / dft));
			
			for(Posting posting: termResults){
				int docId = posting.getDocumentId();
				double wdt = 0; //NEEDS IMPLEMENTATION
				if(Ad.containsKey(docId)) {
					Ad.put(docId, Ad.get(docId)+ wdt*wqt);
				}else {
					Ad.put(docId, wdt*wqt);
				}
			}
		}
		for(Map.Entry<Integer,Double>entry: Ad.entrySet()){
			docWeightsRaf.seek(entry.getKey() * 32 + 16);
			double docByte = docWeightsRaf.readDouble();
			
			entry.setValue(entry.getValue()/ Math.sqrt(docByte));
		}
	}
	

}
