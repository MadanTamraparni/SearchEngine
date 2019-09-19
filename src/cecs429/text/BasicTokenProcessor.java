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
		token = getAplhaNumericToken(token);
		System.out.println(token);
		token = token.replaceAll("\\W", "").toLowerCase();
		System.out.println(token);
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
				//System.out.println(m_Stemmer.GetStemmedToken(tokenizer.nextToken()));
				listHyphenToken.add(m_Stemmer.GetStemmedToken(tokenizer.nextToken()));
			}
		}
		else
		{
			//System.out.println("Stemmed  = " + m_Stemmer.GetStemmedToken(token));
			listHyphenToken.add(m_Stemmer.GetStemmedToken(token));
		}
		return listHyphenToken;
	}
	
	private String getAplhaNumericToken(String token)
	{
		char[] chArray = token.toCharArray();
		for(int i=0; i < chArray.length; i++)
		{
			if(Character.isAlphabetic(chArray[i]) || Character.isDigit(chArray[i]))
			{
				token =  token.substring(i);
				//System.out.println("Pre = " + token);
				break;
			}
		}
		chArray = token.toCharArray();
		for(int j = chArray.length-1;j>0;j--)
		{
			//System.out.println(chArray[j]);
			if(Character.isAlphabetic(chArray[j]) || Character.isDigit(chArray[j]))
			{
				token =  token.substring(0, j+1);
				//System.out.println("Post = " + token);
				break;
			}
		}
		return token;
	}
		
}
