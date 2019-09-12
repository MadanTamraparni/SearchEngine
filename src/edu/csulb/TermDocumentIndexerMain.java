package edu.csulb;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.InvertedIndex;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class TermDocumentIndexerMain {
	public static void main(String[] args)
	{
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
		Index index = indexCorpus(corpus) ;
		// We aren't ready to use a full query parser; for now, we'll only support single-term queries.
		/*List<String> list = index.getVocabulary();
		for(int i=0; i < list.size(); i++)
		{
			System.out.println(list.get(i));
		}*/
		Scanner sc = new Scanner(System.in);
		while(sc.hasNext())
		{
			String query = sc.next();
			//String query = "whale"; // hard-coded search for "whale"
			for (Posting p : index.getPostings(query)) {
				System.out.println("Document ID " + p.getDocumentId());
			}
		}
	}
	
	private static Index indexCorpus(DocumentCorpus corpus) {
		//HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		
		// First, build the vocabulary hash set.
		
		// TODO:
		// Get all the documents in the corpus by calling GetDocuments().
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.
		
		InvertedIndex docIndex = new InvertedIndex();
		Iterable<Document> it = corpus.getDocuments();
		
		for(Document doc : it) {
			EnglishTokenStream eng = new EnglishTokenStream(doc.getContent());
			Iterable<String> strIter = eng.getTokens();
			for(String token : strIter)
			{
				docIndex.addTerm(processor.processToken(token), doc.getId());
			}
		}
		
		return docIndex;
	}
}
