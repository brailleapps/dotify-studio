package application.ui.preview;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.daisy.braille.utils.pef.PEFBook;
import org.daisy.dotify.studio.api.DocumentPosition;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.validity.ValidationReport;

import application.l10n.Messages;
import application.ui.preview.server.Start;
import application.ui.preview.server.StartupDetails;
import application.ui.preview.server.preview.stax.BookReaderResult;
import application.ui.preview.server.preview.stax.StaxPreviewParser;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public class PreviewPefController extends AbstractHtmlController {
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private ObjectProperty<FileDetails> fileDetails = new SimpleObjectProperty<>(FileDetailsCatalog.PEF_FORMAT);
	private ObjectProperty<Optional<ValidationReport>> validationReport = new SimpleObjectProperty<>(Optional.empty());
	private Start start;
	private EmbossView embossView;
	private String pageUrl;

	/**
	 * Creates a new preview controller.
	 */
	public PreviewPefController() {
		super();
		canEmbossProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(true));
	}

	/**
	 * Starts a new preview server.
	 * @param file the file
	 * @return returns a thread that watches for changes in the pef file
	 */
    @Override
	public Consumer<File> open(File file) {
		Task<String> startServer = new Task<String>() {

			@Override
			protected String call() throws Exception {
		        try {
		        	start = new Start();
		        	pageUrl = start.start(new StartupDetails.Builder(file).log(false).display(false).build());
		        	return pageUrl;
				} catch (Exception e1) {
					Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, "Failed to load server.", e1);
				}  
		        return null;
			}
		};
		startServer.setOnSucceeded(ev -> {
				String url = startServer.getValue();
				this.urlProperty.set(url);
				if (url!=null) {
					browser.getEngine().load(url);
					updateValidation();
				} else {
					browser.getEngine().load(getClass().getResource("resource-files/fail.html").toString());
				}
			}
		);
		Thread th = new Thread(startServer);
		th.setDaemon(true);
		th.start();
		return super.open(file);
	}
	
	/**
	 * Reloads the web view. This in turn, will trigger a file update, if the file
	 * has changed.
	 */
	public void reload() {
		super.reload();
		updateValidation();
	}
	
	private void updateValidation() {
		if (start!=null) {
			Optional<BookReaderResult> res = start.getMainPage().getBookReaderResult();
			if (res.isPresent()) {
				validationReport.set(Optional.of(res.get().getValidationReport()));
			} else {
				validationReport.set(Optional.empty());
			}
		} else {
			validationReport.set(Optional.empty());
		}		
	}

	/**
	 * Informs the controller that it should close.
	 */
	public void closing() {
		super.closing();
		if (start!=null) {
			start.stopServer();
		}
	}

	/**
	 * Shows the emboss dialog.
	 */
	public void showEmbossDialog() {
		if (start!=null) {
			Optional<BookReaderResult> reader = start.getMainPage().getBookReaderResult();
			if (reader.isPresent() && reader.get().isValid()) {
				PEFBook book = reader.get().getBook();
				if (embossView==null) {
					embossView = new EmbossView(book);
					embossView.initOwner(this.getScene().getWindow());
					embossView.initModality(Modality.APPLICATION_MODAL); 
				} else {
					embossView.setBook(book);
				}
				embossView.showAndWait();
			} else {
				Alert alert = new Alert(AlertType.ERROR, Messages.ERROR_CANNOT_EMBOSS_INVALID_FILE.localize(), ButtonType.OK);
	    		alert.showAndWait();
			}
		}
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		return Arrays.asList(new ExtensionFilter(Messages.EXTENSION_FILTER_FILE.localize("PEF"), "*.pef"));
	}

	@Override
	public ObservableBooleanValue canEmboss() {
		return canEmbossProperty;
	}

	@Override
	public ObservableObjectValue<FileDetails> fileDetails() {
		return fileDetails;
	}

	@Override
	public ObservableObjectValue<Optional<ValidationReport>> validationReport() {
		return validationReport;
	}
	
	@Override
	public boolean scrollTo(DocumentPosition location) {
		int volume = start.getMainPage().getVolumeForPosition(location);
		String url = pageUrl+"?book.xml&volume="+volume+"#"+StaxPreviewParser.messageId(location);
		browser.getEngine().load(url);
		// returns true if there is a validation message at the given location, false otherwise
		return validationReport.get()
				.map(v->v.getMessages().stream()).orElse(Stream.empty())
				.map(v->DocumentPosition.with(v.getLineNumber(), v.getColumnNumber()))
				.filter(v->v.equals(location))
				.count()>0;
	}

}
