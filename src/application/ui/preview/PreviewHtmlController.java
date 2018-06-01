package application.ui.preview;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.daisy.dotify.studio.api.DocumentPosition;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.validity.ValidationReport;

import application.l10n.Messages;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public class PreviewHtmlController extends AbstractHtmlController {
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private ObjectProperty<FileDetails> fileDetails = new SimpleObjectProperty<>(FileDetailsCatalog.HTML_FORMAT);

	/**
	 * Creates a new preview controller.
	 */
	public PreviewHtmlController() {
		super();
		canEmbossProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
	}

    @Override
	public Consumer<File> open(File file) {
		if (file!=null) {
			String url;
			try {
				url = file.toURI().toURL().toString();
				this.urlProperty.set(url);
				if (url!=null) {
					browser.getEngine().load(url);
				} else {
					browser.getEngine().load(getClass().getResource("resource-files/fail.html").toString());
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return super.open(file);
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		return Arrays.asList(new ExtensionFilter(Messages.EXTENSION_FILTER_FILE.localize("HTML"), "*.html"));
	}

	@Override
	public ObservableBooleanValue canEmboss() {
		return canEmbossProperty;
	}

	@Override
	public ObservableObjectValue<FileDetails> fileDetails() {
		return fileDetails;
	}

	@Override
	public ObservableObjectValue<Optional<ValidationReport>> validationReport() {
		return new SimpleObjectProperty<>(Optional.empty());
	}
	
	@Override
	public boolean scrollTo(DocumentPosition msg) {
		return false;
	}

}
