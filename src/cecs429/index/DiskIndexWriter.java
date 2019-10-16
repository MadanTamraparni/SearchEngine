package cecs429.index;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

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
            FileOutputStream tableFos = new FileOutputStream(tableFile, true);
            DataOutputStream tableStream = new DataOutputStream(tableFos);
            List<String> vocabList = index.getVocabulary();
            for(int i = 0; i < vocabList.size(); i++){
                // there is a empty space register as a vocab i need to increment to avoid wrong
                vocabTable(vocabOffset, postingOffset, tableStream);
                vocabOffset += vocab(vocabList.get(i), vocabFos);
                if(vocabList.get(i).isEmpty()) vocabOffset++;
                // PostingOffset here is just a test code
                // change the line below to appropiately change the offset
                postingOffset += 78;
                vocabFos.flush();
                tableFos.flush();
                tableStream.flush();
            }
            vocabFos.close();
            tableFos.close();
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
}