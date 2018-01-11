package application.preview;

import org.daisy.dotify.studio.api.OpenableEditor;
import org.daisy.dotify.studio.api.PreviewProvider;
import org.daisy.streamline.api.media.FileDetails;

public class PefPreviewProviderImpl implements PreviewProvider {
	
	@Override
	public boolean supportsFormat(FileDetails format) {
		return FormatChecker.isPEF(format) || FormatChecker.isHTML(format);
	}

	@Override
	public OpenableEditor newPreview(FileDetails selected) {
		if (FormatChecker.isPEF(selected)) {
			PreviewController prv = new PreviewController();
			return prv;
		} else if (FormatChecker.isHTML(selected)) {
			PreviewHtmlController prv = new PreviewHtmlController();
			return prv;
		}
		throw new RuntimeException();
	}

}
