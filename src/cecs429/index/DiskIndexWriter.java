package cecs429.index;

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
        try(FileOutputStream vocabFos = new FileOutputStream(vocabFile, true)){
            List<String> vocabList = index.getVocabulary();
            System.out.println(vocabList.size());
            for(int i = 0; i < vocabList.size(); i++){
                int vocabOffset = vocab(vocabList.get(i), vocabFos);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    private int vocab(String term, FileOutputStream fos){
        byte[] bytes = term.getBytes(StandardCharsets.UTF_8);
        System.out.println(term + " : " + bytes.length);
        try{
            fos.write(bytes);
        } catch(IOException e){
            e.printStackTrace();
        }
        return bytes.length;
    }
}