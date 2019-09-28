package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.NearLiteral;
import cecs429.query.QueryComponent;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.PorterStemmer;

import javax.swing.JTextField;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.awt.event.ActionEvent;

public class SearchEngineGui extends JFrame {
	
	Index index;
	BooleanQueryParser queryParser = new BooleanQueryParser();
	DocumentCorpus corpus;
	
	private JPanel contentPane;
	private JTextField txtDirectory;
	private JTextField txtSearch;
	
	public static final String STEM_STR = "stem";
	public static final String QUIT_STR = "q";
	public static final String INDEX_STR = "index";
	public static final String VOCAB_STR = "vocab";
	public static final char NEAR_STR = '[';

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
		setBounds(100, 100, 531, 392);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtDirectory = new JTextField();
		txtDirectory.setBounds(6, 6, 459, 20);
		txtDirectory.setText("Enter document directory");
		contentPane.add(txtDirectory);
		txtDirectory.setColumns(10);
		
		
		JButton directoryButton = new JButton("Return");
		directoryButton.setBounds(468, 6, 57, 20);
		contentPane.add(directoryButton);
		
		txtSearch = new JTextField();
		txtSearch.setBounds(6, 30, 459, 20);
		txtSearch.setText("Search ");
		contentPane.add(txtSearch);
		txtSearch.setColumns(10);
		
		JButton searchButton = new JButton("Return");
		searchButton.setBounds(468, 30, 57, 20);
		contentPane.add(searchButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 55, 519, 309);
		contentPane.add(scrollPane);
		
		JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		//Return button for getting the corpus and indexing terms
		directoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path;
				while(true){
		            path = txtDirectory.getText();
		            File testDir = new File(path);
		            if(testDir.isDirectory()){
		            	textArea.append("Directory Existed. Procceed to indexing..."); 
						break;
					}
		            textArea.append("Directory does not exist.");
		        }
				
				long timeStart = System.currentTimeMillis();
		        corpus = DirectoryCorpus.loadJsonDirectory(new File(path).toPath(),".json");
		        index = indexCorpus(corpus);
		        long timeEnd = System.currentTimeMillis();
		        long time = timeEnd - timeStart;
		        double seconds = time / 1000.0;
				textArea.append(seconds/60.0 + "minutes " + seconds%60 + "seconds." + "\n");
			}
		});
		
		//Return button for searching terms
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String query;
				
				while(true){
		            query = txtSearch.getText();
		            if(query.equals(QUIT_STR))
		            {
		            	System.out.println(QUIT_STR);
		            	textArea.append("q entry for quit.\n");
		            	break;
		            }
		            else if(query.contains(STEM_STR))
		            {
		            	System.out.println(STEM_STR);
		            	query = query.substring(STEM_STR.length()+1);
		            	PorterStemmer stemmer = new PorterStemmer();
		            	textArea.append("Stemmer Token = " + stemmer.GetStemmedToken(query) + "\n");
		            }
		            else if(query.contains(INDEX_STR))
		            {
		            	System.out.println(INDEX_STR);
		            }
		            else if(query.contains(VOCAB_STR))
		            {
		            	System.out.println(VOCAB_STR);
		            	List<String> vocabList = index.getVocabulary();
		            	for(int i =0; i < 1000; i++)
		            		textArea.append(vocabList.get(i) + "\n");
		            	textArea.append("Size of the Vocabulary = " + vocabList.size() + "\n");
		            	break;
		            }
		            else if(query.charAt(0) == NEAR_STR){
		            	System.out.println(NEAR_STR);
						String[] subString = query.split(" ");
						NearLiteral near = new NearLiteral(subString[0].substring(1, subString[0].length()), 
						subString[1].charAt(subString[1].length() - 1),
						subString[2].substring(0, subString[2].length()- 1));
						for (Posting p : near.getPostings(index)) {
							textArea.append("Document ID " + p.getDocumentId() + "\n");
							// Below print line only for tracing the index
							textArea.append("Title: " + corpus.getDocument(p.getDocumentId()).getTitle() + "\n");
						}
					}
		            if(query.length() == 0){
		            	continue;
		            }
		            
		            System.out.println("Search");
		            QueryComponent queryComponent = queryParser.parseQuery(query);
		            textArea.append("Size = " + queryComponent.getPostings(index).size() + "\n");
		            for (Posting p : queryComponent.getPostings(index)) {
		            	int docId = p.getDocumentId();
		            	//if(docId == 0)
		            		//docId = 1;
						//System.out.println("Document ID \"article" + docId + ".json\"");
						// Below print line only for tracing the index
						textArea.append("Title: " + corpus.getDocument(p.getDocumentId()).getTitle() + "\n");
		            }
		            break;
		           
		        }
			}
		});
		
		
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
