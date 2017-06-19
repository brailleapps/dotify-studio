package com.googlecode.e2u;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.daisy.braille.pef.PEFBook;
import org.daisy.dotify.api.validity.ValidationReport;
import org.daisy.dotify.api.validity.ValidatorFactoryMakerService;
import org.daisy.dotify.api.validity.ValidatorMessage;
import org.daisy.dotify.consumer.validity.ValidatorFactoryMaker;

public class BookReader {
	private static final Logger logger = Logger.getLogger(BookReader.class.getCanonicalName());
	private final File source;
    private SwingWorker<BookReaderResult, Void> bookReader;
    private BookReaderResult book = null;
    private org.daisy.dotify.api.validity.Validator pv = null;
	private long lastUpdated;
    
    public static class BookReaderResult {
    	private final PEFBook book;
    	private final File bookFile;
    	private final URI uri;
    	private final boolean validateOK;
    	private final List<ValidatorMessage> messages;
    	
    	private BookReaderResult(PEFBook book, File bookFile, URI uri, ValidationReport report) {
    		this.book = book;
    		this.bookFile = bookFile;
    		this.uri = uri;
    		if (report!=null) {
    			this.validateOK = report.isValid();
    			this.messages = report.getMessages();
    		} else {
    			this.validateOK = false;
    			this.messages = Collections.emptyList();
    		}
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
    	
    	public List<ValidatorMessage> getValidationMessages() {
    		return messages;
    	}
    }
    
    public BookReader(final String resource) throws URISyntaxException {
    	this.source = null;
    	readBook(resource);
    }

    private void readBook(String resource) {
		lastUpdated = System.currentTimeMillis();
        bookReader = new SwingWorker<BookReaderResult, Void>() {
        	Date d;
			@Override
			protected BookReaderResult doInBackground() throws Exception {
				d = new Date();
				URL url = this.getClass().getResource(resource);
		    	URI uri = url.toURI();
		    	PEFBook p = PEFBook.load(uri);
				return new BookReaderResult(p, null, uri, new ValidationReport.Builder(url).build());
			}
			
                @Override
	       protected void done() {
	    	   logger.info("Book Reader (resource): " + (new Date().getTime() - d.getTime()));
	       }
       	
        };
        new NewThreadExecutor().execute(bookReader);    	
    }
    
    public BookReader(final File f) {
    	this.source = f;
        ValidatorFactoryMakerService factory = ValidatorFactoryMaker.newInstance();
        pv = factory.newValidator("application/x-pef+xml");
    	readBook(f);
    }

    private void readBook(File f) {
		lastUpdated = System.currentTimeMillis();
        bookReader = new SwingWorker<BookReaderResult, Void>() {
        	Date d;
			@Override
			protected BookReaderResult doInBackground() throws Exception {
				d = new Date();
				ValidationReport report = null;
				if (pv != null) {
					report = pv.validate(f.toURI().toURL());
				}
				URI uri = f.toURI();
				PEFBook p = PEFBook.load(uri);
		    	return new BookReaderResult(p, f, uri, report);
			}
			
           @Override
	       protected void done() {
	    	   logger.info("Book Reader (file): " + (new Date().getTime() - d.getTime()));
	       }
       	
        };
        new NewThreadExecutor().execute(bookReader);
    }

    public boolean cancel() {
    	return bookReader.cancel(true);
    }
    
    private synchronized void reload() {
    	if (source==null) {
    		logger.warning("Reload on internal resource not supported.");
    		return;
    	}
    	book = null;
    	if (!bookReader.isDone()) {
    		cancel();
    	}
    	readBook(source);
    }
    
	public boolean fileChanged() {
		if (source!=null && lastUpdated<source.lastModified()) {
			reload();
			return true;
		} else {
			return false;
		}
	}
    
    public synchronized BookReaderResult getResult() {
    	fileChanged();
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