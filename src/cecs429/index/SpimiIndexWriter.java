package cecs429.index;

import java.util.List;

public class SpimiIndexWriter {
	
	String mPath;
	int mIndexCounter;
	public SpimiIndexWriter(String path)
	{
		mPath = path;
		mIndexCounter = 0;
	}
	
	public boolean writePartialIndex(Index index)
	{
		DiskIndexWriter diskWriter = new DiskIndexWriter();
		mIndexCounter++;
		diskWriter.WriteIndex(index, mPath, mIndexCounter);
		return true;
	}
	
	public boolean mergePartialIndex(Index index)
	{
		List<String> vocabList = index.getVocabulary();
		for(String term : vocabList)
		{
			
		}
		return true;
	}
	
}
