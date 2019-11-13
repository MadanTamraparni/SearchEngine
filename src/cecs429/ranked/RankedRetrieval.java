package cecs429.ranked;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import cecs429.index.Posting;

public class RankedRetrieval {
	
	private List<Double> accumulator = new ArrayList<Double>(); //List of scores for the top k documents
	
	/**Get the top k documents based on the query and the chosen rank model**/
	public List<Posting>getResults(RankModel rankModel, String query, int k) throws IOException{
		HashMap<Integer,Double> Ad = rankModel.rank(query);
		List<Posting> results = new ArrayList<Posting>(); //List of result postings
		
		//Priority Queue
		PriorityQueue<Map.Entry<Integer,Double>> pQueue = new PriorityQueue<Map.Entry<Integer,Double>>(new scoreComparator());
		
		for(Map.Entry<Integer,Double> entry: Ad.entrySet()) {
			pQueue.add(entry); //Add all elements of accumulator into the queue
		}
		int pQueueSize = pQueue.size();
		
		if(pQueueSize < k) {
			k = pQueue.size(); // if queue size is smaller than k ,set k = queue size
		}
		
		for(int i = 0; i < k; i++) {
			int docId = pQueue.poll().getKey();
			results.add(new Posting(docId)); //get top k postings from the Priority Queue and add to the list
			accumulator.add(Ad.get(docId));
		}
		return results;
	}
	
	public List<Posting>getResults(RankModel rankModel, String query) throws IOException{
		return getResults(rankModel, query, 10);
	}
	
	public List<Double> getAcculumator(){
		return accumulator;
	}
}

/**Comparator for accumulator**/
class scoreComparator implements Comparator<Map.Entry<Integer,Double>>{

	@Override
	public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
		if (o1.getValue() > o2.getValue()) {
			return -1;
		}else if(o1.getValue() < o2.getValue()) {
			return 1;
		}else {
			return 0;
		}
	}
	
}
