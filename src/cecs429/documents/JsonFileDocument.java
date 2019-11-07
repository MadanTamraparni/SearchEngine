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

public class JsonFileDocument implements FileDocument {
    private int docId;
    private Path filePath;
    private String docTitle;

    public JsonFileDocument(int id, Path filePath) {
        docId = id;
        this.filePath = filePath;
        setTitle();
    }

    @Override
    public int getId() {
        return this.docId;
    }

    @Override
    public Path getFilePath() {
        return this.filePath;
    }

    @Override
    public Reader getContent() {
        try{
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(
                new JsonReader(
                    new FileReader(filePath.toString()))).getAsJsonObject();
            Reader inputString = new StringReader(jsonObject.get("body").getAsString());
            return new BufferedReader(inputString);


        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        return docTitle;
    }

    private void setTitle(){
        try{
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(
                new JsonReader(
                    new FileReader(filePath.toString()))).getAsJsonObject();
            this.docTitle =  jsonObject.get("title").getAsString();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}

	@Override
	public long getByte() {

		File doc = new File(filePath.toString());
		long size = doc.length();
		return size;
	}
}