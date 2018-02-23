package application.preview.server.preview.stax;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.daisy.braille.utils.pef.PEFBook;
import org.daisy.streamline.api.validity.ValidationReport;
import org.daisy.streamline.api.validity.ValidatorMessage;

public class BookReaderResult {
	private final PEFBook book;
	private final File bookFile;
	private final URI uri;
	private final ValidationReport report;
	private final boolean validateOK;
	private final List<ValidatorMessage> messages;

	BookReaderResult(PEFBook book, File bookFile, URI uri, ValidationReport report) {
		this.book = book;
		this.bookFile = bookFile;
		this.uri = uri;
		this.report = report;
		if (report != null) {
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

	public ValidationReport getValidationReport() {
		return report;
	}

	public List<ValidatorMessage> getValidationMessages() {
		return messages;
	}
}