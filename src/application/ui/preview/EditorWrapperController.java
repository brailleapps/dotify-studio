package application.ui.preview;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.daisy.dotify.studio.api.Converter;
import org.daisy.dotify.studio.api.Editor;
import org.daisy.dotify.studio.api.ExportAction;
import org.daisy.dotify.studio.api.FileDetailsProperty;
import org.daisy.dotify.studio.api.OpenableEditor;
import org.daisy.dotify.studio.api.PreviewMaker;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.FileDetails;

import application.common.FeatureSwitch;
import application.common.Settings;
import application.common.Settings.Keys;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class EditorWrapperController extends BorderPane implements Editor {
	private final Editor impl;
	private final DotifyController dotify;
	private final ObservableBooleanValue canEmboss;
	private final ObservableBooleanValue canSaveAs;
	
	private EditorWrapperController(Editor impl, DotifyController converter) {
		this.impl = impl;
		this.dotify = converter;
		this.canSaveAs = dotify!=null ? dotify.isIdleProperty().and(impl.canSaveAs()):impl.canSaveAs();
		this.canEmboss = dotify!=null ? dotify.isIdleProperty().and(impl.canEmboss()):impl.canEmboss();
		setLeft(dotify);
	}

	public static EditorWrapperController newInstance(AnnotatedFile selected, Map<String, Object> options) {
		PreviewMaker previewMaker = PreviewMaker.newInstance();
		DotifyController dotify = null;
		Editor prv;
		if (options!=null) {
			FileDetails previewDetails = FeatureSwitch.SELECT_OUTPUT_FORMAT.isOn()?
					FileDetailsCatalog.forMediaType(Settings.getSettings().getConvertTargetFormat())
					:FileDetailsCatalog.PEF_FORMAT;
			OpenableEditor pr = previewMaker.newPreview(previewDetails).orElse(null);
			prv = getEditor(selected, options, pr, previewMaker);
			try {
				File out = File.createTempFile("dotify-studio", "."+previewDetails.getExtension());
				out.deleteOnExit();
				String tag = Settings.getSettings().getString(Keys.locale, Locale.getDefault().toLanguageTag());
				dotify = new DotifyController(selected, out, tag, previewDetails.getExtension(), options, f ->
				{
					return pr.open(f);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			OpenableEditor pr = previewMaker.newPreview(selected).orElse(null);
			prv = getEditor(selected, options, pr, previewMaker);
		}
		EditorWrapperController ret = new EditorWrapperController(prv, dotify);
		ret.setCenter(prv.getNode());
		return ret;
	}
	
	private static Editor getEditor(AnnotatedFile selected, Map<String, Object> options, OpenableEditor pr, PreviewMaker previewMaker) {
		// For now, we assume that the target format is PEF and that is supported or that no conversion is done
		if (EditorController.supportsFormat(selected)) {
			if (options==null && !previewMaker.supportsFormat(selected)) {
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
	public ObservableBooleanValue canSave() {
		return impl.canSave();
	}
	
	@Override
	public ObservableBooleanValue canSaveAs() {
		return canSaveAs;
	}

	@Override
	public void save() {
		impl.save();
	}

	@Override
	public boolean saveAs(File f) throws IOException {
		return impl.saveAs(f);
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
	public ObservableBooleanValue canEmboss() {
		return canEmboss;
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
	public void toggleViewingMode() {
		impl.toggleViewingMode();
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
				&& impl.canSave().get()?
						dotify.getParams():null;
	}

	@Override
	public void activate() {
		impl.activate();
	}

	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public void export(Window ownerWindow, ExportAction action) throws IOException {
		impl.export(ownerWindow, action);
	}

	@Override
	public FileDetailsProperty fileDetailsProperty() {
		return impl.fileDetailsProperty();
	}

	@Override
	public Optional<Converter> getConverter() {
		return Optional.ofNullable(dotify);
	}

}
