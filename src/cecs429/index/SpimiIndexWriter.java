package cecs429.index;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import cecs429.ranked.TfidfModel;

public class SpimiIndexWriter {
	
	private String mPath;
	private int mIndexCounter;
	private BTreeMap<String,Long> mBPlus;
	private List<String> mFullVocabList;
	
	public SpimiIndexWriter(String path)
	{
		mPath = path;
		mIndexCounter = 0;
		mFullVocabList = new ArrayList<String>();
	}
	
	public boolean writePartialIndex(Index index)
	{
		DiskIndexWriter diskWriter = new DiskIndexWriter();
		diskWriter.WriteIndex(index, mPath, mIndexCounter);
		mIndexCounter++;
		return true;
	}
	
	public DiskPositionalIndex mergePartialIndex(Index index)
	{
		DiskPositionalIndex[] partialIndex = new DiskPositionalIndex[mIndexCounter];		
		for(int i = 0; i < mIndexCounter; i++)
		{
			partialIndex[i] = new DiskPositionalIndex(mPath, i);
			mFullVocabList.addAll(partialIndex[i].getVocabulary());
		}

		File finalPosting = new File(mPath + "/postings.bin");
		if(finalPosting.exists())
			finalPosting.delete();
		try {
			finalPosting.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileOutputStream opStream = null;
		try {
			opStream = new FileOutputStream(finalPosting,true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataOutputStream postingbin = new DataOutputStream(opStream);
		DB db = DBMaker.fileDB(mPath + "/bPlus.db").make();
		
		mBPlus = db.treeMap("map")
			    .keySerializer(Serializer.STRING)
			    .valueSerializer(Serializer.LONG)
			    .counterEnable()
			    .createOrOpen();
		    
		long postingOffset = 0;
		Collections.sort(mFullVocabList); //No priority queue and assumed that vocab fits in the memory
		for(String term : mFullVocabList)
		{
			int dft = 0;
			mBPlus.put(term, postingOffset);			
			List<Posting> postings = new ArrayList<Posting>();
			for(int i=0; i < partialIndex.length; i++)
			{
				int tempDft = partialIndex[i].getdft(term);
				dft = dft + tempDft;
				List<Posting> result = partialIndex[i].getPostingsWithPositions(term);
				if(result == null)
					continue;
				postings.addAll(result);
			}			
			try 
			{								
				postingbin.writeInt(dft);
				postingOffset += 4;
				int currentDoc = 0;
				for(int d=0; d < postings.size(); d++)
				{
					Posting posting = postings.get(d);
					if(d == 0)
					{
						currentDoc = posting.getDocumentId();
						postingbin.writeInt(posting.getDocumentId());
						postingOffset += 4;
					}
					else
					{
						postingbin.writeInt(posting.getDocumentId() - currentDoc);
						postingOffset += 4;
						currentDoc = posting.getDocumentId();
					}
					List<Double> wdts = posting.getAllWdt();
					for(Double w : wdts)
					{
						postingbin.writeDouble(w);
						postingOffset += 8;
					}
					List<Integer> postPositions = posting.getPositions();
					int currentPos = 0;
					int tftd = postPositions.size();
					postingbin.writeInt(tftd);
					postingOffset += 4;
					for(int i=0; i < tftd; i++)
					{
						int postingPos = postPositions.get(i);
						if(i == 0)
						{
							currentPos = postingPos;
							postingbin.writeInt(postingPos);
							postingOffset += 4;
						}
						else
						{
							postingbin.writeInt(postingPos - currentPos);
							currentPos = postingPos;
							postingOffset += 4;
						}
					}					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			postingbin.flush();
			postingbin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mBPlus.close();
		DiskPositionalIndex finalDiskIndex = new DiskPositionalIndex(mPath);
		return finalDiskIndex;
	}
	
}
