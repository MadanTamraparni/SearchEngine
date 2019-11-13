package edu.csulb;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.SpimiIndexWriter;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.ranked.BM25Model;
import cecs429.ranked.DefaultModel;
import cecs429.ranked.RankedRetrieval;
import cecs429.ranked.TfidfModel;
import cecs429.ranked.WackyModel;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.PorterStemmer;
import cecs429.text.TokenProcessor;

public class TermDocumentIndexerMain {

	public static final String STEM_STR = "stem";
	public static final String QUIT_STR = "q";
	public static final String INDEX_STR = "index";
	public static final String VOCAB_STR = "vocab";
	public static final int MAX_INDEX_SIZE = 30000;


	public static void main(String[] args)
	{
		// User input and check for file directory
		String mCorpusPath = "", mDiskWritePath = "", mSearchSelection = "", mModelSelection = "";
        Scanner in = new Scanner(System.in);
        while(true){
            System.out.print("Enter document directory: ");
            mCorpusPath = in.nextLine();
            File testDir = new File(mCorpusPath);
            if(testDir.isDirectory()){
				System.out.println("Directory Existed. Procceed to indexing...");
				break;
			}
            System.out.println("Directory does not exist. ");
        }
		long timeStart = System.currentTimeMillis();
		
		// Making document corpus
		DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(new File(mCorpusPath).toPath(), ".json");
		int corpusSize = corpus.getCorpusSize();
		
		while(true){
			System.out.print("Enter bin save path: ");
			mDiskWritePath = in.nextLine();
            File testDir = new File(mDiskWritePath);
            if(testDir.isDirectory()){
				System.out.println("Directory Existed. Procceed to write on disk...");
				break;
            }
            System.out.println("Directory does not exist. ");
		}
		
		Index index = indexCorpus(corpus, mDiskWritePath);

		long timeEnd = System.currentTimeMillis();
		// printout time it take to index the corpus
		timeConvert(timeEnd - timeStart);

		String query = "";
		TokenProcessor processor = new BasicTokenProcessor();
		
		
		// menu for handling special queries
        while(true){
        	List<Posting> postingList = new ArrayList<Posting>();
        	List<Double> accumulator = null;
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

        			index = indexCorpus(corpus, mDiskWritePath) ;
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
			// Query Selecting
			// Selecting searching method
			System.out.println("Select query method: ");
			System.out.println("1. Ranked Retrieval");
			System.out.println("2. Boolean Retrieval");
			while(true){
				System.out.print("Enter query method (Use number as entry): ");
				mSearchSelection = in.nextLine();
				if(mSearchSelection.equals("1")){
						try{
							// TO-DO: call ranked retrieval
							System.out.println("Select model:");
							System.out.println("1. Default rank model");
							System.out.println("2. Okapi BM25 model");
							System.out.println("3. Tf-idf model");
							System.out.println("4. Wacky model");
							RandomAccessFile docWeightsRaf = new RandomAccessFile(new File(mDiskWritePath + "/docWeights.bin"), "r");

							while(true){
								System.out.print("Enter model (Use number as entry): ");
								mModelSelection = in.nextLine();
								RankedRetrieval rankedRetrieval = new RankedRetrieval();
								if(mModelSelection.equals("1")){ //Default model
									postingList = rankedRetrieval.getResults(new DefaultModel(index, corpusSize, docWeightsRaf, processor), query);
									accumulator = rankedRetrieval.getAcculumator();
									break;
								} else if(mModelSelection.equals("2")){//BM25 model
									postingList = rankedRetrieval.getResults(new BM25Model(index, corpusSize, processor), query);
									accumulator = rankedRetrieval.getAcculumator();
									break;
								} else if(mModelSelection.equals("3")){//Tfidf model
									postingList = rankedRetrieval.getResults(new TfidfModel(index, corpusSize, docWeightsRaf, processor), query);
									accumulator = rankedRetrieval.getAcculumator();
									break;
								} else if(mModelSelection.equals("4")){//Wacky model
									postingList = rankedRetrieval.getResults(new WackyModel(index, corpusSize, docWeightsRaf, processor), query);
									accumulator = rankedRetrieval.getAcculumator();
									break;
								}
								System.out.println("Please only select option above.");
							}
						} catch (IOException e){
							System.out.println("Can't find docWeights.bin");
						}
						break;
					}
				else if(mSearchSelection.equals("2")){
						// TO-DO: boolean retrieval
						BooleanQueryParser queryParser = new BooleanQueryParser();
						QueryComponent queryComponent = queryParser.parseQuery(query);
						postingList = queryComponent.getPostings(index, processor);
						break;
					}
				System.out.println("Please only select option above.");
			}
			int i = 0;
            for (Posting p : postingList) {
            	System.out.println("Doc ID = " + p.getDocumentId());
				System.out.println("Title: " + corpus.getDocument(p.getDocumentId()).getTitle());
				if(accumulator != null) {
					System.out.println("Accumulator score: " + accumulator.get(i));
					i += 1;
				}
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
	
	
	
	
	
	private static void checkFileExist(File fileCheck){
        if(fileCheck.exists()){
            fileCheck.delete();
            try{
                fileCheck.createNewFile();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
	
	
	

	private static Index indexCorpus(DocumentCorpus corpus, String path){
		BasicTokenProcessor processor = new BasicTokenProcessor();

		// Get all the documents in the corpus by calling GetDocuments().
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.

		File tmp = new File(path + "\\postings.bin");
		if(tmp.exists())
		{
			return new DiskPositionalIndex(path);
		}
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		SpimiIndexWriter spimiIndexWriter = new SpimiIndexWriter(path);
		Iterable<Document> it = corpus.getDocuments();
		File docWeightsFile = new File(path + "/docWeights.bin");
		checkFileExist(docWeightsFile);
		
		try {
			FileOutputStream docWeightsFos = new FileOutputStream(docWeightsFile);
			DataOutputStream docWeightsDos = new DataOutputStream(docWeightsFos);
			double weight;
			double docLengthAvg = 0;
			
			for(Document doc : it){ //for each document
				HashMap<String, Double> wdt = new HashMap<String,Double>();
				
				double docWeights = 0;
				long docLength = 0; 
				long docByte = doc.getByte();
				double avgTftd = 0;
				int currentPosition = -1;
				int docId = doc.getId();
				
				EnglishTokenStream eng = new EnglishTokenStream(doc.getContent());
				Iterable<String> strIter = eng.getTokens();	
				for(String token : strIter) //for each term in the document
				{
					docLength++;
					List<String> tokenList = processor.enhancedProcessToken(token);//normalize the term into a list of token
					for(String newToken:tokenList) //for each token in the list of token
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
					weight = 1 + Math.log(entry.getValue()); //default wdt
					entry.setValue(weight);
					docWeights += Math.pow(weight, 2.0); //default Ld
				}
				
				avgTftd = avgTftd/wdt.size();
				docWeights = Math.sqrt(docWeights);
				
				docWeightsDos.writeDouble(docWeights);//docID * 32 
				docWeightsDos.writeLong(docLength);//(docID * 32) + 8
				docWeightsDos.writeLong(docByte);//(docID * 32) + 16
				docWeightsDos.writeDouble(avgTftd);//(docID * 32) + 24
				
				docLengthAvg += docLength;
				if(index.getIndexSize() > MAX_INDEX_SIZE)
				{
					spimiIndexWriter.writePartialIndex(index);
					index = new PositionalInvertedIndex();
				}
			}
			docLengthAvg = docLengthAvg/corpus.getCorpusSize();
			docWeightsDos.writeDouble(docLengthAvg);//Last 8 bytes
			docWeightsDos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		if(index.getIndexSize() > 0 )
			spimiIndexWriter.writePartialIndex(index);

		return spimiIndexWriter.mergePartialIndex(index);		
	}
}
