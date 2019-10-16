package cecs429.index;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class DiskIndexWriter {
    // this is hard code for testing
    //private String path = "/mnt/c/Users/nhmin/OneDrive/Documents/DATA/Codes/Projects/SearchEngine/src/indexBin";
	
    public void WriteIndex(Index index, String path){
        int vocabOffset = 0;
        int postingOffset = 0;
        Scanner in = new Scanner(System.in);
        do{
            File testDir = new File(path);
            if(testDir.isDirectory()){
				System.out.println("Directory Existed. Procceed to write on disk...");
				break;
            }
            System.out.println("Directory does not exist. ");
            System.out.print("Re-enter save bin directory: ");
            path = in.nextLine();
        }while(true);

        // Creating file
        File vocabFile = new File(path + "/vocab.bin");
        checkFileExist(vocabFile);
        File tableFile = new File(path + "/vocabTable.bin");
        checkFileExist(tableFile);
        File postingFile = new File(path + "/postings.bin");
        checkFileExist(postingFile);
        // Create all other file stream inside try parameter
        try{
            FileOutputStream vocabFos = new FileOutputStream(vocabFile, true);
            FileOutputStream postingFos = new FileOutputStream(postingFile, true);
            FileOutputStream tableFos = new FileOutputStream(tableFile, true);
            DataOutputStream tableStream = new DataOutputStream(tableFos);
            List<String> vocabList = index.getVocabulary();
            String term;
            for(int i = 0; i < vocabList.size(); i++){
            	term = vocabList.get(i);
            	
                // there is a empty space register as a vocab i need to increment to avoid wrong
                vocabTable(vocabOffset, postingOffset, tableStream);
                
                vocabOffset += vocab(term, vocabFos);
                postingOffset += posting(postingFos, index,term);
                
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
            System.out.println("postingpos: " + postingPosition);
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
    
    private long posting(FileOutputStream file, Index index, String term){
		DataOutputStream binFile = new DataOutputStream(file);
		long position = 0;
		try {
			List<Posting> postings = index.getPostings(term);
			int docNum = postings.size();
			binFile.writeInt(docNum);
			position += 4;
			for(int i = 0; i < docNum; i++) {
				Posting doc = postings.get(i);
				List<Integer> positions = doc.getPositions();
				
				binFile.writeInt(doc.getDocumentId());
				binFile.writeInt(positions.size());
				binFile.writeInt(positions.get(0));
				position += 12;
				
				if(positions.size() > 1) {
					for(int j = 1; j < positions.size(); j++){
						binFile.writeInt(positions.get(j)-positions.get(j-1));
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


