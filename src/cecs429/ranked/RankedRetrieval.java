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
	
	public static List<Posting>getResults(RankModel rankModel, String query, int k) throws IOException{
		HashMap<Integer,Double> Ad = rankModel.rank(query);
		List<Posting> results = new ArrayList<Posting>();
		
		PriorityQueue<Map.Entry<Integer,Double>> pQueue = new PriorityQueue<Map.Entry<Integer,Double>>(new scoreComparator());
		
		for(Map.Entry<Integer,Double> entry: Ad.entrySet()) {
			pQueue.add(entry);
		}
		int pQueueSize = pQueue.size();
		
		if(pQueueSize < k) {
			k = pQueue.size();
		}
		
		for(int i = 0; i < k; i++) {
			results.add(new Posting(pQueue.poll().getKey()));
		}
		return results;
	}
	
	public static List<Posting>getResults(RankModel rankModel, String query) throws IOException{
		return getResults(rankModel, query, 10);
	}
}


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
