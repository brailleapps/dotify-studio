package application.ui.preview;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.studio.api.DocumentPosition;
import org.daisy.dotify.studio.api.Editor;
import org.daisy.dotify.studio.api.ExportAction;
import org.daisy.dotify.studio.api.SearchCapabilities;
import org.daisy.dotify.studio.api.SearchOptions;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.validity.ValidationReport;

import application.common.BindingStore;
import application.l10n.Messages;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
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
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

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
	private final BooleanProperty canSaveProperty;
	private final BooleanProperty canSaveAsProperty;
	private final BooleanProperty modifiedProperty;
	private final StringProperty urlProperty;
	private Node sourceContent;
	private Node previewContent;
	private ObjectProperty<FileDetails> fileDetails;
	private ObjectProperty<Optional<ValidationReport>> validationReport;
	private ObjectProperty<SearchCapabilities> searchCapabilities;
	private final BindingStore bindings;

	/**
	 * Creates a new preview controller.
	 */
	public SourcePreviewController() {
		canEmbossProperty = new SimpleBooleanProperty();
		canSaveProperty = new SimpleBooleanProperty();
		canSaveAsProperty = new SimpleBooleanProperty();
		modifiedProperty = new SimpleBooleanProperty();
		urlProperty = new SimpleStringProperty();
		fileDetails = new SimpleObjectProperty<>();
		validationReport = new SimpleObjectProperty<>(Optional.empty());
		searchCapabilities = new SimpleObjectProperty<>(SearchCapabilities.NONE);
		bindings = new BindingStore();
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
	 * Converts and opens a file.
	 * @param selected the file
	 * @param prv the preview editor
	 */
	public void open(AnnotatedFile selected, Editor prv) {
		setupOpen(prv, selected);
	}

	private void setupOpen(Editor prv, AnnotatedFile selected) {
		bindings.clear();
		canEmbossProperty.unbind();
		canSaveProperty.unbind();
		canSaveAsProperty.unbind();
		modifiedProperty.unbind();
		urlProperty.unbind();
		fileDetails.unbind();
		validationReport.unbind();
		searchCapabilities.unbind();
		previewContent = (Node)prv;
		preview.setContent(previewContent);
		source.setText(Messages.LABEL_SOURCE.localize(selected.getPath().toFile().getName()));
		EditorController editor = new EditorController();
		editor.load(selected.getPath().toFile(), FormatChecker.isXML(selected));
		source.setContent(editor);
		sourceContent = editor;
		canEmbossProperty.bind(bindings.add(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX).and(prv.canEmboss())
			.or(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(SOURCE_INDEX).and(editor.canEmboss())
			)
		));
		fileDetails.bind(bindings.add(
			new When(tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX))
			.then(prv.fileDetails())
			.otherwise(editor.fileDetails())
		));
		validationReport.bind(bindings.add(
			new When(tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX))
			.then(prv.validationReport())
			.otherwise(editor.validationReport())
		));
		searchCapabilities.bind(bindings.add(
			new When(tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX))
			.then(prv.searchCapabilities())
			.otherwise(editor.searchCapabilities())
		));
		canSaveProperty.bind(bindings.add(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX).and(prv.canSave())
			.or(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(SOURCE_INDEX).and(editor.canSave())
			)
		));
		canSaveAsProperty.bind(bindings.add(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX).and(prv.canSaveAs())
			.or(
				tabs.getSelectionModel().selectedIndexProperty().isEqualTo(SOURCE_INDEX).and(editor.canSaveAs())
			)
		));
		modifiedProperty.bind(editor.modifiedProperty());
		urlProperty.bind(bindings.add(
				new When(tabs.getSelectionModel().selectedIndexProperty().isEqualTo(PREVIEW_INDEX))
					.then(prv.urlProperty())
					.otherwise(
						new When(tabs.getSelectionModel().selectedIndexProperty().isEqualTo(SOURCE_INDEX))
							.then(editor.urlProperty())
							.otherwise(new SimpleStringProperty())
					)
				));
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
		view.filter(v->v.canSave().get()).ifPresent(v->{
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
	public ObservableBooleanValue canEmboss() {
		return canEmbossProperty;
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
	public ObservableBooleanValue canSave() {
		return canSaveProperty;
	}
	
	@Override
	public ObservableBooleanValue canSaveAs() {
		return canSaveAsProperty;
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

	@Override
	public void export(Window ownerWindow, ExportAction action) throws IOException {
		Optional<Editor> v = getCurrentEditor();
		//Not using ifPresent, because export might throw IOException
		if (v.isPresent()) {
			v.get().export(ownerWindow, action);
		}
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
	public boolean scrollTo(DocumentPosition msg) {
		return getCurrentEditor().map(v->v.scrollTo(msg)).orElse(false);
	}

	@Override
	public boolean findNext(String text, SearchOptions opts) {
		return getCurrentEditor().map(v->v.findNext(text, opts)).orElse(false);
	}

	@Override
	public void replace(String replace) {
		getCurrentEditor().ifPresent(v->v.replace(replace));
	}

	@Override
	public ObservableObjectValue<SearchCapabilities> searchCapabilities() {
		return searchCapabilities;
	}

	@Override
	public String getSelectedText() {
		return getCurrentEditor().map(v->v.getSelectedText()).orElse("");
	}
	
}