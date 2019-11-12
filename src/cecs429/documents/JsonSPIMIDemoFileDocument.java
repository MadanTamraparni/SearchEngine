package cecs429.documents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonSPIMIDemoFileDocument implements FileDocument {
    private int docId;
    private Path filePath;
    private String docTitle;

    public JsonSPIMIDemoFileDocument(int id, Path filePath)
    {
    	docId = id;
        this.filePath = filePath;
        setTitle();
    }
    
    public static FileDocument loadJsonSPIMIFileDocument(Path absolutePath, int documentId) {
		return new JsonSPIMIDemoFileDocument(documentId, absolutePath);
	}
    
	@Override
	public int getId() {
		return this.docId;
	}

	@Override
	public Reader getContent() {
		try{
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(
                new JsonReader(
                    new FileReader(filePath.toString()))).getAsJsonObject();
            Reader inputString = new StringReader(jsonObject.get("fulltext").getAsString());
            return new BufferedReader(inputString);


        }catch(IOException e){
            throw new RuntimeException(e);
        }
	}

	@Override
	public String getTitle() {
		return docTitle;
	}

	@Override
	public long getByte() {
		File doc = new File(filePath.toString());
		long size = doc.length();
		return size;
	}

	@Override
	public Path getFilePath() {
		return this.filePath;
	}
    private void setTitle(){
        try{
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(
                new JsonReader(
                    new FileReader(filePath.toString()))).getAsJsonObject();
            this.docTitle =  jsonObject.get("headline").getAsString();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
}
