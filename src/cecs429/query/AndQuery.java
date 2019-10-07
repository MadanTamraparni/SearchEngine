package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	
	private List<QueryComponent> mComponents;
	
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
		
		List<Posting> resultPostings = new ArrayList<Posting>();
		
		//Get the first and second QueryComponents
		QueryComponent firstQueryComp = mComponents.get(0);
		QueryComponent secondQueryComp = mComponents.get(1);
		
		//Get the postings of the first and second QueryComponents
		List<Posting> firstQueryPostings = firstQueryComp.getPostings(index, processor);
		List<Posting> secondQueryPostings = secondQueryComp.getPostings(index, processor);
		
		//Check if either of the posting lists is empty
		if(firstQueryPostings.size() == 0 || secondQueryPostings.size() == 0){
			return resultPostings;
		}
		
		/**Get the AND or NOT AND result of the FIRST and the SECOND QueryComponents**/
		
		//Get the AND NOT
		if(firstQueryComp.isNegative() || secondQueryComp.isNegative()){
			
			List<Posting> positivePostings;
			List<Posting> negativePostings;
			
			//Determine which query is negative
			if(firstQueryComp.isNegative()){
				positivePostings = secondQueryPostings;
				negativePostings = firstQueryPostings;
			}else{
				positivePostings = firstQueryPostings;
				negativePostings = secondQueryPostings;
			}
			
			int pos = 0;
			int neg = 0;
			int currentPositiveDocId; //current docID of the positive query's postings list
			int currentNegativeDocId; //current docID of the negative query's postings list
			
			while(true){
				currentPositiveDocId = positivePostings.get(pos).getDocumentId();
				currentNegativeDocId = negativePostings.get(neg).getDocumentId();
				
				//if positive docID == negative docID 
				if(currentPositiveDocId == currentNegativeDocId){
					pos++;
					neg++;
					
					//if finish going through all negative postings list
					if(neg == negativePostings.size()){
						
						//add the rest of positive postings list to result
						for(int i = pos; i < positivePostings.size(); i++){
							resultPostings.add(positivePostings.get(pos));
							pos++;
						}
						break; 
					}
					
					//if finish going through all positive postings list
					else if(pos == positivePostings.size()){
						break;
					}
				}
				
				//if positive docID > negative docID
				else if(currentPositiveDocId > currentNegativeDocId){
					neg++;
					
					//if finish going through all negative postings list
					if(neg == negativePostings.size()){
						
						//add the rest of positive postings list to result
						for(int i = pos; i < positivePostings.size(); i++){
							resultPostings.add(positivePostings.get(pos));
							pos++;
						}
						break;
					}
				}
				
				//if positive docID < negative docID 
				else{
					
					//add current positive posting to result
					resultPostings.add(positivePostings.get(pos));
					pos++;
					
					//if finish going through all positive postings list
					if(pos == positivePostings.size()){
						break;
					}
				}
			}
		}
		//Get the AND
		else{
			int firstPosition = 0;  //first query postings list's position
			int secondPosition = 0; //second query postings list's position 
			int currentFirstDocId;
			int currentSecondDocId;
			
			while(true){
				currentFirstDocId = firstQueryPostings.get(firstPosition).getDocumentId();
				currentSecondDocId = secondQueryPostings.get(secondPosition).getDocumentId();
				
				//if first docID == second docID
				if(currentFirstDocId == currentSecondDocId){
					
					//add the docID
					resultPostings.add(firstQueryPostings.get(firstPosition));
					firstPosition++;
					secondPosition++;
				}
				//if first docID < second docID
				else if(currentFirstDocId < currentSecondDocId){
					firstPosition++;
				}
				//if first docID > second docID
				else{
					secondPosition++;
				}
				
				//if finish going through either all positive or negative postings lists
				if(firstPosition == firstQueryPostings.size() || secondPosition == secondQueryPostings.size()){
					break;
				}
			}
		}
		
		/**Get the AND or AND NOT result of the following QueryComponents**/
		
		if(mComponents.size() > 2){
			for(int i = 2; i < mComponents.size(); i++){
				
				List<Posting> tempResultPostings = new ArrayList<Posting>();
				
				//get the current QueryComponent
				QueryComponent currentQueryComp = mComponents.get(i);
				
				//get the current query's postings list
				List<Posting> currentQueryPostings = currentQueryComp.getPostings(index, processor);
				
				//Check if current QueryComponent has 0 postings
				if(currentQueryPostings.size() == 0){
					return resultPostings;
				}
				
				//AND query
				if(!currentQueryComp.isNegative()){
					
					int firstPosition = 0;
					int secondPosition = 0;
					int currentFirstDocId;
					int currentSecondDocId;
					
					while(true){
						currentFirstDocId = resultPostings.get(firstPosition).getDocumentId(); //get the docID of current result's postings list
						currentSecondDocId = currentQueryPostings.get(secondPosition).getDocumentId(); //get the docID of current query's postings list
						
						//if first docID == second docID
						if(currentFirstDocId == currentSecondDocId){
							
							//add the docID to temporary result
							tempResultPostings.add(resultPostings.get(firstPosition));
							firstPosition++;
							secondPosition++;
						}
						//if first docID < second docID
						else if(currentFirstDocId < currentSecondDocId){
							firstPosition++;
						}
						//if first docID > second docID
						else{
							secondPosition++;
						}
						//if finish going through either all positive or negative postings lists
						if(firstPosition == resultPostings.size() || secondPosition == currentQueryPostings.size()){
							break;
						}
					}
				}
				//AND NOT query
				else{
					int pos = 0;
					int neg = 0;
					int currentPositiveDocId;
					int currentNegativeDocId;
					
					while(true){
						currentPositiveDocId = resultPostings.get(pos).getDocumentId(); //get the docID of current result's postings list
						currentNegativeDocId = currentQueryPostings.get(neg).getDocumentId(); //get the docID of current query's postings list
						
						//if positive docID == negative docID
						if(currentPositiveDocId == currentNegativeDocId){
							pos++;
							neg++;
						}
						//if positive docID > negative docID
						else if(currentPositiveDocId > currentNegativeDocId){
							neg++;
							//if finish going through all negative postings list
							if(neg == currentQueryPostings.size()){
								for(int j = pos; j < resultPostings.size(); j++){
									//add the rest of positive postings list to temporary result
									tempResultPostings.add(resultPostings.get(pos));
									pos++;
								}
								break;
							}
						}
						//if positive docID < negative docID
						else{
							
							//add current positive posting to temporary result
							tempResultPostings.add(resultPostings.get(pos));
							pos++;
							
							//if finish going through all positive postings list
							if(pos == resultPostings.size()){
								break;
							}
						}
					}
				}
				//set temporary result to current result
				resultPostings = tempResultPostings;
			}
		}
		
		return resultPostings;
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}

	@Override
	public boolean isNegative() {
		return false;
	}
}
