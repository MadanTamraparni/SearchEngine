package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.FileDocument;
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


import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;

public class SearchEngineGui extends JFrame {
	
	private Index mIndex;
	private BooleanQueryParser mQueryParser = new BooleanQueryParser();
	private DocumentCorpus mCorpus;
	private String mCorpusPath = null;
	private String mDiskWritePath = null;
	private int mCorpusSize;
	private String mSearchMethod = null;
	private String mQuery = null;
	private RandomAccessFile mDocWeightsRaf;
	private final int MAX_INDEX_SIZE = 30000;
	
	private JPanel mContentPane;
	private JTextField mCorpusPathTextField;
	private JTextField mQueryTextField;
	private List<Posting> mPostingList;
	
	public static final String STEM_STR = "stem";
	public static final String QUIT_STR = "q";
	public static final String INDEX_STR = "index";
	public static final String VOCAB_STR = "vocab";
	private JTextField diskWritePathTextField;
	private JTextField searchTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchEngineGui frame = new SearchEngineGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	/**
	 * Create the frame.
	 */
	
	public SearchEngineGui() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 566, 626);
		mContentPane = new JPanel();
		mContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mContentPane);
		mContentPane.setLayout(null);
		
		//text field for directory
		mCorpusPathTextField = new JTextField();
		mCorpusPathTextField.setBounds(6, 5, 459, 20);
		mContentPane.add(mCorpusPathTextField);
		mCorpusPathTextField.setColumns(10);
		
		//button for corpus path
		JButton corpusButton = new JButton("Corpus");
		corpusButton.setBounds(468, 6, 87, 20);
		mContentPane.add(corpusButton);
		
		//text field for query
		mQueryTextField = new JTextField();
		mQueryTextField.setBounds(6, 57, 459, 20);
		mContentPane.add(mQueryTextField);
		mQueryTextField.setColumns(10);
		
		//button for query
		JButton queryButton = new JButton("Query");
		queryButton.setBounds(468, 58, 87, 20);
		mContentPane.add(queryButton);
		
		//List for search results
		JScrollPane scrollPane;
		DefaultListModel<String> listModel = new DefaultListModel<>();
        
        JList<String> resultsList = new JList<>(listModel);
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.addMouseListener(new MouseAdapter(){
        	public void mouseClicked(MouseEvent e){
        		JList list = (JList)e.getSource(); //get the source of the event
        		int index;
        		//Check if it's a double-click
        		if(e.getClickCount() == 2){
        			index = list.locationToIndex(e.getPoint()); //get the index of the element that is double clicked
        			if(index == mPostingList.size())
        				return;
        			Posting p = mPostingList.get(index);
        			Document d = mCorpus.getDocument(p.getDocumentId());
        			BufferedReader br = (BufferedReader) d.getContent();
        			String line = null;
        			StringBuilder rslt = new StringBuilder();
        			try {
						while ((line = br.readLine()) != null) {
						    rslt.append(line);
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        			openContentWindow(rslt.toString()); //pass the content to be displayed on the new window
        		}
        	}
        });
        scrollPane = new JScrollPane(resultsList);
        scrollPane.setBounds(6, 312, 549, 286);
        mContentPane.add(scrollPane);
		
		//Text Area for indexing process
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(6, 131, 549, 179);
		mContentPane.add(scrollPane_1);
		JTextArea generalTextArea = new JTextArea();
		scrollPane_1.setViewportView(generalTextArea);
		
		//text field for disk write path 
		diskWritePathTextField = new JTextField();
		diskWritePathTextField.setBounds(6, 29, 459, 26);
		mContentPane.add(diskWritePathTextField);
		diskWritePathTextField.setColumns(10);
		
		//button for disk write path 
		JButton diskWriteButton = new JButton("Bin File");
		diskWriteButton.setBounds(468, 33, 87, 20);
		mContentPane.add(diskWriteButton);
		
		//button for index 
		JButton indexButton = new JButton("Index");
		indexButton.setBounds(6, 108, 549, 20);
		mContentPane.add(indexButton);
		
		//text field for search 
		searchTextField = new JTextField();
		searchTextField.setColumns(10);
		searchTextField.setBounds(6, 83, 459, 20);
		mContentPane.add(searchTextField);
		searchTextField.setText("1 for Ranked Retrieval / 2 for Boolean Retrieval");
		
		//button for search
		JButton searchButton = new JButton("Search");
		searchButton.setBounds(468, 84, 87, 20);
		mContentPane.add(searchButton);
		
		
		//action listener for CORPUS BUTTON: get corpus path
		corpusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
		        mCorpusPath = mCorpusPathTextField.getText();
		        generalTextArea.append("Entered corpus path: " + mCorpusPath + "\n");
		        File testDir = new File(mCorpusPath);
		        if(testDir.isDirectory()){
		        	generalTextArea.append("Directory Existed...\n"); 
				}else {
					mCorpusPath = null;
			        generalTextArea.append("Directory does not exist.\n");
			        return;
				}
			}
		});
		
		//action listener for DISK WRITE BUTTON: get disk write path
		diskWriteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mDiskWritePath = diskWritePathTextField.getText();
				generalTextArea.append("Entered disk write path: " + mDiskWritePath + "\n");
		        File testDir = new File(mDiskWritePath);
		        if(testDir.isDirectory()){
		        	generalTextArea.append("Directory Existed...\n");
				}else {
					mDiskWritePath = null;
			        generalTextArea.append("Directory does not exist.\n");
			        return;
				}
			}
		});
		
		//action listener for INDEX BUTTON: index the corpus at the given disk path
		indexButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(mCorpusPath == null || mDiskWritePath == null) {
					generalTextArea.append("Corpus Path and Disk Write Path must be entered first.\n");
					return;
				}
				
				long timeStart = System.currentTimeMillis();
				mCorpus = DirectoryCorpus.loadJsonDirectory(new File(mCorpusPath).toPath(), ".json");
				mCorpusSize = mCorpus.getCorpusSize();
				mIndex = indexCorpus(mCorpus, mDiskWritePath);
				long timeEnd = System.currentTimeMillis();
				long time = timeEnd - timeStart;
		        double seconds = time / 1000.0;
		        int min = (int)(seconds/60.0), intSecond = (int)(seconds%60);
		        generalTextArea.append(" Time to create index "+ min + " minutes " + intSecond + " seconds." + "\n");
		        
		        try {
					mDocWeightsRaf = new RandomAccessFile(new File(mDiskWritePath + "/docWeights.bin"), "r");
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		

		//action listener for QUERY BUTTON: search terms
		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mQuery = mQueryTextField.getText();
				generalTextArea.append("Entered Query: " + mQuery +"\n");
				if(mQuery.equals(QUIT_STR))
	            {
					System.exit(0);
	            	return;
	            }
	            else if(mQuery.contains(STEM_STR))
	            {
	            	mQuery = mQuery.substring(STEM_STR.length()+1);
	            	PorterStemmer stemmer = new PorterStemmer();
	            	generalTextArea.append("Stemmer Token = " + stemmer.GetStemmedToken(mQuery) + "\n");
	            	mQuery = null;
	            	return;
	            }
	            else if(mQuery.contains(INDEX_STR))
	            {
	            	mQuery = mQuery.substring(INDEX_STR.length()+1);
	            	mCorpusPath = mQuery;
	            	generalTextArea.append("Entered corpus path: " + mCorpusPath + "\n");
			        File testDir = new File(mCorpusPath);
			        if(testDir.isDirectory()){
			        	generalTextArea.append("Directory Existed...\n"); 
					}else {
						mCorpusPath = null;
						mQuery = null;
				        generalTextArea.append("Directory does not exist.\n");
				        return;
					}
			        
			        long timeStart = System.currentTimeMillis();
					mCorpus = DirectoryCorpus.loadJsonDirectory(new File(mCorpusPath).toPath(), ".json");
					mCorpusSize = mCorpus.getCorpusSize();
					mIndex = indexCorpus(mCorpus, mDiskWritePath);
					long timeEnd = System.currentTimeMillis();
					long time = timeEnd - timeStart;
			        double seconds = time / 1000.0;
			        int min = (int)(seconds/60.0), intSecond = (int)(seconds%60);
			        generalTextArea.append(" Time to create index "+ min + " minutes " + intSecond + " seconds." + "\n");
			        mQuery = null;
	            	
	            }
	            else if(mQuery.contains(VOCAB_STR))
	            {
	            	List<String> vocabList = mIndex.getVocabulary();
	            	for(int i =0; i < 1000; i++){
	            		generalTextArea.append(vocabList.get(i) + "\n");
	            	}
	            	generalTextArea.append("Size of the Vocabulary = " + vocabList.size() + "\n");
	            	mQuery = null;
	            	return;
				}
	            if(mQuery.length() == 0){
	            	mQuery = null;
				}
			}
		});
		
		//Action Listener for SEARCH BUTTON
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(mQuery == null) {
					generalTextArea.append("Query must be entered first.\n");
					return;
				}
				TokenProcessor processor = new BasicTokenProcessor();
				if(mSearchMethod == null) {
					
					mSearchMethod = searchTextField.getText();
					if(!mSearchMethod.equals("1") && !mSearchMethod.equals("2")) {
						generalTextArea.append("Invalid selection\n");
						mSearchMethod = null;
						return;
					}
					
					generalTextArea.append("Selected method: " + mSearchMethod + "\n");
					if(mSearchMethod.equals("1")) {
						generalTextArea.append("Select model:\n");
						generalTextArea.append("1. Default rank model\n");
						generalTextArea.append("2. Okapi BM25 model\n");
						generalTextArea.append("3. Tf-idf model\n");
						generalTextArea.append("4. Wacky model\n");
						return;
					}else{
						QueryComponent queryComponent = mQueryParser.parseQuery(mQuery);
						listModel.clear();
						mPostingList = queryComponent.getPostings(mIndex, processor);
						for(Posting p: mPostingList) {
							listModel.addElement("Title: " + mCorpus.getDocument(p.getDocumentId()).getTitle() + "\n");
						}
						listModel.addElement("Posting List size = " + mPostingList.size() + "\n");
						mSearchMethod = null;
						return;
					}
				}
				
				String rankMethod = searchTextField.getText();
				if(!rankMethod.equals("1") && !rankMethod.equals("2") && !rankMethod.equals("3") && !rankMethod.equals("4")){
					generalTextArea.append("Invalid selection\n");
					return;
				}
				try {
					if(rankMethod.equals("1")) {
						mPostingList = RankedRetrieval.getResults(new DefaultModel(mIndex, mCorpusSize, mDocWeightsRaf, processor), mQuery);
					}else if(rankMethod.equals("2")) {
						mPostingList = RankedRetrieval.getResults(new BM25Model(mIndex, mCorpusSize, processor), mQuery);
					}else if(rankMethod.equals("3")) {
						mPostingList = RankedRetrieval.getResults(new TfidfModel(mIndex, mCorpusSize, mDocWeightsRaf, processor), mQuery);
					}else if(rankMethod.equals("4")) {
						mPostingList = RankedRetrieval.getResults(new WackyModel(mIndex, mCorpusSize, mDocWeightsRaf, processor), mQuery);
					}
				}catch(IOException e1) {
					e1.printStackTrace();
				}
				
				for(Posting p: mPostingList) {
					listModel.addElement("Title: " + mCorpus.getDocument(p.getDocumentId()).getTitle() + "\n");
				}
				listModel.addElement("Posting List size = " + mPostingList.size() + "\n");
				mSearchMethod = null;
			}
		});

	}
	
	
	
	
	/**Method for opening a new window**/
	private static void openContentWindow(String content){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = new JFrame();
					JTextArea text = new JTextArea();
					JPanel panel = new JPanel();
					
					panel.setBorder(new EmptyBorder(5, 5, 5, 5));
					frame.setContentPane(panel);
					panel.setLayout(null);
					
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setBounds(100, 100, 561, 430);
					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setBounds(5, 5, 549, 400);
					panel.add(scrollPane);
					scrollPane.setViewportView(text);
					text.setLineWrap(true);
					text.append(content);
					
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	/**Checking if file already exist**/
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
	
	/**For indexing corpus**/
	private Index indexCorpus(DocumentCorpus corpus, String path){
		//HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();

		// Get all the documents in the corpus by calling GetDocuments().
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.

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
