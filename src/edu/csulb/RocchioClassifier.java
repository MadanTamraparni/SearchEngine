package edu.csulb;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class RocchioClassifier {
	private final String JAY = "JAY";
	private final String HAMILTON = "HAMILTON";
	private final String MADISON = "MADISON";
	private final String PAPER_ID = "paper_52.txt";
	private HashMap<String, List<Double>> mCentroids;
	private DocumentCorpus mDisputedCorpus;
	private Index mJayIndex, mHamiltonIndex, mMadisonIndex;//, mDisputedIndex;
	private Index mIndex;
	private DocumentCorpus mCorpus;
	private List<String> mJayDocs;
	private List<String> mHamiltonDocs;
	private List<String> mMadisonDocs;
	
	
	
	public RocchioClassifier(Index index, DocumentCorpus corpus, DocumentCorpus jayCorpus, DocumentCorpus hamCorpus, 
			DocumentCorpus madCorpus, DocumentCorpus disputedCorpus) {
		
		this.mIndex = index;
		this.mCorpus = corpus;
		mCentroids = new HashMap<>();
		mJayDocs = new ArrayList<String>();
		mHamiltonDocs = new ArrayList<String>();
		mMadisonDocs = new ArrayList<String>();
		Iterable<Document> it = jayCorpus.getDocuments();
		for(Document doc: it)
			mJayDocs.add(doc.getTitle());
		it = hamCorpus.getDocuments();
		for(Document doc: it)
			mHamiltonDocs.add(doc.getTitle());
		it = madCorpus.getDocuments();
		for(Document doc: it)
			mMadisonDocs.add(doc.getTitle());

		mCentroids.put(JAY, findCentroid(mJayDocs));
		mCentroids.put(HAMILTON, findCentroid(mHamiltonDocs));
		mCentroids.put(MADISON, findCentroid(mMadisonDocs));
		this.mDisputedCorpus = disputedCorpus;
	}
	
	public HashMap<String, String> classify()
	{
		HashMap<String, String> disputedDocOwners = new HashMap<>();
		Iterable<Document> it = mDisputedCorpus.getDocuments();
		for(Document doc : it)
		{
			List<Double> docVector = getVector(doc);
			double jayEucDist = calEuclidianDistance(docVector, mCentroids.get(JAY));
			double hamiltonEucDist = calEuclidianDistance(docVector, mCentroids.get(HAMILTON));
			double madisonEucDist = calEuclidianDistance(docVector, mCentroids.get(MADISON));
			//if(doc.getTitle().equals(PAPER_ID))
			System.out.println("JAY = " + jayEucDist + " , HAMILTON = " + hamiltonEucDist + " , MADISON = " + madisonEucDist);
			
			if(jayEucDist < hamiltonEucDist && jayEucDist < madisonEucDist)
				disputedDocOwners.put(doc.getTitle(), JAY);
			else if(hamiltonEucDist < madisonEucDist && hamiltonEucDist < jayEucDist)
				disputedDocOwners.put(doc.getTitle(), HAMILTON);
			else
				disputedDocOwners.put(doc.getTitle(), MADISON);
		}
		
		return disputedDocOwners;
	}
	private List<Double> getVector(Document doc)
	{
		HashMap<String, Double> wdt = new HashMap<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		List<Double> resVector = new ArrayList<Double>();
		double docWeights = 0;
		List<String> vocabList = mIndex.getVocabulary();
		double weight;
		EnglishTokenStream eng = new EnglishTokenStream(doc.getContent());
		Iterable<String> strIter = eng.getTokens();	
		for(String token : strIter)
		{
			List<String> tokenList = processor.enhancedProcessToken(token);
			for(String newToken:tokenList)
			{
				if(wdt.containsKey(newToken)){
					wdt.put(newToken,wdt.get(newToken) + 1);
				}else {
					wdt.put(newToken, 1.0);
				}
			}
		}

		for(Map.Entry<String,Double> entry:wdt.entrySet()){
			weight = 1 + Math.log(entry.getValue());
			entry.setValue(weight);
			docWeights += Math.pow(weight, 2.0);
		}
		
		docWeights = Math.sqrt(docWeights);
		for(String term : vocabList)
			resVector.add(wdt.getOrDefault(term, 0.0)/docWeights);
				
		if(doc.getTitle().equals(PAPER_ID))
		{
			System.out.print("Vector Components = ");
			for(int i=0; i < 30; i++)
				System.out.print(resVector.get(i) + " ");
			System.out.println();
		}
		return resVector;
	}
	
	private double calEuclidianDistance(List<Double> vec1, List<Double> vec2)
	{
		double euclidianDist = 0.0;
		for(int i=0; i < vec1.size(); i++)
		{
			euclidianDist += Math.pow(vec1.get(i) - vec2.get(i), 2);
		}
		euclidianDist = Math.sqrt(euclidianDist);
		return euclidianDist;
	}
	private List<Double> findCentroid(List<String> inList)
	{
		Iterable<Document> it = mCorpus.getDocuments();
		
		List<List<Double> > docVectorList = new ArrayList<List<Double> >();
		List<String> vocabList = mIndex.getVocabulary();
		
		for(Document doc : it)
		{
			if(inList.contains(doc.getTitle()))
			{	
				HashMap<String, Double> wdt = doc.getWdtMap();
				double ld = doc.getLd();
				List<Double> docVector = new ArrayList<>();
				for(String term : vocabList)
					docVector.add(wdt.getOrDefault(term, 0.0)/ld);
			
				docVectorList.add(docVector);
			}
		}
		return calculateCentroid(docVectorList);
	}
	
	private List<Double> calculateCentroid(List<List<Double>> docVectorList)
	{
		int len = docVectorList.get(0).size();
		List<Double> centroid = new ArrayList<Double>();
		for(int i =0; i < len; i++)
		{
			double sum = 0.0;
			for(int j=0; j < docVectorList.size(); j++)
				sum += docVectorList.get(j).get(i);
			
			sum = sum/docVectorList.size();
			centroid.add(sum);
		}
		
		return centroid;
	}
}
