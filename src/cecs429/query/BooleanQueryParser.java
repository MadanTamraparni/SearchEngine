package cecs429.query;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

/**
 * Parses boolean queries according to the base requirements of the CECS 429 project.
 * Does not handle phrase queries, NOT queries, NEAR queries, or wildcard queries... yet.
 */
public class BooleanQueryParser {
	/**
	 * Identifies a portion of a string with a starting index and a length.
	 */
	private static class StringBounds {
		int start;
		int length;
		
		StringBounds(int start, int length) {
			this.start = start;
			this.length = length;
		}
	}
	
	/**
	 * Encapsulates a QueryComponent and the StringBounds that led to its parsing.
	 */
	private static class Literal {
		StringBounds bounds;
		QueryComponent literalComponent;
		
		Literal(StringBounds bounds, QueryComponent literalComponent) {
			this.bounds = bounds;
			this.literalComponent = literalComponent;
		}
	}
	
	/**
	 * Given a boolean query, parses and returns a tree of QueryComponents representing the query.
	 */
	public QueryComponent parseQuery(String query) {
		int start = 0;
		
		// General routine: scan the query to identify a literal, and put that literal into a list.
		//	Repeat until a + or the end of the query is encountered; build an AND query with each
		//	of the literals found. Repeat the scan-and-build-AND-query phase for each segment of the
		// query separated by + signs. In the end, build a single OR query that composes all of the built
		// AND subqueries.
		
		List<QueryComponent> allSubqueries = new ArrayList<>();
		EnglishTokenStream eng = new EnglishTokenStream(new StringReader(query));
		Iterable<String> strIter = eng.getTokens();
		StringBuilder strBuilder = new StringBuilder();
		for(String token : strIter)
		{
			TokenProcessor processor = new BasicTokenProcessor();
			List<String> subQueryList = processor.enhancedProcessToken(token);
		
			//for(String i : subQueryList) System.out.print(i + " ");
			for(String s : subQueryList)
			{
				if(token.charAt(0) == '-')
				{
					strBuilder.append("-"+s);
					strBuilder.append(" ");
				}
				else
				{
					strBuilder.append(s);
					strBuilder.append(" ");
				}
					
			}
		}
		query = strBuilder.toString();
		query = query.substring(0,query.length() - 1);
		
		do 
		{
			// Identify the next subquery: a portion of the query up to the next + sign.
			StringBounds nextSubquery = findNextSubquery(query, start);
			//System.out.println("Sub = " + nextSubquery);
			// Extract the identified subquery into its own string.
			String subquery = query.substring(nextSubquery.start, nextSubquery.start + nextSubquery.length);

			int subStart = 0;
			// this is subquery print. REMOVE AFTER TESTING
			System.out.println(subquery);
			// Store all the individual components of this subquery.
			List<QueryComponent> subqueryLiterals = new ArrayList<>(0);

			do {
				// Extract the next literal from the subquery.
				Literal lit = findNextLiteral(subquery, subStart);
				
				// Add the literal component to the conjunctive list.
				/**** Check if the literal component is negative(NOT)****/
				QueryComponent literalComponent = lit.literalComponent;
				
				if(literalComponent.isNegative()){
					subqueryLiterals.add(new NotQuery(literalComponent));
				}else{
					subqueryLiterals.add(literalComponent);
				}
				/*******************************************************/
				
				// Set the next index to start searching for a literal.
				subStart = lit.bounds.start + lit.bounds.length;
				
			} while (subStart < subquery.length());
			
			// After processing all literals, we are left with a conjunctive list
			// of query components, and must fold that list into the final disjunctive list
			// of components.
			
			// If there was only one literal in the subquery, we don't need to AND it with anything --
			// its component can go straight into the list.
			if (subqueryLiterals.size() == 1) {
				allSubqueries.add(subqueryLiterals.get(0));
			}
			else {
				// With more than one literal, we must wrap them in an AndQuery component.
				allSubqueries.add(new AndQuery(subqueryLiterals));
			}
			start = nextSubquery.start + nextSubquery.length;
		} while (start < query.length());

		
		// After processing all subqueries, we either have a single component or multiple components
		// that must be combined with an OrQuery.
		if (allSubqueries.size() == 1) {
			return allSubqueries.get(0);
		}
		else if (allSubqueries.size() > 1) {
			return new OrQuery(allSubqueries);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Locates the start index and length of the next subquery in the given query string,
	 * starting at the given index.
	 */
	private StringBounds findNextSubquery(String query, int startIndex) {
		int lengthOut;
		
		// Find the start of the next subquery by skipping spaces and + signs.
		char test = query.charAt(startIndex);
		while (test == ' ' || test == '+') {
			test = query.charAt(++startIndex);
		}
		
		// Find the end of the next subquery.
		int nextPlus = query.indexOf('+', startIndex + 1);
		
		if (nextPlus < 0) {
			// If there is no other + sign, then this is the final subquery in the
			// query string.
			lengthOut = query.length() - startIndex;
		}
		else {
			// If there is another + sign, then the length of this subquery goes up
			// to the next + sign.
		
			// Move nextPlus backwards until finding a non-space non-plus character.
			// Purpose: To remove all white space and plus. Give back the term
			test = query.charAt(nextPlus);
			while (test == ' ' || test == '+') {
				test = query.charAt(--nextPlus);
			}
			
			lengthOut = 1 + nextPlus - startIndex;
		}
		
		// startIndex and lengthOut give the bounds of the subquery.
		return new StringBounds(startIndex, lengthOut);
	}
	
	/**
	 * Locates and returns the next literal from the given subquery string.
	 */
	private Literal findNextLiteral(String subquery, int startIndex) {
		int subLength = subquery.length();
		int lengthOut;
		boolean isNegative = false;
		// Skip past white space.
		while (subquery.charAt(startIndex) == ' ') {
			++startIndex;
		}
		
		/**Check if the query starts with a negative sign***/
		if(subquery.charAt(startIndex) == '-'){
			isNegative = true;
			startIndex += 1;
		}
		/***************************************************/
		
		//Code to check for phrase queries, if not present, default code should be executed
		if(subquery.charAt(startIndex) == '"')
		{
			++startIndex;
			int closePhrase = subquery.indexOf('"',startIndex);
			lengthOut = closePhrase - startIndex; // Assuming that there is close phrase
			
			return new Literal(new StringBounds(startIndex, lengthOut), 
					new PhraseLiteral(subquery.substring(startIndex, startIndex + lengthOut), isNegative));
		}
		// else if(subquery.charAt(startIndex) == '('){

		// }
		else if(subquery.charAt(startIndex) == '['){
			++startIndex;
			int endNearLiteral = subquery.indexOf(']',startIndex);
			lengthOut = endNearLiteral - startIndex;
			// Might need fix on how long the token is
			// Wating on Madan
			String[] nearLiteral = subquery.substring(startIndex, startIndex + lengthOut).split(" ");

			return new Literal(new StringBounds(startIndex, lengthOut),
					new NearLiteral(nearLiteral[0], 
									Character.getNumericValue(nearLiteral[1].charAt(nearLiteral[1].length() - 1) - '0'), 
									nearLiteral[2]));
		}
		else
		{
			// Locate the next space to find the end of this literal.
			int nextSpace = subquery.indexOf(' ', startIndex);
			if (nextSpace < 0) {
				// No more literals in this subquery.
				lengthOut = subLength - startIndex;
			}
			else {
				lengthOut = nextSpace - startIndex;
			}
			
			// This is a term literal containing a single term.
			return new Literal(
			 new StringBounds(startIndex, lengthOut),
			 new TermLiteral(subquery.substring(startIndex, startIndex + lengthOut), isNegative));
		}
		/*
		TODO:
		Instead of assuming that we only have single-term literals, modify this method so it will create a PhraseLiteral
		object if the first non-space character you find is a double-quote ("). In this case, the literal is not ended
		by the next space character, but by the next double-quote character.
		 */
	}
}
