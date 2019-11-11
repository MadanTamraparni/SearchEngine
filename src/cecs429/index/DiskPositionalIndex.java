package cecs429.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import org.mapdb.*;

public class DiskPositionalIndex implements Index{
	private String mPath;
	private RandomAccessFile mPostingList;
	private BTreeMap<String,Long> mBPlus;
	
	public DiskPositionalIndex(String path, int counter)
	{
		mPath = path;
		initialize(mPath + "\\partialIndex" + "\\postings" +  Integer.toString(counter) + ".bin",
				mPath + "\\partialIndex" + "\\bPlus" + Integer.toString(counter) + ".db");
	}
	public DiskPositionalIndex(String path)
	{
		mPath = path;
		initialize(mPath + "\\postings.bin", mPath + "\\bPlus.db");
	}
	
	private void initialize(String postingFileName, String bPlusFileName)
	{
		DB db = DBMaker.fileDB(bPlusFileName).make();

		mBPlus = db.treeMap("map")
			.keySerializer(Serializer.STRING)
			.valueSerializer(Serializer.LONG)
			.counterEnable()
			.open();	

		try {
			mPostingList = new RandomAccessFile(postingFileName, "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> postings = new ArrayList<Posting>();
		if(!mBPlus.containsKey(term))
			return postings;
		
		
		long position = mBPlus.get(term);
		try 
		{
			mPostingList.seek(position);
			int numOfDocs = mPostingList.readInt();
			position += 4;
			int prevDocId = 0;
			for(int i=0; i < numOfDocs; i++)
			{
				mPostingList.seek(position);
				int docId = mPostingList.readInt();
				Posting posting = null;
				if(i == 0)
				{
					 posting = new Posting(docId);
					 prevDocId = docId;
				}
				else
				{
					posting = new Posting(prevDocId + docId);
					prevDocId += docId;
				}
				position += 4;
				for(int x = 0; x < 3; x++)
				{
					mPostingList.seek(position);
					posting.addWdt(mPostingList.readDouble());
					position += 8;
				}
				postings.add(posting);
				mPostingList.seek(position);
				int tftd = mPostingList.readInt();
				position += 4;
				position += (tftd * 4);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		// TODO Auto-generated method stub
		return postings;
	}

	@Override
	public List<String> getVocabulary() {
		return new ArrayList<String>(mBPlus.keySet());
	}
	
	@Override
	public List<Posting> getPostingsWithPositions(String term) {
		List<Posting> postings = new ArrayList<Posting>();
		if(!mBPlus.containsKey(term))
			return postings;
		
		
		long position = mBPlus.get(term);		
		try {
			mPostingList.seek(position);
			int numOfDocs = mPostingList.readInt(); //dft
			position += 4;
			int prevDocId = 0;
			for(int i=0; i < numOfDocs; i++)
			{
				mPostingList.seek(position);
				int docId = mPostingList.readInt();
				Posting posting = null;
				if(i == 0)
				{
					 posting = new Posting(docId);
					 prevDocId = docId;
				}
				else
				{
					posting = new Posting(prevDocId + docId);
					prevDocId += docId;
				}
				position += 4;
				for(int x = 0; x < 3; x++)
				{
					mPostingList.seek(position);
					posting.addWdt(mPostingList.readDouble());
					position += 8;
				}
				
				mPostingList.seek(position);
				int tftd = mPostingList.readInt();
				position += 4;
				int prevPosition = 0;
				for(int j=0; j < tftd; j++)
				{
					if(mPostingList.length() < position+4)
					{
						System.out.println("Posting = " + mPostingList.length() + " TFTD = " + tftd);
					}
					mPostingList.seek(position);
					int pos = mPostingList.readInt();
					if(j==0)
					{
						posting.addPosition(pos);
						prevPosition = pos;
					}
					else
					{
						posting.addPosition(prevPosition+pos);
						prevPosition += pos;
					}
					position += 4;
				}
				postings.add(posting);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return postings;
	}
	public int getdft(String term)
	{
		if(!mBPlus.containsKey(term))
			return 0;
		long position = mBPlus.get(term);
		
		int dft = 0;
		try {
			mPostingList.seek(position);
			dft = mPostingList.readInt();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dft;
	}
	@Override
	public long getByte() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIndexSize() {
		// TODO Auto-generated method stub
		return 0;
	}
}
