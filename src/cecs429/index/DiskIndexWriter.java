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
    // this is hard code for testing
    //private String path = "/mnt/c/Users/nhmin/OneDrive/Documents/DATA/Codes/Projects/SearchEngine/src/indexBin";
	private BTreeMap<String,Long> mBPlus;
	
    public void WriteIndex(Index index, String path, int counter){
    	//Creating only postings file as BPlus tree does not use vocab and vocabTable.bin files to search the terms
    	int postingOffset = 0;
        File postingFile = new File(path + "\\partialIndex" + "\\postings" + Integer.toString(counter) + ".bin");
        checkFileExist(postingFile);
        File docWeightsFile = new File(path + "\\docWeights.bin");

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
            
            DB db = DBMaker.fileDB(path + "/partialIndex" + "/bPlus" + Integer.toString(counter) + ".db")
            		.closeOnJvmShutdown()
        			.transactionEnable()
        			.make();
            
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
			binFile.writeInt(docNum);//write dft
			position += 4;
			int prevDocId = 0;
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
					binFile.writeInt(doc.getDocumentId() - prevDocId);
					prevDocId = doc.getDocumentId();
				}				
				               
                double defaultWdt = 1.0+Math.log(tftd);
				binFile.writeDouble(defaultWdt);//write default wdt
				doc.addWdt(defaultWdt);
				
				double bm25Wdt = (2.2 * tftd) / (1.2 * (0.25 + 0.75 * (docLength / docLengthAvg) + tftd));
				binFile.writeDouble(bm25Wdt);//write BM25 wdt
				doc.addWdt(bm25Wdt);

				double wackyWdt = (1.0+Math.log(tftd))/(1.0+Math.log(avgTftd));
				binFile.writeDouble(wackyWdt);//write Wacky wdt
				doc.addWdt(wackyWdt);
				
				binFile.writeInt(tftd);//write tftd
				binFile.writeInt(positions.get(0));//write position 1
				position += 36;
				
				if(positions.size() > 1) {
					for(int j = 1; j < positions.size(); j++){
						binFile.writeInt(positions.get(j)-positions.get(j-1));//write the position gap					
						position += 4;
					}
				}
			}
		}catch (IOException e) {
			//e.printStackTrace();
		}

		return position;
	}

    /*private void vocabTable(long vocabPosition, long postingPosition, DataOutputStream tableStream){
        try{
            //System.out.println("vocabPos: " + vocabPosition);
            tableStream.writeLong(vocabPosition);
            tableStream.writeLong(postingPosition);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private long vocab(String term, FileOutputStream fos){
        byte[] bytes = term.getBytes(StandardCharsets.UTF_8);
        try{
            fos.write(bytes);
        } catch(IOException e){
            e.printStackTrace();
        }
        return Long.valueOf(bytes.length);
    }*/
}


