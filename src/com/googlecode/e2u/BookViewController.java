package com.googlecode.e2u;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.daisy.braille.utils.pef.PEFBook;

import com.googlecode.e2u.preview.stax.BookReader;
import com.googlecode.e2u.preview.stax.BookReaderResult;
import com.googlecode.e2u.preview.stax.StaxPreviewController;

import shared.Settings;

public class BookViewController {
	private static final Settings settings = Settings.getSettings();
	private BookReader bookReader;
	private AboutBookView aboutBookView;
	private StaxPreviewController controller;

	public BookViewController(File f) {
		if (f==null) {
	    	try {
	    		bookReader = new BookReader("resource-files/book.pef");
	    	} catch (URISyntaxException e) {
	    		e.printStackTrace();
	    	}
		} else {
			bookReader = new BookReader(f);
		}
    	aboutBookView = null;
    	controller = null;
	}
	
	public URI getBookURI() {
    	return bookReader.getResult().getURI();
    }
    
    public PEFBook getBook() {
    	return bookReader.getResult().getBook();
    }
    
    public BookReaderResult getBookReaderResult() {
    	return bookReader.getResult();
    }
    
    public boolean bookIsValid() {
    	return bookReader.getResult().isValid();
    }

    public AboutBookView getAboutBookView() {
    	return new AboutBookView(bookReader.getResult().getBook(), bookReader.getResult().getValidationMessages());
    }
    
    public StaxPreviewController getPreviewView() {
    	if (controller==null) {
    		controller = new StaxPreviewController(bookReader, settings);
    	}
    	return controller;
    }
    
    public void close() {
    	bookReader.cancel();
    }

}
