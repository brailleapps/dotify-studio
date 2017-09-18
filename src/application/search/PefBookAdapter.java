package application.search;

import org.daisy.braille.utils.pef.PEFBook;

import application.l10n.Messages;

/**
 * Provides an adapter for pef books suitable for display in a list (via {@link #toString()}.
 * @author Joel Håkansson
 */
public class PefBookAdapter {
	private final PEFBook book;
	private final String display;
	
	/**
	 * Creates a new pef book adapter with the supplied book.
	 * @param book the book
	 */
	public PefBookAdapter(PEFBook book) {
		this.book = book;
    	String untitled = Messages.MESSAGE_UNKNOWN_TITLE.localize();
    	String unknown = Messages.MESSAGE_UNKNOWN_AUTHOR.localize();
		Iterable<String> title = book.getTitle(); 
		Iterable<String> authors = book.getAuthors();
		this.display = Messages.MESSAGE_SEARCH_RESULT.localize((title==null?untitled:title.iterator().next()), (authors==null?unknown:authors.iterator().next()));
	}
	
	/**
	 * Gets the pef book in this adapter.
	 * @return returns the book
	 */
	public PEFBook getBook() {
		return book;
	}
	
	@Override
	public String toString() {
		return display;
	}
}
