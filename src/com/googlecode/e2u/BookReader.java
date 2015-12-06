package com.googlecode.e2u;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.daisy.braille.consumer.validator.ValidatorFactory;
import org.daisy.braille.pef.PEFBook;

public class BookReader {
	
    private SwingWorker<BookReaderResult, Void> bookReader;
    private BookReaderResult book = null;
    
    public static class BookReaderResult {
    	private final PEFBook book;
    	private final File bookFile;
    	private final URI uri;
    	private final boolean validateOK;
    	private final org.daisy.braille.api.validator.Validator pv;
    	
    	private BookReaderResult(PEFBook book, File bookFile, URI uri, boolean validateOK, org.daisy.braille.api.validator.Validator pv) {
    		this.book = book;
    		this.bookFile = bookFile;
    		this.uri = uri;
    		this.validateOK = validateOK;
    		this.pv = pv;
    	}
    	
    	public PEFBook getBook() {
    		return book;
    	}
    	
    	public File getBookFile() {
    		return bookFile;
    	}
    	
    	public URI getURI() {
    		return uri;
    	}

    	public boolean isValid() {
    		return validateOK;
    	}
    	
    	public org.daisy.braille.api.validator.Validator getValidator() {
    		return pv;
    	}
    }
    
    public BookReader(final String resource) throws URISyntaxException {
        bookReader = new SwingWorker<BookReaderResult, Void>() {
        	Date d;
			@Override
			protected BookReaderResult doInBackground() throws Exception {
				d = new Date();
		    	URI uri = this.getClass().getResource(resource).toURI();
		    	PEFBook p = PEFBook.load(uri);
				return new BookReaderResult(p, null, uri, true, null);
			}
			
                @Override
	       protected void done() {
	    	   System.out.println("Book Reader (resource): " + (new Date().getTime() - d.getTime()));
	       }
       	
        };
        new NewThreadExecutor().execute(bookReader);
    }
    
    public BookReader(final File f) {
        bookReader = new SwingWorker<BookReaderResult, Void>() {
        	Date d;
			@Override
			protected BookReaderResult doInBackground() throws Exception {
				d = new Date();
				ValidatorFactory factory = ValidatorFactory.newInstance();
				org.daisy.braille.api.validator.Validator pv = factory.newValidator("org.daisy.braille.pef.PEFValidator");
				boolean validateOK = false;
				if (pv != null) {
					try {
						validateOK = pv.validate(f.toURI().toURL());
					} catch (MalformedURLException e) {
						validateOK = false;
					}
				} else {
					validateOK = false;
				}
				URI uri = f.toURI();
				PEFBook p = PEFBook.load(uri);
		    	return new BookReaderResult(p, f, uri, validateOK, pv);
			}
			
                @Override
	       protected void done() {
	    	   System.out.println("Book Reader (file): " + (new Date().getTime() - d.getTime()));
	       }
       	
        };
        new NewThreadExecutor().execute(bookReader);
    }

    public boolean cancel() {
    	return bookReader.cancel(true);
    }
    
    public synchronized BookReaderResult getResult() {
    	if (book==null) {
    		try {
				book = bookReader.get();
			} catch (InterruptedException | ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	return book;
    }

}