package application.ui.preview.server;

import java.io.File;
import java.net.URI;
import java.util.Objects;

import org.daisy.braille.utils.pef.PEFBook;

import application.ui.preview.server.preview.stax.BookReader;
import application.ui.preview.server.preview.stax.BookReaderResult;
import application.ui.preview.server.preview.stax.StaxPreviewController;
import shared.Settings;

public class BookViewController {
	private static final Settings settings = Settings.getSettings();
	private BookReader bookReader;
	private AboutBookView aboutBookView;
	private StaxPreviewController controller;

	public BookViewController(File f) {
		bookReader = new BookReader(Objects.requireNonNull(f));
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
