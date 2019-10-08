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
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;

public class SearchEngineGui extends JFrame {
	
	private Index mIndex;
	private BooleanQueryParser mQueryParser = new BooleanQueryParser();
	private DocumentCorpus mCorpus;
	
	private JPanel mContentPane;
	private JTextField mTextDirectory;
	private JTextField mTextSearch;
	private List<Posting> mPostingList;
	
	public static final String STEM_STR = "stem";
	public static final String QUIT_STR = "q";
	public static final String INDEX_STR = "index";
	public static final String VOCAB_STR = "vocab";

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
		setBounds(100, 100, 561, 430);
		mContentPane = new JPanel();
		mContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mContentPane);
		mContentPane.setLayout(null);
		
		//text field for directory
		mTextDirectory = new JTextField();
		mTextDirectory.setBounds(6, 5, 459, 20);
		mContentPane.add(mTextDirectory);
		mTextDirectory.setColumns(10);
		
		//button for indexing
		JButton directoryButton = new JButton("Index");
		directoryButton.setBounds(468, 6, 87, 20);
		mContentPane.add(directoryButton);
		
		//text field for search
		mTextSearch = new JTextField();
		mTextSearch.setBounds(6, 30, 459, 20);
		mContentPane.add(mTextSearch);
		mTextSearch.setColumns(10);
		
		//button for searching
		JButton searchButton = new JButton("Search");
		searchButton.setBounds(468, 30, 87, 20);
		mContentPane.add(searchButton);
		
		//List
		JScrollPane scrollPane;
		DefaultListModel<String> listModel = new DefaultListModel<>();
        
        JList<String> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter(){
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
        scrollPane = new JScrollPane(list);
        scrollPane.setBounds(6, 140, 549, 265);
        mContentPane.add(scrollPane);
		
		//Text Area
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(6, 62, 549, 72);
		mContentPane.add(scrollPane_1);
		JTextArea textArea = new JTextArea();
		scrollPane_1.setViewportView(textArea);
		
		//action listener for index button: getting the corpus and indexing terms
		directoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path;
				while(true){
		            path = mTextDirectory.getText();
		            File testDir = new File(path);
		            if(testDir.isDirectory()){
		            	textArea.append("Directory Existed. Procceed to indexing..."); 
						break;
					}
		            textArea.append("Directory does not exist.");
		        }
				
				long timeStart = System.currentTimeMillis();
		        mCorpus = DirectoryCorpus.loadJsonDirectory(new File(path).toPath(),".json");
		        mIndex = indexCorpus(mCorpus);
		        long timeEnd = System.currentTimeMillis();
		        long time = timeEnd - timeStart;
		        double seconds = time / 1000.0;
		        int min = (int)(seconds/60.0), intSecond = (int)(seconds%60);
		        textArea.append(" Time to create index "+ min + " minutes " + intSecond + " seconds." + "\n");
			}
		});
		
		//action listener for search button: searching terms
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String query;
				TokenProcessor  processor = new BasicTokenProcessor();
				
				query = mTextSearch.getText();
	            if(query.equals(QUIT_STR))
	            {
	            	System.exit(0);
	            	return;
	            }
	            else if(query.contains(STEM_STR))
	            {
	            	query = query.substring(STEM_STR.length()+1);
	            	PorterStemmer stemmer = new PorterStemmer();
	            	textArea.append("Stemmer Token = " + stemmer.GetStemmedToken(query) + "\n");
	            	return;
	            }
	            else if(query.contains(INDEX_STR))
	            {
	            	query = query.substring(INDEX_STR.length()+1);
	            	File testDir = new File(query);
	                if(testDir.isDirectory()){
	                	textArea.append("Directory Existed. Procceed to indexing...\n"); 
	    				long timeStart = System.currentTimeMillis();
	        			mCorpus = DirectoryCorpus.loadJsonDirectory(new File(query).toPath(),".json");
	        			mIndex = indexCorpus(mCorpus) ;
	        			long timeEnd = System.currentTimeMillis();
	        			long time = timeEnd - timeStart;
	    		        double seconds = time / 1000.0;
	    		        int min = (int)(seconds/60.0), intSecond = (int)(seconds%60);
	    		        textArea.append(" Time to create index "+ min + " minutes " + intSecond + " seconds." + "\n");
	    			}else{
	    				textArea.append("Directory does not exist.\n");
	    			}
	                return;
	            }
	            else if(query.contains(VOCAB_STR))
	            {
	            	List<String> vocabList = mIndex.getVocabulary();
	            	for(int i =0; i < 1000; i++){
	            		textArea.append(vocabList.get(i) + "\n");
	            	}
	            	textArea.append("Size of the Vocabulary = " + vocabList.size() + "\n");
	            	return;
	            }
	            
	            if(query.length() == 0){
	            	return;
	            }
	            
	            QueryComponent queryComponent = mQueryParser.parseQuery(query);
	            listModel.clear();
	            mPostingList = queryComponent.getPostings(mIndex, processor);
	            for (Posting p : mPostingList) {
					listModel.addElement("Title: " + mCorpus.getDocument(p.getDocumentId()).getTitle() + "\n");
	            }
	            listModel.addElement("Posting List size = " + queryComponent.getPostings(mIndex, processor).size() + "\n");
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
	
	
	
	/**For indexing corpus**/
	private static Index indexCorpus(DocumentCorpus corpus) {
		BasicTokenProcessor processor = new BasicTokenProcessor();
		
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
			}
		}
		return index;
	}
}
