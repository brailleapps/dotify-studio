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
		
	public static final FileDetails XHTML_FORMAT = new FileDetails(){
		@Override
		public String getFormatName() {
			return "xhtml";
		}

		@Override
		public String getExtension() {
			return "xhtml";
		}

		@Override
		public String getMediaType() {
			return "application/xhtml+xml";
		}

		@Override
		public Map<String, Object> getProperties() {
			return Collections.emptyMap();
		}};
		
		public static final FileDetails FORMATTED_TEXT_FORMAT = new FileDetails(){
			@Override
			public String getFormatName() {
				return "formatted-text";
			}

			@Override
			public String getExtension() {
				return "txt";
			}

			@Override
			public String getMediaType() {
				return "text/plain";
			}

			@Override
			public Map<String, Object> getProperties() {
				return Collections.emptyMap();
			}};


	private FileDetailsCatalog() {
		throw new AssertionError("No instances allowed.");
	}
	
	public static FileDetails forMediaType(String mediaType) {
		if (mediaType.equals(PEF_FORMAT.getMediaType())) {
			return PEF_FORMAT;
		} else if (mediaType.equals(XHTML_FORMAT.getMediaType())) {
			return XHTML_FORMAT;
		} else if (mediaType.equals(FORMATTED_TEXT_FORMAT.getMediaType())) {
			return FORMATTED_TEXT_FORMAT;
		} else {
			return new MediaTypeDetails(mediaType);
		}
	}
	
	private static class MediaTypeDetails implements FileDetails {
		private final String mediaType;
		
		private MediaTypeDetails(String mediaType) {
			this.mediaType = mediaType;
		}

		@Override
		public String getFormatName() {
			return null;
		}

		@Override
		public String getExtension() {
			return null;
		}

		@Override
		public String getMediaType() {
			return mediaType;
		}

		@Override
		public Map<String, Object> getProperties() {
			return Collections.emptyMap();
		}
		
	}
}
