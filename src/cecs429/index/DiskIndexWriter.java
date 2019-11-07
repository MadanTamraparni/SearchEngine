package cecs429.index;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import org.mapdb.*;

public class DiskIndexWriter {
    // this is hard code for testing
    //private String path = "/mnt/c/Users/nhmin/OneDrive/Documents/DATA/Codes/Projects/SearchEngine/src/indexBin";
	private BTreeMap<String,Long> bPlus;
	
    public void WriteIndex(Index index, String path){
        int vocabOffset = 0;
        int postingOffset = 0;
        // Creating file
        File vocabFile = new File(path + "/vocab.bin");
        File tableFile = new File(path + "/vocabTable.bin");
        checkFileExist(tableFile);
        //File postingFile = new File(path + "/postings"  + Integer.toString(indexCounter) +  ".bin");
        File postingFile = new File(path + "/postings.bin");
        checkFileExist(postingFile);
        File docWeightsFile = new File(path + "/docWeights.bin");
        checkFileExist(docWeightsFile);
        RandomAccessFile docWeightsRaf = null;
        try {
			docWeightsRaf = new RandomAccessFile(docWeightsFile, "r");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        
        // Create all other file stream inside try parameter
        try{
            FileOutputStream vocabFos = new FileOutputStream(vocabFile, true);
            FileOutputStream postingFos = new FileOutputStream(postingFile, true);
            FileOutputStream tableFos = new FileOutputStream(tableFile, true);
            DataOutputStream tableStream = new DataOutputStream(tableFos);
            List<String> vocabList = index.getVocabulary();
            String term;
            
            DB db = DBMaker.fileDB(path + "/bPlus.db").make();
 		    bPlus = db.treeMap("map")
 			    .keySerializer(Serializer.STRING)
 			    .valueSerializer(Serializer.LONG)
 			    .counterEnable()
                 .createOrOpen();
            bPlus.put(, (long)postingOffset);
            bPlus.put(term, (long)postingOffset);
            bPlus.put(term, (long)postingOffset);
            
            for(int i = 0; i < vocabList.size(); i++){
            	
            	term = vocabList.get(i);
                System.out.println("before convert:" + postingOffset);
                System.out.println("after conver: " + (long)postingOffset);
            	bPlus.put(term, (long)postingOffset);
            	
                // there is a empty space register as a vocab i need to increment to avoid wrong
                vocabTable(vocabOffset, postingOffset, tableStream);
                
                vocabOffset += vocab(term, vocabFos);
                postingOffset += posting(postingFos, index,term, docWeightsRaf);
                
                if(vocabList.get(i).isEmpty()){
                	vocabOffset++;
                }
                
                vocabFos.flush();
                tableFos.flush();
                postingFos.flush();
                tableStream.flush();
            }
            vocabFos.close();
            tableFos.close();
            postingFos.close();
            tableStream.close();
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

    private void vocabTable(long vocabPosition, long postingPosition, DataOutputStream tableStream){
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
    }
    
    private long posting(FileOutputStream file, Index index, String term, RandomAccessFile docWeightsRaf){
		DataOutputStream binFile = new DataOutputStream(file);
		long position = 0;
		try {
			List<Posting> postings = index.getPostings(term);
			int docNum = postings.size();
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
				
				
				binFile.writeInt(doc.getDocumentId());//write docID
				binFile.writeDouble(1+Math.log(tftd));//write default wdt
				binFile.writeDouble((2.2 * tftd) / (1.2 * (0.25 + 0.75 * (docLength / docLengthAvg) + tftd)));//write BM25 wdt
				binFile.writeDouble((1+Math.log(tftd))/(1+Math.log(avgTftd)));//write Wacky wdt
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
			e.printStackTrace();
		}
		
		return position;
		
	}
}


