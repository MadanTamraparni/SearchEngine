package cecs429.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.mapdb.*;


public class DiskPositionalIndex implements Index{
	private String mPath;
	private RandomAccessFile mVocabList;
	private RandomAccessFile mPostingList;
	private long[] mVocabTable;
	private BTreeMap<Long,Long> bPlus;

	public DiskPositionalIndex(String path){
		try{
			mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
			mPostingList = new RandomAccessFile(new File(path, "postings.bin"), "r");
			mVocabTable = readVocabTable(path);

			makeBMap();

			
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public List<Posting> getPostings(String term) {
		// TODO Auto-generated method stub
		List<Posting> postings = new ArrayList<Posting>();
		long position = binarySearchVocabulary(term);
		byte[] byteBuffer = new byte[4];
		try 
		{
			mPostingList.read(byteBuffer, (int) position, 4);
			long numOfDocs = ByteBuffer.wrap(byteBuffer).getLong();
			position += 4;
			for(int i=0; i < numOfDocs; i++)
			{
				mPostingList.read(byteBuffer, (int) position, 4);
				Posting posting = new Posting(ByteBuffer.wrap(byteBuffer).getInt());
				position += 4;  
				for(int x = 0; x < 3; x++)
				{
					mPostingList.read(byteBuffer, (int) position, 8);
					posting.addWdt(ByteBuffer.wrap(byteBuffer).getDouble());
				}
				long numOfPos = mPostingList.read(byteBuffer, (int) position, 4);
				for(int j=0; j < numOfPos; j++)
				{
					position += 4;
					mPostingList.read(byteBuffer, (int) position, 4);
					posting.addPosition(ByteBuffer.wrap(byteBuffer).getInt());
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

	@Override
	public List<String> getVocabulary() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Posting> getPostingsWithPositions(String term) {
		List<Posting> postings = new ArrayList<Posting>();
		long position = binarySearchVocabulary(term);
		byte[] byteBuffer = new byte[4];
		try {
			mPostingList.read(byteBuffer, (int) position, 4);
			long numOfDocs = ByteBuffer.wrap(byteBuffer).getLong();
			position += 4;
			for(int i=0; i < numOfDocs; i++)
			{
				mPostingList.read(byteBuffer, (int) position, 4);
				postings.add(new Posting(ByteBuffer.wrap(byteBuffer).getInt()));
				position += 4;  
				long numOfPos = mPostingList.read(byteBuffer, (int) position, 4);
				position += numOfPos;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return postings;
	}
	

	private void makeBMap(){
		DB db = DBMaker.memoryDB().make();
		bPlus = db.treeMap("map")
			.keySerializer(Serializer.LONG)
			.valueSerializer(Serializer.LONG)
			.counterEnable()
			.createOrOpen();
		int i;
		for(i = 0; i + 2 < mVocabTable.length;){
			try{
				long currentVocabLoc = mVocabTable[i];
				int termLength = (int)mVocabTable[i + 2] - (int)currentVocabLoc;

				mVocabList.seek(currentVocabLoc);
				System.out.println("term length: " + termLength);
				byte[] buffer = new byte[termLength];
				mVocabList.read(buffer, 0, termLength);
				String fileTerm = new String(buffer, "ASCII");
				System.out.println(fileTerm);
			} catch(IOException e){ e.printStackTrace(); }
		}
	}

	// Locates the byte position of the postings for the given term.
	// For example, binarySearchVocabulary("angel") will return the byte position
	// to seek to in postings.bin to find the postings for "angel".
	private long binarySearchVocabulary(String term) {
		try{
		System.out.println("vocab list length: " + mVocabList.length());
		} catch(IOException e) { e.printStackTrace(); } 
		// do a binary search over the vocabulary, using the vocabTable and the file vocabList.
		int i = 0, j = mVocabTable.length / 2 - 1;
		while (i <= j) {
			try {
			System.out.println("i: " + i);
			System.out.println("j: " + j);
			int m = (i + j) / 2;
			System.out.println("m: " + m);
			long vListPosition = mVocabTable[m * 2];
			System.out.println("vList: " + vListPosition);
			// termLength get out of Int
			int termLength;
			if (m == mVocabTable.length / 2 - 1) {
				System.out.println("mVocabTable[m*2]: " + mVocabTable[m*2]);
				System.out.println("term length:  " + (mVocabList.length() - mVocabTable[m*2]));
				termLength = (int)(mVocabList.length() - mVocabTable[m*2]);
			}
			else {
				System.out.println("mVocabTable[(m+1)*2]: " + mVocabTable[(m+1)*2]);
				if((mVocabTable[(m + 1) * 2] - vListPosition) > Integer.MAX_VALUE) System.out.println("out of range");
				System.out.println("term length: " + (mVocabTable[(m + 1) * 2] - vListPosition));
				termLength = (int)((mVocabTable[(m + 1) * 2] - vListPosition));
				System.out.println("termLenght: == " + termLength);
			}

			mVocabList.seek(vListPosition);
			System.out.println("term length: " + termLength);
			byte[] buffer = new byte[termLength];
			mVocabList.read(buffer, 0, termLength);
			String fileTerm = new String(buffer, "ASCII");
			System.out.println("compare term: " + fileTerm);
			int compareValue = term.compareTo(fileTerm);
			if (compareValue == 0) {
				System.out.println("found it");
				// found it!
				return mVocabTable[m * 2 + 1];
			}
			else if (compareValue < 0) {
				System.out.println("left tree");
				j = m - 1;
			}
			else {
				System.out.println("right tree");
				i = m + 1;
			}
			}
			catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
		return -1;
	}

	// Reads the file vocabTable.bin into memory.
   private static long[] readVocabTable(String indexName) {
      try {
         long[] vocabTable;
         
         RandomAccessFile tableFile = new RandomAccessFile(
          new File(indexName, "vocabTable.bin"),
          "r");
         byte[] byteBuffer = new byte[4];
         tableFile.read(byteBuffer, 0, byteBuffer.length);

		 int tableIndex = 0;
		 // tableFile size / 16 to get numberOfTerm
		 System.out.println("table size: " + tableFile.length());
		 vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
         vocabTable = new long[((int)tableFile.length()/16) * 2];
		 byteBuffer = new byte[8];
         while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { 
			// while we keep reading 8 bytes
            vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
            tableIndex++;
         }
         tableFile.close();
         return vocabTable;
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      return null;
   }


}
