package application.search;

import org.daisy.braille.pef.PEFBook;

import application.l10n.Messages;

public class PefBookAdapter {
	private final PEFBook book;
	private final String display;
	
	public PefBookAdapter(PEFBook book) {
		this.book = book;
    	String untitled = Messages.MESSAGE_UNKNOWN_TITLE.localize();
    	String unknown = Messages.MESSAGE_UNKNOWN_AUTHOR.localize();
		Iterable<String> title = book.getTitle(); 
		Iterable<String> authors = book.getAuthors();
		this.display = Messages.MESSAGE_SEARCH_RESULT.localize((title==null?untitled:title.iterator().next()), (authors==null?unknown:authors.iterator().next()));
	}
	
	public PEFBook getBook() {
		return book;
	}
	
	@Override
	public String toString() {
		return display;
	}
}
