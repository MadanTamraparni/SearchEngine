package edu.csulb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
// import javafx.scene.shape.Path;
// import jdk.nashorn.internal.parser.JSONParser;

public class BetterTermDocumentIndexer {
	public static void main(String[] args) {
		parseJSON();
		System.out.println("Done");
//		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
//		Index index = indexCorpus(corpus) ;
//		// We aren't ready to use a full query parser; for now, we'll only support single-term queries.
//		Scanner sc = new Scanner(System.in);
//		while(sc.hasNext())
//		{
//			String query = sc.next();
//			//String query = "whale"; // hard-coded search for "whale"
//			for (Posting p : index.getPostings(query)) {
//				System.out.println("Document ID " + p.getDocumentId());
//			}
//		}
	}
	
	private static Index indexCorpus(DocumentCorpus corpus) {
		HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		
		// First, build the vocabulary hash set.
		
		// TODO:
		// Get all the documents in the corpus by calling GetDocuments().
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.
		
		Iterable<Document> it = corpus.getDocuments();
		
		for(Document doc : it) {
			EnglishTokenStream eng = new EnglishTokenStream(doc.getContent());
			Iterable<String> strIter = eng.getTokens();
			for(String token : strIter)
			{
				vocabulary.add(processor.processToken(token));
			}
		}
		
		// TODO:
		// Constuct a TermDocumentMatrix once you know the size of the vocabulary.
		// THEN, do the loop again! But instead of inserting into the HashSet, add terms to the index with addPosting.
		
		TermDocumentIndex docIndex = new TermDocumentIndex(vocabulary,corpus.getCorpusSize());
		
		Iterable<Document> iter = corpus.getDocuments();
		
		for(Document doc : iter) {
			EnglishTokenStream eng = new EnglishTokenStream(doc.getContent());
			Iterable<String> strIter = eng.getTokens();
			for(String token : strIter)
			{
				docIndex.addTerm(processor.processToken(token), doc.getId());
			}
			try {
				eng.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return docIndex;
	}
	
	public static void parseJSON()
	{
		File file = new File(Paths.get("").toAbsolutePath().toString() + "\\all-nps-sites.json");
		System.out.println("Parsing");
		JsonParser parser = new JsonParser();
		JsonObject object = null;
		try {
			object = (JsonObject) parser.parse(new FileReader(file));
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonArray arrObject = object.getAsJsonArray("documents");
		
		int index = 1;
		
		for(int i =0; i < arrObject.size(); i++)
		{
			JsonObject obj = (JsonObject)arrObject.get(i);
			File outFile = new File("F://EPract//SearchEngineHW - HM2//outFiles//article" + Integer.toString(index) + ".json");
			FileWriter writer;
			try {
				writer = new FileWriter(outFile);
				writer.write(obj.toString());
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			index++;
		}
		
	}
}

