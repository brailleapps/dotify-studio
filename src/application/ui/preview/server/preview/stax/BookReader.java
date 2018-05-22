package application.ui.preview.server.preview.stax;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.utils.pef.PEFBook;
import org.daisy.streamline.api.validity.ValidationReport;
import org.daisy.streamline.api.validity.ValidatorFactoryMaker;
import org.daisy.streamline.api.validity.ValidatorFactoryMakerService;

import javafx.concurrent.Task;

public class BookReader {
	private static final Logger logger = Logger.getLogger(BookReader.class.getCanonicalName());
	private final File source;
	private Task<BookReaderResult> bookReader;
	private org.daisy.streamline.api.validity.Validator pv = null;
	private long lastUpdated = 0;

	public BookReader(final File f) {
		this.source = Objects.requireNonNull(f);
		ValidatorFactoryMakerService factory = ValidatorFactoryMaker.newInstance();
		pv = factory.newValidator("application/x-pef+xml");
		fileChanged();
	}
	
	public File getFile() {
		return source;
	}

	public boolean cancel() {
		return bookReader.cancel();
	}

	private synchronized boolean fileChanged() {
		if (lastUpdated<source.lastModified()) {
			if (bookReader!=null && !bookReader.isDone()) {
				cancel();
			}
			lastUpdated = System.currentTimeMillis();
			bookReader = new Task<BookReaderResult>() {
				@Override
				protected BookReaderResult call() throws Exception {
					Date d = new Date();
					try {
						ValidationReport report = null;
						if (pv != null) {
							report = pv.validate(source.toURI().toURL());
						}
						if (isCancelled()) {
							return null;
						}
						URI uri = source.toURI();
						PEFBook p = PEFBook.load(uri);
						return new BookReaderResult(p, source, uri, report);
					} finally {
						logger.info("Book Reader (file): " + (new Date().getTime() - d.getTime()));
					}
				}
			};
			new Thread(bookReader).start();
			return true;
		} else {
			return false;
		}
	}

	public synchronized BookReaderResult getResult() {
		fileChanged();
		try {
			return bookReader.get();
		} catch (InterruptedException | ExecutionException e1) {
			logger.log(Level.INFO, "An error occurred.", e1);
			return null;
		}
	}

}