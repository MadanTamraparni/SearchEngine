package edu.csulb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.PorterStemmer;
import cecs429.text.TokenStream;
import cecs429.query.NearLiteral;

public class TermDocumentIndexerMain {

	public static final String STEM_STR = "stem";
	public static final String QUIT_STR = "q";
	public static final String INDEX_STR = "index";
	public static final String VOCAB_STR = "vocab";
	public static final char NEAR_STR = '[';
	
	public static void main(String[] args)
	{
		//DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
        String path = "";
        Scanner in = new Scanner(System.in);
        while(true){
            System.out.print("Enter document directory: ");
            path = in.nextLine();
            File testDir = new File(path);
            if(testDir.isDirectory()){
				System.out.println("Directory Existed. Procceed to indexing...");
				break;
			}
            System.out.print("Directory does not exist. ");
        }
		long timeStart = System.currentTimeMillis();
        DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(new File(path).toPath(),".json");

        Index index = indexCorpus(corpus) ;
        
        BooleanQueryParser queryParser = new BooleanQueryParser();

		long timeEnd = System.currentTimeMillis();
		timeConvert(timeEnd - timeStart);
        // We aren't ready to use a full query parser; for now, we'll only support single-term queries.

        String query = "";
        while(true){
            System.out.print("Enter search query: ");
            query = in.nextLine();
            if(query.equals(QUIT_STR))
            {
            	System.out.println("q entry for quit.");
            	break;
            }
            else if(query.contains(STEM_STR))
            {
            	query = query.substring(STEM_STR.length()+1);
            	PorterStemmer stemmer = new PorterStemmer();
            	System.out.println("Stemmer Token = " + stemmer.GetStemmedToken(query));
            }
            else if(query.contains(INDEX_STR))
            {
            	
            }
            else if(query.contains(VOCAB_STR))
            {
            	List<String> vocabList = index.getVocabulary();
            	for(int i =0; i < 1000; i++)
            		System.out.println(vocabList.get(i));
            	System.out.println("Size of the Vocabulary = " + vocabList.size());
            	break;
			}
			// This is for testing purpose
            // else if(query.charAt(0) == NEAR_STR){
			// 	String[] subString = query.split(" ");
			// 	NearLiteral near = new NearLiteral(subString[0].substring(1, subString[0].length()), 
			// 	subString[1].charAt(subString[1].length() - 1),
			// 	subString[2].substring(0, subString[2].length()- 1));
			// 	for (Posting p : near.getPostings(index)) {
			// 		System.out.println("Document ID " + p.getDocumentId());
			// 		// Below print line only for tracing the index
			// 		System.out.println("Title: " + corpus.getDocument(p.getDocumentId()).getTitle());
			// 	}
			// }
            if(query.length() == 0)
            	continue;
            QueryComponent queryComponent = queryParser.parseQuery(query);
            System.out.println("Size = " + queryComponent.getPostings(index).size());
            for (Posting p : queryComponent.getPostings(index)) {
            	//int doc = p.getDocumentId();
            	//doc++;
				//System.out.println("Document ID \"article" + doc + ".json\"");
				// Below print line only for tracing the index
				System.out.println("Title: " + corpus.getDocument(p.getDocumentId()).getTitle());
            }

			
           
        }
        in.close();
	}

	private static void timeConvert(long time){
		double seconds = time / 1000.0;
		int min = (int)(seconds/60.0), intSecond = (int)(seconds%60);
		System.out.println(min + " minute/s " + intSecond + " seconds.");
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
		
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		Iterable<Document> it = corpus.getDocuments();
		
		for(Document doc : it) {
			EnglishTokenStream eng = new EnglishTokenStream(doc.getContent());
			Iterable<String> strIter = eng.getTokens();
			int currentPosition = -1;
			int docId = doc.getId();
			for(String token : strIter)
			{
				List<String> tokenList = processor.enhancedProcessToken(token);
				for(String newToken:tokenList)
				{
					currentPosition++;
					index.addTerm(newToken, docId, currentPosition);
				}

				// ==============================Testing section===========================
				// Below is old code that not using our enhancedProcessToken
				// it serve purpose of testing Near operation
				// currentPosition++;
				// index.addTerm(processor.processToken(token), docId, currentPosition);
				// ============================= End testing===============================
			}
		}
		return index;
	}
}
