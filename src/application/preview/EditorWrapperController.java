package application.preview;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.daisy.dotify.studio.api.Editor;
import org.daisy.streamline.api.media.AnnotatedFile;

import application.FeatureSwitch;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;
import shared.Settings;
import shared.Settings.Keys;

public class EditorWrapperController extends BorderPane implements Editor {
	private final Editor impl;
	private DotifyController dotify;
	
	private EditorWrapperController(Editor impl, DotifyController converter) {
		this.impl = impl;
		this.dotify = converter;
		setLeft(dotify);
	}

	public static EditorWrapperController newInstance(AnnotatedFile selected, Map<String, Object> options) {
		PreviewController pr = new PreviewController();
		Editor prv = getEditor(selected, options, pr);
		DotifyController dotify = null;
		if (options!=null) {
			try {
				File out = File.createTempFile("dotify-studio", ".pef");
				String tag = Settings.getSettings().getString(Keys.locale, Locale.getDefault().toLanguageTag());
				dotify = new DotifyController(selected, out, tag, options, f ->
				{
					Thread pefWatcher = pr.open(f);
					return f2 -> {
						pefWatcher.interrupt();
					};
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		EditorWrapperController ret = new EditorWrapperController(prv, dotify);
		ret.setCenter((Node)prv);
		return ret;
	}
	
	private static Editor getEditor(AnnotatedFile selected, Map<String, Object> options, PreviewController pr) {
		// For now, we assume that the target format is PEF and that is supported or that no conversion is done
		if (FeatureSwitch.EDITOR.isOn() && SourcePreviewController.supportsFormat(selected)) {
			if (options==null && !PreviewController.supportsFormat(selected)) {
				EditorController prv = new EditorController();
				prv.load(selected.getFile(), FormatChecker.isXML(selected));
				return prv;
			} else {
				SourcePreviewController prv = new SourcePreviewController();
				prv.open(selected, pr);
				if (options==null) {
					pr.open(selected.getFile());
				}
				return prv;
			}
		} else {
			return pr;
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
		if (dotify!=null) {
			dotify.closing();
		}
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

	/**
	 * Gets the options of this editor.
	 * @return returns the options.
	 */
	public Map<String, Object> getOptions() {
		return dotify!=null
				&& impl.canSave()?
						dotify.getParams():null;
	}

	@Override
	public void activate() {
		impl.activate();
	}

}
