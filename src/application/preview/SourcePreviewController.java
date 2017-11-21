package application.preview;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.daisy.dotify.api.tasks.AnnotatedFile;

import application.l10n.Messages;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;

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
	private final BooleanProperty canEmbossProperty;
	private final BooleanProperty canExportProperty;
	private final BooleanProperty canSaveProperty;
	private final StringProperty urlProperty;

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
		canEmbossProperty = new SimpleBooleanProperty(false);
		canExportProperty = new SimpleBooleanProperty(false);
		canSaveProperty = new SimpleBooleanProperty(false);
		urlProperty = new SimpleStringProperty();
	}

	@FXML void initialize() {
		tabs.getSelectionModel().selectedItemProperty().addListener((o, ot, nt)->{
			Optional<Node> pr = Optional.of(nt.getContent())
					.filter(v->v instanceof Preview);
			canEmbossProperty.set(
					pr.map(v->((Preview)v).canEmboss()).orElse(false)
					);
			canExportProperty.set(
					pr.map(v->((Preview)v).canExport()).orElse(false)
					);
			canSaveProperty.set(
					pr.map(v->((Preview)v).canSave()).orElse(false)
					);
			urlProperty.set(
					pr.map(v->((Preview)v).urlProperty().get()).orElse(null)
					);
		});
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
	public void open(AnnotatedFile selected, Map<String, Object> options) {
		if (options==null) {
			open(selected);
		} else {
			PreviewController prv = new PreviewController();
			prv.open(selected, options);
			setupOpen(prv, selected);
		}
	}

	public void open(AnnotatedFile selected) {
		PreviewController prv = new PreviewController();
		prv.open(selected, null);
		setupOpen(prv, selected);
	}

	private void setupOpen(PreviewController prv, AnnotatedFile selected) {
		preview.setContent(prv);
		source.setText(Messages.LABEL_SOURCE.localize(selected.getFile().getName()));
		EditorController editor = new EditorController();
		editor.load(selected.getFile(), isXML(selected));
		source.setContent(editor);
		canEmbossProperty.set(prv.canEmboss());
		canExportProperty.set(prv.canExport());
		canSaveProperty.set(prv.canSave());
		urlProperty.set(prv.urlProperty().get());
		editor.modifiedProperty().addListener((o, ov, nv)->{
			String modified = "* ";
			String t = source.getText();
			if (nv) {
				if (!t.startsWith(modified)) {
					source.setText(modified + source.getText());
				}
			} else {
				if (t.startsWith(modified)) {
					source.setText(t.substring(2));
				}
			}
		});
	}

	@Override
	public void reload() {
		getSelectedView().ifPresent(v->v.reload());
	}

	@Override
	public Optional<String> getURL() {
		return getSelectedView().map(v->v.getURL()).orElse(Optional.empty());
	}

	@Override
	public void showEmbossDialog() {
		((Preview)preview.getContent()).showEmbossDialog();
	}

	@Override
	public void closing() {
		((Preview)source.getContent()).closing();
		((Preview)preview.getContent()).closing();
	}

	private Optional<Preview> getSelectedView() {
		return Optional.ofNullable(tabs.getSelectionModel())
				.map(v->v.getSelectedItem().getContent())
				.filter(v->v instanceof Preview)
				.map(v->(Preview)v);
	}

	@Override
	public void save() {
		SingleSelectionModel<Tab> m = tabs.getSelectionModel(); 
		if (m!=null && m.getSelectedItem() == source) {
			((EditorController)source.getContent()).save();
		}
	}

	@Override
	public void saveAs(File f) throws IOException {
		Optional<Preview> view = getSelectedView();
		if (view.isPresent()) {
			view.get().saveAs(f);
		}
	}

	@Override
	public void export(File f) throws IOException {
		((Preview)preview.getContent()).export(f);
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
	public List<ExtensionFilter> getSaveAsFilters() {
		return getSelectedView().map(v->v.getSaveAsFilters()).orElse(Collections.emptyList());
	}

	@Override
	public ReadOnlyStringProperty urlProperty() {
		return urlProperty;
	}

	@Override
	public ReadOnlyBooleanProperty canSaveProperty() {
		return canSaveProperty;
	}

	@Override
	public void toggleView() {		
		SingleSelectionModel<Tab> select = tabs.getSelectionModel();
		if (select.isSelected(0)) {
			select.selectLast();
		} else {
			select.selectFirst();
		}
	}

	@Override
	public Map<String, Object> getOptions() {
		SingleSelectionModel<Tab> m = tabs.getSelectionModel();
		if (m!=null) {
			Tab t = m.getSelectedItem();
			if (t!=null && t.getContent() instanceof Preview) {
				Preview p = (Preview)t.getContent();
				if (t == source) {
					if (p.getURL().orElse("").endsWith(".pef")) {
						return null;
					} else {
						return ((Preview)preview.getContent()).getOptions();
					}
				} else if (t == preview) {
					return null;
				}
			}
		}
		throw new RuntimeException();
	}
}