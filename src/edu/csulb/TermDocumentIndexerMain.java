package edu.csulb;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import kotlin.random.Random.Default;

import org.mapdb.*;

public class TermDocumentIndexerMain {

	public static final String STEM_STR = "stem";
	public static final String QUIT_STR = "q";
	public static final String INDEX_STR = "index";
	public static final String VOCAB_STR = "vocab";
	public static final String TEST_STR = "test";
	public static final int MAX_INDEX_SIZE = 30000;
	
	public static final boolean printQuery = false;


	public static void main(String[] args)
	{
		// User input and check for file directory
		String mCorpusPath = "", mDiskWritePath = "", mSearchSelection = "", mModelSelection = "";
		boolean testPreset = false;
        Scanner in = new Scanner(System.in);
        System.out.println("Test Rocchio Classifier, Enter Y for Yes, N for No ");
        if(in.nextLine().equalsIgnoreCase("y"))
        	RocchioTest();
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
		long timeStartCorpus = System.currentTimeMillis();
		
		// Making document corpus
		DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(new File(mCorpusPath).toPath(), ".json");
		int corpusSize = corpus.getCorpusSize();
		long timeEndCorpus = System.currentTimeMillis();
		
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
		long timeStartIndex = System.currentTimeMillis();
		Index index = indexCorpus(corpus, mDiskWritePath);
		long timeEndIndex = System.currentTimeMillis();
		// printout time it take to index the corpus
		timeConvert((timeEndCorpus - timeStartCorpus) + (timeEndIndex - timeStartIndex));

		String query = "";
		//Scanner queryScanner, relDocScanner;
		TokenProcessor processor = new BasicTokenProcessor();
		try{
		
		// menu for handling special queries
        while(true){
        	List<Posting> postingList = new ArrayList<Posting>();
        	List<Double> accumulator = null;
				// unless the text file we scanning hit end, it will only allow test query to be pass
				System.out.print("Enter search query: ");
				query = in.nextLine();
            if(query.equals(QUIT_STR))
            {
            	System.out.println("q entry for quit.");
            	break;
				}
				else if(query.contains(TEST_STR)){
					File testFileDir = new File(mCorpusPath + "/relevance");
					if(testFileDir.exists()){
						HashMap<Integer, String> modelMap = new HashMap<>();
						modelMap.put(1, "Default Model");
						modelMap.put(2, "Okapi BM25 Model");
						modelMap.put(3, "Tf-idf Model");
						modelMap.put(4, "Wacky Model");
						for(int i =1; i < 5; i++){
							System.out.println("");
							System.out.println(modelMap.get(i));
							Scanner queryScanner = new Scanner(new File(testFileDir.getAbsolutePath() + "/queries.txt"));
							Scanner relDocScanner = new Scanner(new File(testFileDir.getAbsolutePath() + "/qrel.txt"));
							corpusTester(queryScanner,relDocScanner,mDiskWritePath, Integer.toString(i),corpus,index);
							queryScanner.close();
							relDocScanner.close();
						}
						continue;
					}
						System.out.println("Test file not exist for this corpus.");
						continue;
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
						long timeStart = System.currentTimeMillis();
        			corpus = DirectoryCorpus.loadJsonDirectory(new File(query).toPath(),".json");

        			index = indexCorpus(corpus, mDiskWritePath) ;
						long timeEnd = System.currentTimeMillis();
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
								// to make sure this menu won't repeatedly print during test mode
								RandomAccessFile docWeightsRaf = new RandomAccessFile(new File(mDiskWritePath + "/docWeights.bin"), "r");
								
							// TO-DO: call ranked retrieval
							System.out.println("Select model:");
							System.out.println("1. Default rank model");
							System.out.println("2. Okapi BM25 model");
							System.out.println("3. Tf-idf model");
							System.out.println("4. Wacky model");

							while(true){
								System.out.print("Enter model (Use number as entry): ");
								mModelSelection = in.nextLine();
								RankedRetrieval rankedRetrieval = new RankedRetrieval();
								if(mModelSelection.equals("1")){
									postingList = rankedRetrieval.getResults(new DefaultModel(index, corpusSize, docWeightsRaf, processor), query);
									accumulator = rankedRetrieval.getAcculumator();
									break;
								} else if(mModelSelection.equals("2")){
									postingList = rankedRetrieval.getResults(new BM25Model(index, corpusSize, processor), query);
									accumulator = rankedRetrieval.getAcculumator();
									break;
								} else if(mModelSelection.equals("3")){
									postingList = rankedRetrieval.getResults(new TfidfModel(index, corpusSize, docWeightsRaf, processor), query);
									accumulator = rankedRetrieval.getAcculumator();
									break;
								} else if(mModelSelection.equals("4")){
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

		} catch(IOException e){
			System.out.println("queries and/or relerance file not Exist.");
			e.printStackTrace();
		}
        in.close();
	}

	private static void corpusTester(Scanner queryScanner, Scanner relDocScanner, String mDiskWritePath, String model, DocumentCorpus corpus, Index index){
		String query = "";
		List<Posting> postingList = new ArrayList<Posting>();
		List<Double> accumulator = null, avgPrecision = new ArrayList<>();
		List<Long> responseTimeList = new ArrayList<>();
		TokenProcessor processor = new BasicTokenProcessor();
		int corpusSize = corpus.getCorpusSize(), totalQuery = 0;
		System.out.println("Test mode is activated. Default to use rank retrieval.");
		try{
			RandomAccessFile docWeightsRaf = new RandomAccessFile(new File(mDiskWritePath + "/docWeights.bin"), "r");
			RankedRetrieval rankedRetrieval = new RankedRetrieval();
			while(queryScanner.hasNextLine()){
				query = queryScanner.nextLine();
				totalQuery++;
				//System.out.println("Test Query: " + query);
				// TODO: time start
				long timeStart = System.currentTimeMillis();
				if(model.equals("1")){ //Default model
					//System.out.println("test Mode 1");
					postingList = rankedRetrieval.getResults(new DefaultModel(index, corpusSize, docWeightsRaf, processor), query);
					accumulator = rankedRetrieval.getAcculumator();
				} else if(model.equals("2")){//BM25 model
					postingList = rankedRetrieval.getResults(new BM25Model(index, corpusSize, processor), query);
					accumulator = rankedRetrieval.getAcculumator();
				} else if(model.equals("3")){//Tfidf model
					postingList = rankedRetrieval.getResults(new TfidfModel(index, corpusSize, docWeightsRaf, processor), query);
					accumulator = rankedRetrieval.getAcculumator();
				} else if(model.equals("4")){//Wacky model
					postingList = rankedRetrieval.getResults(new WackyModel(index, corpusSize, docWeightsRaf, processor), query);
					accumulator = rankedRetrieval.getAcculumator();
				}
				// TO DO: time end
				long timeEnd = System.currentTimeMillis();
				responseTimeList.add(timeEnd - timeStart);
				List<String> relList = Arrays.asList(relDocScanner.nextLine().split("\\s+"));
				List<Double> precision = new ArrayList<>(), recall = new ArrayList<>();
				int loResult = 0;
				double i = 0, k = 1;
				HashMap<Integer, Integer> docIndex = corpus.getDocIndex();
				//System.out.println("Posting size :"  + postingList.size());
				/*for(Posting abc : postingList){
					System.out.println(docIndex.get(abc.getDocumentId()));
				}*/
				while(loResult < postingList.size()){
					//System.out.println("docID: " + postingList.get(loResult).getDocumentId() + " in " + docIndex.get(postingList.get(loResult).getDocumentId()));
					if(relList.contains(Integer.toString(docIndex.get(postingList.get(loResult).getDocumentId())))){
					//	System.out.println("precision: " + (++i/k));
						precision.add(++i/k);
						recall.add(i/relList.size());	
					}
					else
					{
						precision.add(i/k);
						recall.add(i/relList.size());
					}
					loResult++;
					k++;
				}
				double sum = 0.0;
				for(double d: precision)
					sum += d;
				
				double ap = sum/postingList.size();
				//System.out.println("Average Precision =  " + ap);
				avgPrecision.add(ap);

				if(!queryScanner.hasNextLine()){
					// TODO: graph

				}
			}
			double MAP = 0;
			for(double i : avgPrecision){
				MAP += i;
			}
			double MRT = 0;
			for(long i : responseTimeList){
				MRT += i;
			}
			MRT /= totalQuery;
			MAP /= totalQuery;
			double throughPut = 1/MRT;
			System.out.println("MAP: " + MAP);
			System.out.println("MRT: " + MRT);
			System.out.println("Throughput: " + throughPut);

		}catch(IOException e){ e.printStackTrace(); }
		
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
		//HashSet<String> vocabulary = new HashSet<>();
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
			
			for(Document doc : it){
				//HashMap<String, Double> wdt = new HashMap<String,Double>();
				HashMap<String, Double> wdt = doc.getWdtMap();
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
				doc.setWdtMap(wdt);
				doc.setLd(docWeights);
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
	
	public static void RocchioTest()
	{
		String filePath = "";
		Scanner in = new Scanner(System.in);
		while(true){
			System.out.print("Enter Federal docs path: ");
			filePath = in.nextLine();
            File testDir = new File(filePath);
            if(testDir.isDirectory()){
				System.out.println("Directory Existed. Procceed to write on disk...");
				break;
            }
            System.out.println("Directory does not exist. ");
		}
		String jayFilePath = filePath + "\\JAY", hamiltonFilePath = filePath + "\\HAMILTON";
		String madisonFilePath = filePath + "\\MADISON", dispFilePath = filePath + "\\DISPUTED";
		
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(new File(filePath+"\\All").toPath(), ".txt");
		DocumentCorpus jayCorpus = DirectoryCorpus.loadTextDirectory(new File(jayFilePath).toPath(), ".txt");
		DocumentCorpus hamiltonCorpus = DirectoryCorpus.loadTextDirectory(new File(hamiltonFilePath).toPath(), ".txt");
		DocumentCorpus madisonCorpus = DirectoryCorpus.loadTextDirectory(new File(madisonFilePath).toPath(), ".txt");
		DocumentCorpus disputedCorpus = DirectoryCorpus.loadTextDirectory(new File(dispFilePath).toPath(), ".txt");
		
		Index index = indexCorpus(corpus, filePath + "\\All" + "\\indexBin");
		RocchioClassifier rocchioClassifier = new RocchioClassifier(index,corpus, jayCorpus, hamiltonCorpus, madisonCorpus, disputedCorpus);
		
		HashMap<String, String> res = rocchioClassifier.classify();
		//System.out.println("Size = " + res.size());
		for(Map.Entry<String, String> entry : res.entrySet())
		{
			System.out.println(entry.getKey() + " Author = " + entry.getValue());
		}
		
	}
}
