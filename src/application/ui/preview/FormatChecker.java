package application.ui.preview;

import java.util.regex.Pattern;

import org.daisy.streamline.api.media.FileDetails;

public final class FormatChecker {
	static final Pattern XML_PATTERN = Pattern.compile("\\Qapplication/\\E([\\w-]+\\+)?\\Qxml\\E");
	static final Pattern TEXT_PATTERN = Pattern.compile("\\Qtext/\\E.+");

	private FormatChecker() {
		throw new AssertionError("No instances allowed.");
	}
	
	public static boolean isXML(FileDetails af) {
		return af.getMediaType()!=null && XML_PATTERN.matcher(af.getMediaType()).matches();
	}

	public static boolean isHTML(FileDetails af) { 
		return af.getMediaType()!=null && ("text/html".equals(af.getMediaType())
				|| isXML(af) && "html".equals(af.getProperties().get("local-name"))
				);
	}

	public static boolean isText(FileDetails af) {
		return af.getMediaType()!=null && TEXT_PATTERN.matcher(af.getMediaType()).matches()
				|| af.getMediaType()==null && af.getExtension()!=null && "txt".equals(af.getExtension());
	}
	
	public static boolean isPEF(FileDetails af) {
		return af.getMediaType()!=null && "application/x-pef+xml".equals(af.getMediaType());
	}
}
