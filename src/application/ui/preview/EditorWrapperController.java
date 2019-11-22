package application.ui.preview;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.daisy.dotify.studio.api.Converter;
import org.daisy.dotify.studio.api.DocumentPosition;
import org.daisy.dotify.studio.api.Editor;
import org.daisy.dotify.studio.api.ExportAction;
import org.daisy.dotify.studio.api.OpenableEditor;
import org.daisy.dotify.studio.api.PreviewMaker;
import org.daisy.dotify.studio.api.SearchCapabilities;
import org.daisy.dotify.studio.api.SearchOptions;
import org.daisy.streamline.api.details.FormatDetailsProvider;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultFileDetails;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.media.FormatIdentifier;
import org.daisy.streamline.api.validity.ValidationReport;

import application.common.FeatureSwitch;
import application.common.Settings;
import application.common.Settings.Keys;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
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
	
	private static FileDetails getDetailsForFormat() {
		return FormatDetailsProvider.newInstance()
			.getDetails(FormatIdentifier.with(Settings.getSettings().getConvertTargetFormatName()))
			.map(v->{
				DefaultFileDetails.Builder fd = new DefaultFileDetails.Builder()
						.formatName(v.getIdentifier().getIdentifier());
				v.getExtensions().stream().findAny().ifPresent(x->fd.extension(x));
				v.getMediaType().ifPresent(x->fd.mediaType(x));
				return fd.build();
			}).orElseThrow(RuntimeException::new);
	}

	public static Optional<EditorWrapperController> newInstance(AnnotatedFile selected, Map<String, Object> options) {
		PreviewMaker previewMaker = PreviewMaker.newInstance();
		DotifyController dotify = null;
		Editor prv;
		if (options!=null) {
			FileDetails previewDetails = FeatureSwitch.SELECT_OUTPUT_FORMAT.isOn()?
				getDetailsForFormat()
				:FileDetailsCatalog.PEF_FORMAT;
			
			OpenableEditor pr = previewMaker.newPreview(previewDetails).orElse(null);
			prv = getEditor(selected, pr);
			try {
				String tag = Settings.getSettings().getString(Keys.locale, Locale.getDefault().toLanguageTag());
				dotify = new DotifyController(selected, tag, previewDetails, options, f ->
				{
					return pr.open(f);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			OpenableEditor pr = previewMaker.newPreview(selected).filter(v->!(v instanceof EditorController)).orElse(null);
			prv = getEditor(selected, pr);
			if (pr!=null) {
				pr.open(selected.getPath().toFile());
			} else if (prv==null) {
				return Optional.empty();
			}
		}
		EditorWrapperController ret = new EditorWrapperController(prv, dotify);
		ret.setCenter(prv.getNode());
		return Optional.of(ret);
	}
	
	private static Editor getEditor(AnnotatedFile selected, OpenableEditor pr) {
		if (EditorController.supportsFormat(selected)) {
			if (pr==null) {
				EditorController prv = new EditorController();
				prv.load(selected.getPath().toFile(), FormatChecker.isXML(selected));
				return prv;
			} else {
				SourcePreviewController prv = new SourcePreviewController();
				prv.open(selected, pr);
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
	public ObservableObjectValue<FileDetails> fileDetails() {
		return impl.fileDetails();
	}

	@Override
	public Optional<Converter> getConverter() {
		return Optional.ofNullable(dotify);
	}

	@Override
	public ObservableObjectValue<Optional<ValidationReport>> validationReport() {
		return impl.validationReport();
	}

	@Override
	public boolean scrollTo(DocumentPosition msg) {
		return impl.scrollTo(msg);
	}

	@Override
	public boolean findNext(String text, SearchOptions opts) {
		return impl.findNext(text, opts);
	}

	@Override
	public void replace(String replace) {
		impl.replace(replace);
	}

	@Override
	public ObservableObjectValue<SearchCapabilities> searchCapabilities() {
		return impl.searchCapabilities();
	}

	@Override
	public String getSelectedText() {
		return impl.getSelectedText();
	}

}
