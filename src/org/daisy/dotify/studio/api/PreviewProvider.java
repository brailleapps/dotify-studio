package org.daisy.dotify.studio.api;

import org.daisy.streamline.api.media.FileDetails;

public interface PreviewProvider {
	
	public boolean supportsFormat(FileDetails format);
	
	public OpenableEditor newPreview(FileDetails format);

}
