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
import java.util.List;

public class SpimiIndexWriter {
	
	String mPath;
	int mIndexCounter;
	List<String> mFullVocabList;
	public SpimiIndexWriter(String path)
	{
		mPath = path;
		mIndexCounter = 0;
		mFullVocabList = new ArrayList<String>();
	}
	
	public boolean writePartialIndex(Index index)
	{
		DiskIndexWriter diskWriter = new DiskIndexWriter();
		mIndexCounter++;
		mFullVocabList.addAll(index.getVocabulary());
		diskWriter.WriteIndex(index, mPath, new String("/" + Integer.toString(mIndexCounter)));
		return true;
	}
	
	public DiskPositionalIndex mergePartialIndex()
	{
		DiskPositionalIndex finalDiskIndex = new DiskPositionalIndex(mPath);
		RandomAccessFile[] partialIndexFile = new RandomAccessFile[mIndexCounter]; 
		DiskPositionalIndex[] partialIndex = new DiskPositionalIndex[mIndexCounter];		
		for(int i = 1; i < mIndexCounter; i++)
		{
			try {
				partialIndexFile[i] = new RandomAccessFile(new File(mPath + "/postings" + Integer.toString(i) +  ".bin"),"r");
				partialIndex[i] = new DiskPositionalIndex(mPath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		File finalPosting = new File(mPath + "/postings.bin");
		if(finalPosting.exists())
			finalPosting.delete();
		else
			try {
				finalPosting.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		FileOutputStream opStream = null;
		try {
			opStream = new FileOutputStream(finalPosting);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataOutputStream postingbin = new DataOutputStream(opStream);
		long[] startOffset = new long[mIndexCounter+1];
		long[] endOffset = new long[mIndexCounter];
		Arrays.fill(startOffset, 0);
		Arrays.fill(endOffset, 0);
		for(String term : mFullVocabList)
		{
			for(int pIndexer = 0; pIndexer < partialIndex.length; pIndexer++)
			{
				endOffset[pIndexer] = partialIndex[pIndexer].getPostingOffset(term);
				byte[] byteBuffer = new byte[(int)(endOffset[pIndexer] - startOffset[pIndexer])];
				try {
					partialIndexFile[pIndexer].seek(startOffset[pIndexer]);
					partialIndexFile[pIndexer].read(byteBuffer, (int)startOffset[pIndexer], (int)(endOffset[pIndexer] - startOffset[pIndexer]));
					postingbin.write(byteBuffer,(int)startOffset[pIndexer],(int)(endOffset[pIndexer] - startOffset[pIndexer]));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startOffset[pIndexer+1] = endOffset[pIndexer];
			}
			try {
				postingbin.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			postingbin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return finalDiskIndex;
	}
}
