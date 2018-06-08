package application.ui.preview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.studio.api.ExportAction;
import org.daisy.dotify.studio.api.OpenableEditor;
import org.daisy.dotify.studio.api.SearchCapabilities;
import org.daisy.dotify.studio.api.SearchOptions;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public abstract class AbstractHtmlController extends BorderPane implements OpenableEditor {
	private static final Logger logger = Logger.getLogger(AbstractHtmlController.class.getCanonicalName());
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
	private final ReadOnlyBooleanProperty canSaveProperty;
	private final ObservableBooleanValue canSaveAsProperty;
	protected final StringProperty urlProperty;
	private boolean closing;
	private File source;

	AbstractHtmlController() {
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
		});
		urlProperty = new SimpleStringProperty();
		canSaveProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		canSaveAsProperty = new SimpleBooleanProperty(true);
		closing = false;
	}
	
	@Override
	public Consumer<File> open(File file) {
		this.source = file;
		Thread wth = null;
		if (file!=null) {
			ReloadDocumentWatcher watcher = new ReloadDocumentWatcher(file);
			wth = new Thread(watcher);
			wth.setDaemon(true);
			wth.start();
			return f2 -> {
				watcher.trigger();
			};
		} else {
			return f2 -> {};
		}
	}
	
	class ReloadDocumentWatcher extends DocumentWatcher {
		ReloadDocumentWatcher(File f) {
			super(f);
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
	 * Reloads the view.
	 */
	public void reload() {
		browser.getEngine().reload();
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
			Files.copy(source.toPath(), new FileOutputStream(f));
			return true;
		} catch (IOException e) {
			throw e;
		}
	}
	
	public ReadOnlyStringProperty urlProperty() {
		return urlProperty;
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
	public void export(Window ownerWindow, ExportAction action) throws IOException {
		action.export(ownerWindow, source);
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
	
	@Override
	public String getSelectedText() {
		return browser.getEngine().executeScript("window.getSelection().toString()").toString();
	}
}
