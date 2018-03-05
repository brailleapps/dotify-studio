package application.ui.preview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.studio.api.DocumentPosition;
import org.daisy.dotify.studio.api.ExportAction;
import org.daisy.dotify.studio.api.OpenableEditor;
import org.daisy.dotify.studio.api.SearchCapabilities;
import org.daisy.dotify.studio.api.SearchOptions;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.validity.ValidationReport;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public class PreviewHtmlController extends BorderPane implements OpenableEditor {
	private static final Logger logger = Logger.getLogger(PreviewHtmlController.class.getCanonicalName());
	private static final ReadOnlyObjectProperty<SearchCapabilities> SEARCH_CAPABILITIES = new SimpleObjectProperty<>(
			new SearchCapabilities.Builder()
			.direction(true)
			.matchCase(true)
			.wrap(true)
			.find(true)
			.replace(false)
			.build()
	);
	@FXML WebView browser;
	private boolean closing;
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private final ReadOnlyBooleanProperty canSaveProperty;
	private final ObservableBooleanValue canSaveAsProperty;
	private final StringProperty urlProperty;
	private File file;
	private ObjectProperty<FileDetails> fileDetails = new SimpleObjectProperty<>(FileDetailsCatalog.HTML_FORMAT);
	

	/**
	 * Creates a new preview controller.
	 */
	public PreviewHtmlController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PreviewHtml.fxml"), Messages.getBundle());
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
		canEmbossProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		canSaveProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		canSaveAsProperty = new SimpleBooleanProperty(true);
		urlProperty = new SimpleStringProperty();
	}
	
    class HtmlDocumentWatcher extends DocumentWatcher {
    	HtmlDocumentWatcher(File html) {
    		super(html);
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

    @Override
	public Consumer<File> open(File file) {
		Thread htmlwt = null;
		if (file!=null) {
			HtmlDocumentWatcher htmlWatcher = new HtmlDocumentWatcher(file);
    		htmlwt = new Thread(htmlWatcher);
    		htmlwt.setDaemon(true);
    		htmlwt.start();
    		this.file = file;
		}
		Thread htmlWatcherThread = htmlwt;
		if (htmlWatcherThread!=null) {
			String url;
			try {
				url = file.toURI().toURL().toString();
				this.urlProperty.set(url);
				if (url!=null) {
					browser.getEngine().load(url);
				} else {
					browser.getEngine().load(getClass().getResource("resource-files/fail.html").toString());
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return f2 -> {
				htmlWatcherThread.interrupt();
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
	}

	@Override
	public void showEmbossDialog() {
	}

	@Override
	public void save() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean saveAs(File f) throws IOException {
		try {
			Files.copy(this.file.toPath(), new FileOutputStream(f));
			return true;
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		return Arrays.asList(new ExtensionFilter(Messages.EXTENSION_FILTER_FILE.localize("HTML"), "*.html"));
	}

	@Override
	public ObservableBooleanValue canEmboss() {
		return canEmbossProperty;
	}

	@Override
	public ObservableBooleanValue canSave() {
		return canSaveProperty;
	}
	
	@Override
	public ObservableBooleanValue canSaveAs() {
		return canSaveAsProperty;
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
	public void export(Window ownerWindow, ExportAction action) {
		action.export(ownerWindow, file);
	}

	@Override
	public ObservableObjectValue<FileDetails> fileDetails() {
		return fileDetails;
	}
	
	@Override
	public ObservableObjectValue<Optional<ValidationReport>> validationReport() {
		return new SimpleObjectProperty<>(Optional.empty());
	}
	
	@Override
	public boolean scrollTo(DocumentPosition msg) {
		return false;
	}

	@Override
	public boolean findNext(String text, SearchOptions opts) {
		return (Boolean)browser.getEngine().executeScript(
			String.format("self.find('%s', %b, %b, %b)", text, 
			opts.shouldMatchCase(), opts.shouldReverseSearch(), opts.shouldWrapAround())
		);
	}

	@Override
	public void replace(String replace) {
		// Not supported
	}

	@Override
	public ObservableObjectValue<SearchCapabilities> searchCapabilities() {
		return SEARCH_CAPABILITIES;
	}

}
