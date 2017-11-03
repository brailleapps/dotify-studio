package application.preview;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public class SourcePreviewController extends BorderPane implements Preview {
	private static final Logger logger = Logger.getLogger(SourcePreviewController.class.getCanonicalName());
	@FXML TabPane tabs;
	@FXML Tab preview;
	@FXML Tab source;

	/**
	 * Creates a new preview controller.
	 */
	public SourcePreviewController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SourcePreview.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
	}
	
	/**
	 * Converts and opens a file.
	 * @param selected the file
	 * @param options the options
	 */
	public void convertAndOpen(File selected, Map<String, Object> options) {
        PreviewController prv = new PreviewController();
        prv.convertAndOpen(selected, options);
		preview.setContent(prv);
		source.setText(Messages.LABEL_SOURCE.localize(selected.getName()));
		EditorController editor = new EditorController();
		editor.load(selected);
		source.setContent(editor);
	}

	@Override
	public void reload() {
		((Preview)preview.getContent()).reload();
	}

	@Override
	public String getURL() {
		return ((Preview)preview.getContent()).getURL();
	}

	@Override
	public void showEmbossDialog() {
		((Preview)preview.getContent()).showEmbossDialog();
	}

	@Override
	public Optional<URI> getBookURI() {
		return ((Preview)preview.getContent()).getBookURI();
	}

	@Override
	public void closing() {
		((Preview)preview.getContent()).closing();
	}

	@Override
	public boolean canSave() {
		SingleSelectionModel<Tab> m = tabs.getSelectionModel(); 
		if (m!=null) {
			return m.getSelectedItem() == source;
		}
		return false;
	}

	@Override
	public void save() {
		SingleSelectionModel<Tab> m = tabs.getSelectionModel(); 
		if (m!=null && m.getSelectedItem() == source) {
			((EditorController)source.getContent()).save();
		}
	}

}
