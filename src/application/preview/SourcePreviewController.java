package application.preview;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.studio.api.Editor;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.FileDetails;

import application.l10n.Messages;
import javafx.beans.binding.When;
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
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public class SourcePreviewController extends BorderPane implements Editor {
	private static final Logger logger = Logger.getLogger(SourcePreviewController.class.getCanonicalName());
	private static final int PREVIEW_INDEX = 0;
	private static final int SOURCE_INDEX = 1;

	@FXML TabPane tabs;
	@FXML Tab preview;
	@FXML Tab source;
	private final BooleanProperty canEmbossProperty;
	private final BooleanProperty canExportProperty;
	private final BooleanProperty canSaveProperty;
	private final BooleanProperty modifiedProperty;
	private final StringProperty urlProperty;
	private Node sourceContent;
	private Node previewContent;

	/**
	 * Creates a new preview controller.
	 */
	public SourcePreviewController() {
		canEmbossProperty = new SimpleBooleanProperty();
		canExportProperty = new SimpleBooleanProperty();
		canSaveProperty = new SimpleBooleanProperty();
		modifiedProperty = new SimpleBooleanProperty();
		urlProperty = new SimpleStringProperty();
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SourcePreview.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
	}

	@FXML void initialize() {
	}

	/**
	 * Returns true if the editor format is supported (the preview format is assumed to be PEF)
	 * @param editorFormat the editor format
	 * @return returns true if the editor format is supported
	 */
	public static boolean supportsFormat(FileDetails editorFormat) {
		return supportsFormat(editorFormat, FileDetailsCatalog.PEF_FORMAT);
	}
	public static boolean supportsFormat(FileDetails editorFormat, FileDetails previewFormat) {
		return EditorController.supportsFormat(editorFormat) && PreviewController.supportsFormat(previewFormat);
	}

	/**
	 * Converts and opens a file.
	 * @param selected the file
	 * @param prv the preview editor
	 */
	public void open(AnnotatedFile selected, Editor prv) {
		setupOpen(prv, selected);
	}

	private void setupOpen(Editor prv, AnnotatedFile selected) {
		previewContent = (Node)prv;
		preview.setContent(previewContent);
		source.setText(Messages.LABEL_SOURCE.localize(selected.getFile().getName()));
		EditorController editor = new EditorController();
		editor.load(selected.getFile(), FormatChecker.isXML(selected));
		source.setContent(editor);
		sourceContent = editor;
		canEmbossProperty.bind(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX).and(prv.canEmbossProperty())
			.or(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(SOURCE_INDEX).and(editor.canEmbossProperty())
			)
		);
		canExportProperty.bind(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX).and(prv.canExportProperty())
			.or(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(SOURCE_INDEX).and(editor.canExportProperty())
			)
		);
		canSaveProperty.bind(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX).and(prv.canSaveProperty())
			.or(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(SOURCE_INDEX).and(editor.canSaveProperty())
			)
		);
		modifiedProperty.bind(editor.modifiedProperty());
		urlProperty.bind(
				new When(tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX))
					.then(prv.urlProperty())
					.otherwise(
						new When(tabs.getSelectionModel().selectedIndexProperty().isEqualTo(SOURCE_INDEX))
							.then(editor.urlProperty())
							.otherwise(new SimpleStringProperty())
					)
				);
	}

	@Override
	public void reload() {
		getCurrentEditor().ifPresent(v->v.reload());
	}

	@Override
	public Optional<String> getURL() {
		return getCurrentEditor().map(v->v.getURL()).orElse(Optional.empty());
	}

	@Override
	public void showEmbossDialog() {
		((Editor)previewContent).showEmbossDialog();
	}

	@Override
	public void closing() {
		((Editor)sourceContent).closing();
		((Editor)previewContent).closing();
	}

	private Optional<Editor> getCurrentEditor() {
		return Optional.ofNullable(tabs.getSelectionModel())
				.flatMap(v->
				{
					switch (v.getSelectedIndex()) {
						case PREVIEW_INDEX:
							return Optional.of((Editor)previewContent);
						case SOURCE_INDEX:
							return Optional.of((Editor)sourceContent);
						default:
							return Optional.empty();
					}			
				});
	}

	@Override
	public void save() {
		Optional<Editor> view = getCurrentEditor();
		view.filter(v->v.canSave()).ifPresent(v->{
			v.save();
		});
	}

	@Override
	public boolean saveAs(File f) throws IOException {
		Optional<Editor> view = getCurrentEditor();
		if (view.isPresent()) {
			return view.get().saveAs(f);
		} else {
			return false;
		}
	}

	@Override
	public void export(File f) throws IOException {
		((Editor)previewContent).export(f);
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
		return getCurrentEditor().map(v->v.getSaveAsFilters()).orElse(Collections.emptyList());
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
	public ReadOnlyBooleanProperty modifiedProperty() {
		return modifiedProperty;
	}

	@Override
	public ReadOnlyBooleanProperty toggleViewProperty() {
		return new SimpleBooleanProperty(true);
	}

	@Override
	public void toggleView() {		
		SingleSelectionModel<Tab> select = tabs.getSelectionModel();
		if (select.isSelected(0)) {
			select.selectLast();
		} else {
			select.selectFirst();
		}
		if (getCenter() instanceof SplitPane) {
			setSplitPane();
		}
	}
	
	@Override
	public void toggleViewingMode() {
		if (getCenter() instanceof TabPane) {
			source.setContent(null);
			preview.setContent(null);
			setSplitPane();
		} else if (getCenter() instanceof SplitPane) {
			SplitPane sp = (SplitPane)getCenter();			
			sp.getItems().clear();
			source.setContent(sourceContent);
			preview.setContent(previewContent);
			setCenter(tabs);
		}
	}

	private void setSplitPane() {
		// Put the preview first by default and if it matches the selected view
		boolean previewFirst = getCurrentEditor().map(v->v==previewContent).orElse(true);
		SplitPane sp;
		if (previewFirst) {
			sp = new SplitPane(previewContent, sourceContent);
		} else {
			sp = new SplitPane(sourceContent, previewContent);
		}
		sp.setDividerPosition(0, 0.6);
		setCenter(sp);

	}

	@Override
	public void activate() {
		getCurrentEditor().ifPresent(v->v.activate());
	}
	
	@Override
	public Node getNode() {
		return this;
	}
}