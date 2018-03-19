package application.ui.preview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.utils.pef.PEFBook;
import org.daisy.dotify.studio.api.ExportAction;
import org.daisy.dotify.studio.api.FileDetailsProperty;
import org.daisy.dotify.studio.api.OpenableEditor;
import org.daisy.streamline.api.media.FileDetails;

import application.l10n.Messages;
import application.ui.preview.server.Start;
import application.ui.preview.server.StartupDetails;
import application.ui.preview.server.preview.stax.BookReaderResult;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public class PreviewPefController extends BorderPane implements OpenableEditor {
	private static final Logger logger = Logger.getLogger(PreviewPefController.class.getCanonicalName());
	@FXML WebView browser;
	private Start start;
	private boolean closing;
	private EmbossView embossView;
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private final ReadOnlyBooleanProperty canSaveProperty;
	private StringProperty urlProperty;
	private FileDetailsProperty fileDetailsProperty = new FileDetailsProperty(FileDetailsCatalog.PEF_FORMAT);

	/**
	 * Creates a new preview controller.
	 */
	public PreviewPefController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Preview.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}

        WebEngine webEngine = browser.getEngine();
        browser.setOnDragOver(event->event.consume());
        webEngine.setCreatePopupHandler(p-> {
                Stage stage = new Stage(StageStyle.UTILITY);
                WebView wv2 = new WebView();
                stage.setScene(new Scene(wv2));
                stage.show();
                return wv2.getEngine();
            }
        );
		closing = false;
		canEmbossProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(true));
		canSaveProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		urlProperty = new SimpleStringProperty();
	}

	public static boolean supportsFormat(FileDetails format) {
		return FormatChecker.isPEF(format);
	}

    class PefDocumentWatcher extends DocumentWatcher {
    	PefDocumentWatcher(File pef) {
    		super(pef);
    	}

		@Override
		boolean shouldMonitor() {
			return super.shouldMonitor() && !closing;
		}

		@Override
		void performAction() {
			Platform.runLater(()->reload());
		}

    }

	/**
	 * Starts a new preview server.
	 * @param file the file
	 * @return returns a thread that watches for changes in the pef file
	 */
    @Override
	public Consumer<File> open(File file) {
		Thread pwt = null;
		if (file!=null) {
			PefDocumentWatcher pefWatcher = new PefDocumentWatcher(file);
    		pwt = new Thread(pefWatcher);
    		pwt.setDaemon(true);
    		pwt.start();
		}
		final Thread pefWatcherThread = pwt;
		Task<String> startServer = new Task<String>() {

			@Override
			protected String call() throws Exception {
		        try {
		        	start = new Start();
		        	return start.start(new StartupDetails.Builder(file).log(false).display(false).build());
				} catch (Exception e1) {
					Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, "Failed to load server.", e1);;
				}  
		        return null;
			}
		};
		startServer.setOnSucceeded(ev -> {
				String url = startServer.getValue();
				this.urlProperty.set(url);
				if (url!=null) {
					browser.getEngine().load(url);
				} else {
					browser.getEngine().load(getClass().getResource("resource-files/fail.html").toString());
				}
			}
		);
		Thread th = new Thread(startServer);
		th.setDaemon(true);
		th.start();
		if (pefWatcherThread!=null) {
			return f2 -> {
				pefWatcherThread.interrupt();
			};
		} else {
			return f2 -> {};
		}
	}
	
	/**
	 * Reloads the web view.
	 */
	public void reload() {
		browser.getEngine().reload();
	}

	public ReadOnlyStringProperty urlProperty() {
		return urlProperty;
	}
	
	/**
	 * Informs the controller that it should close.
	 */
	public void closing() {
		closing = true;
		if (start!=null) {
			start.stopServer();
		}
	}
	
	private Optional<URI> getBookURI() {
		if (start!=null) {
			return start.getMainPage().getBookURI();
		} else {
			return Optional.<URI>empty();
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
	public void save() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean saveAs(File f) throws IOException {
		URI uri = getBookURI().orElseThrow(()->new IOException("Nothing to save."));		
		try {
			Files.copy(Paths.get(uri), new FileOutputStream(f));
			return true;
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		return Arrays.asList(new ExtensionFilter(Messages.EXTENSION_FILTER_FILE.localize("PEF"), "*.pef"));
	}

	@Override
	public ReadOnlyBooleanProperty canEmbossProperty() {
		return canEmbossProperty;
	}

	@Override
	public ReadOnlyBooleanProperty canSaveProperty() {
		return canSaveProperty;
	}

	@Override
	public void activate() {
		browser.requestFocus();
	}
	
	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public void export(Window ownerWindow, ExportAction action) throws IOException {
		URI uri = getBookURI().orElseThrow(()->new IOException("Nothing to export."));
		File input = new File(uri);
		action.export(ownerWindow, input);
	}

	@Override
	public FileDetailsProperty fileDetailsProperty() {
		return fileDetailsProperty;
	}

}
