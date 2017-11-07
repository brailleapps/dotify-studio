package application.preview;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.daisy.dotify.api.tasks.AnnotatedFile;

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
	static final Pattern XML_PATTERN = Pattern.compile("\\Qapplication/\\E([\\w-]+\\+)?\\Qxml\\E");
	static final Pattern TEXT_PATTERN = Pattern.compile("\\Qtext/\\E.+");
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
	
	public static boolean supportsFormat(AnnotatedFile af) {
		// TODO: also support application/epub+zip
		return isText(af) || isHTML(af) || isXML(af);
	}
	
	public static boolean isXML(AnnotatedFile af) {
		return af.getMediaType()!=null && XML_PATTERN.matcher(af.getMediaType()).matches();
	}
	
	public static boolean isHTML(AnnotatedFile af) { 
		return af.getMediaType()!=null && "text/html".equals(af.getMediaType());
	}
	
	public static boolean isText(AnnotatedFile af) {
		return af.getMediaType()!=null && TEXT_PATTERN.matcher(af.getMediaType()).matches();
	}
	
	/**
	 * Converts and opens a file.
	 * @param selected the file
	 * @param options the options
	 */
	public void convertAndOpen(AnnotatedFile selected, Map<String, Object> options) {
        PreviewController prv = new PreviewController();
        prv.convertAndOpen(selected, options);
		preview.setContent(prv);
		source.setText(Messages.LABEL_SOURCE.localize(selected.getFile().getName()));
		EditorController editor = new EditorController();
		editor.load(selected.getFile(), isXML(selected));
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
