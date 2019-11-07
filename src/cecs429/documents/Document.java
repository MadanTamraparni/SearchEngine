package cecs429.documents;

import java.io.Reader;

/**
 * Represents a document in an index.
 */
public interface Document {
	/**
	 * The ID used by the index to represent the document.
	 */
	int getId();
	
	/**
	 * Gets a stream over the content of the document.
	 */
	Reader getContent();
	
	/**
	 * The title of the document, for displaying to the user.
	 */
	String getTitle();
<<<<<<< HEAD

=======
	
>>>>>>> 66a7f082b643120b31e9f75141def134c8d9048a
	long getByte();
}
