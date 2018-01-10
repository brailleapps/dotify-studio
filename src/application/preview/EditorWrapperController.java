package application.preview;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.studio.api.Editor;
import org.daisy.streamline.api.media.AnnotatedFile;

import application.FeatureSwitch;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;

public class EditorWrapperController extends BorderPane implements Editor {
	private final Editor impl;
	
	private EditorWrapperController(Editor impl) {
		this.impl = impl;
	}

	public static EditorWrapperController newInstance(AnnotatedFile selected, Map<String, Object> options) {
		Editor prv = getEditor(selected, options);
		EditorWrapperController ret = new EditorWrapperController(prv);
		ret.setCenter((Node)prv);
		return ret;
	}
	
	private static Editor getEditor(AnnotatedFile selected, Map<String, Object> options) {
		// For now, we assume that the target format is PEF and that is supported or that no conversion is done
		if (FeatureSwitch.EDITOR.isOn() && SourcePreviewController.supportsFormat(selected)) {
			if (options==null && !PreviewController.supportsFormat(selected)) {
				EditorController prv = new EditorController();
				prv.load(selected.getFile(), FormatChecker.isXML(selected));
				return prv;
			} else {
				SourcePreviewController prv = new SourcePreviewController();
				prv.open(selected, options);
				return prv;
			}
		} else {
			PreviewController prv = new PreviewController();
			prv.open(selected, options);
			return prv;
		}
	}

	@Override
	public ReadOnlyBooleanProperty canSaveProperty() {
		return impl.canSaveProperty();
	}

	@Override
	public void save() {
		impl.save();
	}

	@Override
	public void saveAs(File f) throws IOException {
		impl.saveAs(f);
	}

	@Override
	public ReadOnlyBooleanProperty canExportProperty() {
		return impl.canExportProperty();
	}

	@Override
	public void export(File f) throws IOException {
		impl.export(f);
	}

	@Override
	public void closing() {
		impl.closing();
	}

	@Override
	public ReadOnlyStringProperty urlProperty() {
		return impl.urlProperty();
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		return impl.getSaveAsFilters();
	}

	@Override
	public void reload() {
		impl.reload();
	}

	@Override
	public ReadOnlyBooleanProperty canEmbossProperty() {
		return impl.canEmbossProperty();
	}

	@Override
	public void showEmbossDialog() {
		impl.showEmbossDialog();
	}

	@Override
	public void toggleView() {
		impl.toggleView();
	}

	@Override
	public ReadOnlyBooleanProperty toggleViewProperty() {
		return impl.toggleViewProperty();
	}

	@Override
	public ReadOnlyBooleanProperty modifiedProperty() {
		return impl.modifiedProperty();
	}

	@Override
	public Map<String, Object> getOptions() {
		return impl.getOptions();
	}

	@Override
	public void activate() {
		impl.activate();
	}

}
