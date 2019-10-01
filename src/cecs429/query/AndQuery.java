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
		
		
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		List<Posting> resultPostings = new ArrayList<Posting>();
		
		
		//Get the first and second QueryComponents
		QueryComponent firstQueryComp = mComponents.get(0);
		QueryComponent secondQueryComp = mComponents.get(1);
		
		//Get the postings of the first and second QueryComponents
		List<Posting> firstQueryPostings = firstQueryComp.getPostings(index, processor);
		List<Posting> secondQueryPostings = secondQueryComp.getPostings(index, processor);
		
		//Check if either of posting lists is empty
		if(firstQueryPostings.size() == 0 || secondQueryPostings.size() == 0){
			return resultPostings;
		}
		
		/**Get the AND or NOT AND result of the first and the second QueryComponents**/
		
		//Get the AND NOT
		if(firstQueryComp.isNegative() || secondQueryComp.isNegative()){
			List<Posting> positivePostings;
			List<Posting> negativePostings;
			if(firstQueryComp.isNegative()){
				positivePostings = secondQueryPostings;
				negativePostings = firstQueryPostings;
			}else{
				positivePostings = firstQueryPostings;
				negativePostings = secondQueryPostings;
			}
			int pos = 0;
			int neg = 0;
			int currentPositiveDocId;
			int currentNegativeDocId;
			while(true){
				currentPositiveDocId = positivePostings.get(pos).getDocumentId();
				currentNegativeDocId = negativePostings.get(neg).getDocumentId();
				if(currentPositiveDocId == currentNegativeDocId){
					pos++;
					neg++;
					if(neg == negativePostings.size()){
						for(int i = pos; i < positivePostings.size(); i++){
							resultPostings.add(positivePostings.get(pos));
							pos++;
						}
						break;
					}else if(pos == positivePostings.size()){
						break;
					}
				}
				else if(currentPositiveDocId > currentNegativeDocId){
					neg++;
					if(neg == negativePostings.size()){
						for(int i = pos; i < positivePostings.size(); i++){
							resultPostings.add(positivePostings.get(pos));
							pos++;
						}
						break;
					}
				}
				else{
					resultPostings.add(positivePostings.get(pos));
					pos++;
					if(pos == positivePostings.size()){
						break;
					}
				}
			}
		}
		//Get the AND
		else{
			int firstPosition = 0;
			int secondPosition = 0;
			int currentFirstDocId;
			int currentSecondDocId;
			while(true){
				currentFirstDocId = firstQueryPostings.get(firstPosition).getDocumentId();
				currentSecondDocId = secondQueryPostings.get(secondPosition).getDocumentId();
				if(currentFirstDocId == currentSecondDocId){
					resultPostings.add(firstQueryPostings.get(firstPosition));
					firstPosition++;
					secondPosition++;
				}else if(currentFirstDocId < currentSecondDocId){
					firstPosition++;
				}else{
					secondPosition++;
				}
				
				if(firstPosition == firstQueryPostings.size() || secondPosition == secondQueryPostings.size()){
					break;
				}
			}
		}
		
		/**Get the AND or AND NOT result of the following QueryComponents**/
		
		if(mComponents.size() > 2){
			for(int i = 2; i < mComponents.size(); i++){
				
				QueryComponent currentQueryComp = mComponents.get(i);
				List<Posting> currentQueryPostings = currentQueryComp.getPostings(index, processor);
				List<Posting> tempResultPostings = new ArrayList<Posting>();
				
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
						currentFirstDocId = resultPostings.get(firstPosition).getDocumentId();
						currentSecondDocId = currentQueryPostings.get(secondPosition).getDocumentId();
						if(currentFirstDocId == currentSecondDocId){
							tempResultPostings.add(resultPostings.get(firstPosition));
							firstPosition++;
							secondPosition++;
						}else if(currentFirstDocId < currentSecondDocId){
							firstPosition++;
						}else{
							secondPosition++;
						}
						
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
						currentPositiveDocId = resultPostings.get(pos).getDocumentId();
						currentNegativeDocId = currentQueryPostings.get(neg).getDocumentId();
						if(currentPositiveDocId == currentNegativeDocId){
							pos++;
							neg++;
						}
						else if(currentPositiveDocId > currentNegativeDocId){
							neg++;
							if(neg == currentQueryPostings.size()){
								for(int j = pos; j < resultPostings.size(); j++){
									tempResultPostings.add(resultPostings.get(pos));
									pos++;
								}
								break;
							}
						}
						else{
							tempResultPostings.add(resultPostings.get(pos));
							pos++;
							if(pos == resultPostings.size()){
								break;
							}
						}
					}
				}
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
