package application.ui.preview;

import java.util.Collections;
import java.util.Map;

import org.daisy.streamline.api.media.FileDetails;

public class FileDetailsCatalog {
	public static final FileDetails PEF_FORMAT = new FileDetails(){
		@Override
		public String getFormatName() {
			return "pef";
		}

		@Override
		public String getExtension() {
			return "pef";
		}

		@Override
		public String getMediaType() {
			return "application/x-pef+xml";
		}

		@Override
		public Map<String, Object> getProperties() {
			return Collections.emptyMap();
		}};
		
	public static final FileDetails HTML_FORMAT = new FileDetails(){
		@Override
		public String getFormatName() {
			return "html";
		}

		@Override
		public String getExtension() {
			return "html";
		}

		@Override
		public String getMediaType() {
			return "text/html";
		}

		@Override
		public Map<String, Object> getProperties() {
			return Collections.emptyMap();
		}};


	private FileDetailsCatalog() {
		throw new AssertionError("No instances allowed.");
	}
}
