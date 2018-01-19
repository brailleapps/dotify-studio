package application.preview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.studio.api.OpenableEditor;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public class PreviewHtmlController extends BorderPane implements OpenableEditor {
	private static final Logger logger = Logger.getLogger(PreviewHtmlController.class.getCanonicalName());
	@FXML WebView browser;
	private boolean closing;
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private final ReadOnlyBooleanProperty canExportProperty;
	private final ReadOnlyBooleanProperty canSaveProperty;
	private final StringProperty urlProperty;
	private File file;

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
		canExportProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		canSaveProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
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
	public void export(File f) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		return Arrays.asList(new ExtensionFilter("HTML-files", "*.html"));
	}

	@Override
	public ReadOnlyBooleanProperty canEmbossProperty() {
		return canEmbossProperty;
	}

	@Override
	public ReadOnlyBooleanProperty canExportProperty() {
		return canExportProperty;
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

}
