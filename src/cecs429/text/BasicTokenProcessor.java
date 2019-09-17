package cecs429.text;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class BasicTokenProcessor implements TokenProcessor {
	public final String STR_HYPHEN = "-";
	private final PorterStemmer m_Stemmer = new PorterStemmer();
	@Override
	public String processToken(String token) {
		token = m_Stemmer.GetStemmedToken(token);
		return token.replaceAll("\\W", "").toLowerCase();
	}
	
	@Override
	public List<String> enhancedProcessToken(String token) {
		// TODO Auto-generated method stub
		token = token.replaceAll("[^A-Za-z]", "");
		token = token.replaceAll("\\W", "").toLowerCase();
		List<String> tokenList = processHypenToken(token);
		return tokenList;
	}
	
	private List<String> processHypenToken(String token)
	{
		List<String> listHyphenToken = new ArrayList<String>(); 
		if(token.contains(STR_HYPHEN))
		{
			StringTokenizer tokenizer = new StringTokenizer(token,"-");
			while(tokenizer.hasMoreTokens())
			{
				listHyphenToken.add(m_Stemmer.GetStemmedToken(tokenizer.nextToken()));
			}
		}
		else
			listHyphenToken.add(m_Stemmer.GetStemmedToken(token));
		
		return listHyphenToken;
	}
		
}
