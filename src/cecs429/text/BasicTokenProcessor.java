package cecs429.text;

import java.util.ArrayList;
import java.util.Arrays;
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
		token = getAplhaNumericToken(token);
		token = token.toLowerCase();
		List<String> tokenList = processHypenToken(token);
		return tokenList;
	}
	
	private List<String> processHypenToken(String token)
	{
		List<String> listHyphenToken = new ArrayList<String>(); 
		if(token.contains(STR_HYPHEN))
		{
			StringTokenizer tokenizer = new StringTokenizer(token,"-");
			StringBuilder finalString = new StringBuilder();
			while(tokenizer.hasMoreTokens())
			{
				String temp = tokenizer.nextToken();
				finalString.append(temp);
				listHyphenToken.add(m_Stemmer.GetStemmedToken(temp));
			}
			listHyphenToken.add(0,finalString.toString());
		}
		else
		{
			listHyphenToken.add(m_Stemmer.GetStemmedToken(token));
		}
		return listHyphenToken;
	}
	
	private String getAplhaNumericToken(String token)
	{
		if(token.length() == 0 || token.length() == 1)
			return token;
		
		char[] chArray = token.toCharArray();
		int startIndex = 0;
		int endIndex = chArray.length-1;
		boolean startIndexFound = false, endIndexFound = false;
		while(true)
		{

			if(Character.isAlphabetic(chArray[startIndex]) 
				|| Character.isDigit(chArray[startIndex])) 
			{
				if(startIndexFound == false)
				{	
					startIndexFound = true;
				}
			}
			else
				startIndex++;
			

			if(Character.isAlphabetic(chArray[endIndex]) 
				|| Character.isDigit(chArray[endIndex])) 
			{
				if(endIndexFound == false)
				{	
					endIndexFound = true;
				}
			}
			else
				endIndex--;
			
			if(startIndex >= endIndex)
				return token;
			if(startIndexFound && endIndexFound)
				break;
			
		}
		return token.substring(startIndex, endIndex+1);
	}
		
}
