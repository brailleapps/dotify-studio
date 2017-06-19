package com.googlecode.e2u;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.daisy.braille.consumer.validator.ValidatorFactory;
import org.daisy.braille.pef.PEFBook;

public class BookReader {
	private static final Logger logger = Logger.getLogger(BookReader.class.getCanonicalName());
	private final File source;
    private SwingWorker<BookReaderResult, Void> bookReader;
    private BookReaderResult book = null;
    private org.daisy.braille.api.validator.Validator pv = null;
	private long lastUpdated;
    
    public static class BookReaderResult {
    	private final PEFBook book;
    	private final File bookFile;
    	private final URI uri;
    	private final boolean validateOK;
    	private final String messages;
    	
    	private BookReaderResult(PEFBook book, File bookFile, URI uri, boolean validateOK, String messages) {
    		this.book = book;
    		this.bookFile = bookFile;
    		this.uri = uri;
    		this.validateOK = validateOK;
    		this.messages = messages;
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
    	
    	public String getValidationMessages() {
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
		    	URI uri = this.getClass().getResource(resource).toURI();
		    	PEFBook p = PEFBook.load(uri);
				return new BookReaderResult(p, null, uri, true, null);
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
		ValidatorFactory factory = ValidatorFactory.newInstance();
		pv = factory.newValidator("org.daisy.braille.pef.PEFValidator");
    	readBook(f);
    }

    private void readBook(File f) {
		lastUpdated = System.currentTimeMillis();
        bookReader = new SwingWorker<BookReaderResult, Void>() {
        	Date d;
			@Override
			protected BookReaderResult doInBackground() throws Exception {
				d = new Date();
				boolean validateOK = false;
				String mess = null;
				if (pv != null) {
					synchronized(pv) {
						try {
							validateOK = pv.validate(f.toURI().toURL());
						} catch (MalformedURLException e) {
							validateOK = false;
						}
						int c;
						try (InputStreamReader isr = new InputStreamReader(pv.getReportStream())) {
							StringBuilder sb = new StringBuilder();
							while ((c = isr.read())>-1) {
								sb.append(((char)c));
							}
							mess = sb.toString();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					validateOK = false;
				}
				URI uri = f.toURI();
				PEFBook p = PEFBook.load(uri);
		    	return new BookReaderResult(p, f, uri, validateOK, mess);
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