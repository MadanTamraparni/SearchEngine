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
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.SpimiIndexWriter;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.ranked.BM25Model;
import cecs429.ranked.DefaultModel;
import cecs429.ranked.RankModel;
import cecs429.ranked.RankedRetrieval;
import cecs429.ranked.TfidfModel;
import cecs429.ranked.WackyModel;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.PorterStemmer;
import cecs429.text.TokenProcessor;
import kotlin.random.Random.Default;

import org.mapdb.*;

public class TermDocumentIndexerMain {

	public static final String STEM_STR = "stem";
	public static final String QUIT_STR = "q";
	public static final String INDEX_STR = "index";
	public static final String VOCAB_STR = "vocab";
	public static final int MAX_INDEX_SIZE = 1000000;

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

		// Commented line below is to handle text file
		DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(new File(mCorpusPath).toPath(), ".json");
		int corpusSize = corpus.getCorpusSize();
		
		//pathDisk = "F:\\Study\\Fall\\CECS529\\Project\\SearchEngine\\src\\indexBin";
		// Remove after testing
		//mDiskWritePath = "/mnt/c/Users/nhmin/OneDrive/Documents/DATA/Codes/Projects/SearchEngine/src/indexBin";

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
		// where posting.bin, vocab.bin, and vocabTable.bin
		// vocab.bin and vocabTable.bin will be comment out
		DiskIndexWriter indexDisk = new DiskIndexWriter();
		//mDiskWritePath = "/mnt/c/Users/nhmin/OneDrive/Documents/DATA/Codes/Projects/SearchEngine/src/indexBin";
		indexDisk.WriteIndex(index, mDiskWritePath, "postings.bin");

		// NOTE: Declare but never use
		DiskPositionalIndex diskPosition = new DiskPositionalIndex(mDiskWritePath);

        //BooleanQueryParser queryParser = new BooleanQueryParser();

		long timeEnd = System.currentTimeMillis();
		// printout time it take to index the corpus
		// Reimplement later 
		timeConvert(timeEnd - timeStart);

		String query = "";
		TokenProcessor processor = new BasicTokenProcessor();
		List<Posting> postingList = new ArrayList<Posting>();
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
							RankedRetrieval rankedRetrieval = new RankedRetrieval();
							RandomAccessFile docWeightsRaf = new RandomAccessFile(new File(mDiskWritePath + "/docWeights.bin"), "r");

							while(true){
								System.out.print("Enter model (Use number as entry): ");
								mModelSelection = in.nextLine();
								if(mModelSelection.equals("1")){
									postingList = rankedRetrieval.getResults(new DefaultModel(index, corpusSize, docWeightsRaf), query);
									break;
								} else if(mModelSelection.equals("2")){
									postingList = rankedRetrieval.getResults(new BM25Model(index, corpusSize), query);
									break;
								} else if(mModelSelection.equals("3")){
									postingList = rankedRetrieval.getResults(new TfidfModel(index, corpusSize, docWeightsRaf), query);
									break;
								} else if(mModelSelection.equals("4")){
									postingList = rankedRetrieval.getResults(new WackyModel(index, corpusSize, docWeightsRaf), query);
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
		//SpimiIndexWriter spimiIndexWriter = new SpimiIndexWriter(path);
		Iterable<Document> it = corpus.getDocuments();
		File docWeightsFile = new File(path + "/docWeights.bin");

		try {
			FileOutputStream docWeightsFos = new FileOutputStream(docWeightsFile);
			DataOutputStream docWeightsDos = new DataOutputStream(docWeightsFos);
			double weight;
			double docLengthAvg = 0;
			
			for(Document doc : it){
				HashMap<String, Double> wdt = new HashMap<String,Double>();
				double docWeights = 0;
				long docLength = 0; 
				long docByte = doc.getByte();
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
					
						if(MAX_INDEX_SIZE == index.getIndexSize())
						{
							//if(spimiIndexWriter.writePartialIndex(index))
								//index = new PositionalInvertedIndex();
						}
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
			docWeightsDos.writeDouble(docLengthAvg);//Last 8 bytes
			docWeightsDos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//return spimiIndexWriter.mergePartialIndex();
		return index;
	}
}
