package cecs429.index;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import org.mapdb.*;

public class DiskIndexWriter {
	private BTreeMap<String,Long> mBPlus;
	
    public void WriteIndex(Index index, String path, int counter){
    	//Creating only postings file as BPlus tree does not use vocab and vocabTable.bin files to search the terms
    	int postingOffset = 0;
        File postingFile = new File(path + "/partialIndex" + "/postings" + Integer.toString(counter) + ".bin");
        checkFileExist(postingFile);
        File docWeightsFile = new File(path + "/docWeights.bin");

        RandomAccessFile docWeightsRaf = null;
        try {
			docWeightsRaf = new RandomAccessFile(docWeightsFile, "r");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        
        // Create all other file stream inside try parameter
        try{
            FileOutputStream postingFos = new FileOutputStream(postingFile, true);
            List<String> vocabList = index.getVocabulary();
            String term;
            
            DB db = DBMaker.fileDB(path + "/partialIndex" + "/bPlus" + Integer.toString(counter) + ".db").make();
            
 		    mBPlus = db.treeMap("map")
 			    .keySerializer(Serializer.STRING)
 			    .valueSerializer(Serializer.LONG)
 			    .counterEnable()
 			    .createOrOpen();
            
            for(int i = 0; i < vocabList.size(); i++)
            {
            	term = vocabList.get(i);
            	mBPlus.put(term, (long)postingOffset);            	
                postingOffset += posting(postingFos, index,term, docWeightsRaf);
                postingFos.flush();
            }
            postingFos.close();
            db.close();            
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    /**if file exists, delete and create a new one**/
    private void checkFileExist(File fileCheck){
        if(fileCheck.exists()){
            fileCheck.delete();
            try{
                fileCheck.createNewFile();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    
    private long posting(FileOutputStream file, Index index, String term, RandomAccessFile docWeightsRaf){
		DataOutputStream binFile = new DataOutputStream(file);
		long position = 0;
		try {
			List<Posting> postings = index.getPostings(term);
            int docNum = postings.size();
            int prevDocId = 0;
			binFile.writeInt(docNum);//write dft
			position += 4;
			
			for(int i = 0; i < docNum; i++) {
				
				Posting doc = postings.get(i);
				int docId = doc.getDocumentId();
				
				List<Integer> positions = doc.getPositions();
				int tftd = positions.size();
				
				docWeightsRaf.seek(docId*32 + 8);
				double docLength = docWeightsRaf.readDouble();//read docLength
				
				docWeightsRaf.seek(docWeightsRaf.length()-8);
				double docLengthAvg = docWeightsRaf.readDouble();//read docAverage
				
				docWeightsRaf.seek(docId*32 +24);
				double avgTftd = docWeightsRaf.readDouble();//read average tftd of a doc

				if(i == 0)
				{
					binFile.writeInt(doc.getDocumentId());//write docID
					prevDocId = docId;
				}
				else
				{
					binFile.writeInt(doc.getDocumentId() - prevDocId); //write doc gap
					prevDocId = doc.getDocumentId();
				}				
				               
                double defaultWdt = 1.0+Math.log(tftd);
				binFile.writeDouble(defaultWdt);//write default wdt
				
				double bm25Wdt = (2.2 * tftd) / (1.2 * (0.25 + 0.75 * (docLength / docLengthAvg) + tftd));
				binFile.writeDouble(bm25Wdt);//write BM25 wdt

				double wackyWdt = (1.0+Math.log(tftd))/(1.0+Math.log(avgTftd));
				binFile.writeDouble(wackyWdt);//write Wacky wdt
				
				binFile.writeInt(tftd);//write tftd
				binFile.writeInt(positions.get(0));//write position 1
				position += 36;
				int prevPos = positions.get(0);
				if(positions.size() > 1) {
					for(int j = 1; j < positions.size(); j++){
						binFile.writeInt(positions.get(j)-prevPos);//write the position gap		
						prevPos = positions.get(j);
						position += 4;
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}

		return position;
	}
}


