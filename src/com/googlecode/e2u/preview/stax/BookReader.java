package com.googlecode.e2u.preview.stax;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.daisy.braille.utils.pef.PEFBook;
import org.daisy.streamline.api.validity.ValidationReport;
import org.daisy.streamline.api.validity.ValidatorFactoryMaker;
import org.daisy.streamline.api.validity.ValidatorFactoryMakerService;

public class BookReader {
	private static final Logger logger = Logger.getLogger(BookReader.class.getCanonicalName());
	private final File source;
	private SwingWorker<BookReaderResult, Void> bookReader;
	private BookReaderResult book = null;
	private org.daisy.streamline.api.validity.Validator pv = null;
	private long lastUpdated;

	public BookReader(final File f) {
		this.source = Objects.requireNonNull(f);
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
		book = null;
		if (!bookReader.isDone()) {
			cancel();
		}
		readBook(source);
	}

	public boolean fileChanged() {
		if (lastUpdated<source.lastModified()) {
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