package edu.csulb;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.PorterStemmer;
import cecs429.text.TokenProcessor;

import org.mapdb.*;

public class TermDocumentIndexerMain {

	public static final String STEM_STR = "stem";
	public static final String QUIT_STR = "q";
	public static final String INDEX_STR = "index";
	public static final String VOCAB_STR = "vocab";

	public static void main(String[] args)
	{
		// User input and check for file directory
        String path = "", pathDisk = "";
        Scanner in = new Scanner(System.in);
        while(true){
            System.out.print("Enter document directory: ");
            path = in.nextLine();
            File testDir = new File(path);
            if(testDir.isDirectory()){
				System.out.println("Directory Existed. Procceed to indexing...");
				break;
			}
            System.out.println("Directory does not exist. ");
        }
		long timeStart = System.currentTimeMillis();
		// Making document corpus

		// Commented line below is to handle text file
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(new File(path).toPath(), ".txt");
		int corpusSize = corpus.getCorpusSize();
		pathDisk = "/mnt/c/Users/nhmin/OneDrive/Documents/DATA/Codes/Projects/SearchEngine/src/indexBin";
		Index index = indexCorpus(corpus, pathDisk);

		while(true){
			System.out.print("Enter bin save path: ");
			pathDisk = in.nextLine();
            File testDir = new File(path);
            if(testDir.isDirectory()){
				System.out.println("Directory Existed. Procceed to write on disk...");
				break;
            }
            System.out.println("Directory does not exist. ");
		}

		DiskIndexWriter indexDisk = new DiskIndexWriter();
		pathDisk = "/mnt/c/Users/nhmin/OneDrive/Documents/DATA/Codes/Projects/SearchEngine/src/indexBin";
		indexDisk.WriteIndex(index, pathDisk);
		DiskPositionalIndex diskPosition = new DiskPositionalIndex(pathDisk);
		for(String i : diskPosition.getVocabulary()) System.out.println(i);

        BooleanQueryParser queryParser = new BooleanQueryParser();

		long timeEnd = System.currentTimeMillis();
		// printout time it take to index the corpus
		timeConvert(timeEnd - timeStart);

		String query = "";
		// menu for handling special queries
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
            	continue;
            }
            else if(query.contains(INDEX_STR))
            {
            	query = query.substring(INDEX_STR.length()+1);
            	File testDir = new File(query);
                if(testDir.isDirectory()){
    				System.out.println("Directory Existed. Procceed to indexing...");
    				timeStart = System.currentTimeMillis();
        			corpus = DirectoryCorpus.loadJsonDirectory(new File(query).toPath(),".json");

        			index = indexCorpus(corpus, pathDisk) ;
        			timeEnd = System.currentTimeMillis();
        			timeConvert(timeEnd - timeStart);
    			}else{
    				System.out.println("Directory does not exist. ");
    			}
                continue;
            }
            else if(query.contains(VOCAB_STR))
            {
            	List<String> vocabList = index.getVocabulary();
            	for(int i =0; i < 1000; i++)
            		System.out.println(vocabList.get(i));
            	System.out.println("Size of the Vocabulary = " + vocabList.size());
            	continue;
			}
            if(query.length() == 0){
            	continue;
            }
            QueryComponent queryComponent = queryParser.parseQuery(query);
            TokenProcessor processor = new BasicTokenProcessor();
            List<Posting> postingList = queryComponent.getPostings(index, processor);

            for (Posting p : postingList) {
				System.out.println("Title: " + corpus.getDocument(p.getDocumentId()).getTitle());
            }
            System.out.println("Posting List size = " + postingList.size());
        }
        in.close();
	}

	private static void timeConvert(long time){
		double seconds = time / 1000.0;
		int min = (int)(seconds/60.0), intSecond = (int)(seconds%60);
		System.out.println(min + " minute/s " + intSecond + " seconds.");
	}

	private static Index indexCorpus(DocumentCorpus corpus, String path){
		//HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();

		// Get all the documents in the corpus by calling GetDocuments().
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.

		PositionalInvertedIndex index = new PositionalInvertedIndex();
		Iterable<Document> it = corpus.getDocuments();
		File docWeightsFile = new File(path + "/docWeights.bin");

		try {
			FileOutputStream docWeightsFos = new FileOutputStream(docWeightsFile);
			DataOutputStream docWeightsDos = new DataOutputStream(docWeightsFos);
			double weight;
			long docLengthAvg = 0;
			
			for(Document doc : it){
				HashMap<String, Double> wdt = new HashMap<String,Double>();
				double docWeights = 0;
				long docLength = 0; 
				long docByte = 0; //needs implementation
				double avgTftd = 0;
				int currentPosition = -1;
				int docId = doc.getId();
				EnglishTokenStream eng = new EnglishTokenStream(doc.getContent());
				Iterable<String> strIter = eng.getTokens();
				
				
				for(String token : strIter)
				{
					docLength++;
					List<String> tokenList = processor.enhancedProcessToken(token);
					for(String newToken:tokenList)
					{
						currentPosition++;
						index.addTerm(newToken, docId, currentPosition);

						if(wdt.containsKey(newToken)){
							wdt.put(newToken,wdt.get(newToken) + 1);
						}else {
							wdt.put(newToken, 1.0);
						}
					}
				}


				for(Map.Entry<String,Double> entry:wdt.entrySet()){
					avgTftd += entry.getValue();
					weight = 1 + Math.log(entry.getValue());
					entry.setValue(weight);
					docWeights += Math.pow(weight, 2.0);
				}
				
				avgTftd = avgTftd/wdt.size();
				docWeights = Math.sqrt(docWeights);
				
				docWeightsDos.writeDouble(docWeights);//docID * 32 
				docWeightsDos.writeLong(docLength);//(docID * 32) + 8
				docWeightsDos.writeLong(docByte);//(docID * 32) + 16
				docWeightsDos.writeDouble(avgTftd);//(docID * 32) + 24
				
				
				docLengthAvg += docLength;
			}
			docLengthAvg = docLengthAvg/corpus.getCorpusSize();
			docWeightsDos.writeLong(docLengthAvg);//Last 8 bytes
			docWeightsDos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return index;
	}
}
