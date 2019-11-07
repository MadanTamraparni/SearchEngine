package cecs429.ranked;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;

import cecs429.index.Index;
import cecs429.index.Posting;

public interface RankModel {
	HashMap<Integer, Double> rank(String query) throws IOException;
}
