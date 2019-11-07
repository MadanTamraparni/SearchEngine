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
	
	public static void main(String[] args) {
		HashMap<Integer,Double> Ad = new HashMap<Integer,Double>();
		Ad.put(0, 15.2);
		Ad.put(1, 2.3);
		Ad.put(2, 19.2);
		Ad.put(3, 7.8);
		Ad.put(4, 26.2);
		Ad.put(5, 2.3);
		Ad.put(6, 1.1);
		
		PriorityQueue<Map.Entry<Integer,Double>> pQueue = new PriorityQueue<Map.Entry<Integer,Double>>(new scoreComparator());
		for(Map.Entry<Integer,Double> entry: Ad.entrySet()) {
			pQueue.add(entry);
		}
		System.out.println(pQueue.poll().toString());
		System.out.println(pQueue.poll().toString());
		System.out.println(pQueue.poll().toString());
		System.out.println(pQueue.poll().toString());
		System.out.println(pQueue.poll().toString());
		System.out.println(pQueue.poll().toString());
		System.out.println(pQueue.poll().toString());
		
	}
	
	public List<Posting>getResults(RankModel rankModel, String query, int k) throws IOException{
		HashMap<Integer,Double> Ad = rankModel.rank(query);
		List<Posting> results = new ArrayList<Posting>();
		
		PriorityQueue<Map.Entry<Integer,Double>> pQueue = new PriorityQueue<Map.Entry<Integer,Double>>(new scoreComparator());
		
		for(Map.Entry<Integer,Double> entry: Ad.entrySet()) {
			pQueue.add(entry);
		}
		
		for(int i = 0; i < k; i++) {
			results.add(new Posting(pQueue.poll().getKey()));
			
		}
		return results;
	}
	
	public List<Posting>getResults(RankModel rankModel, String query) throws IOException{
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
