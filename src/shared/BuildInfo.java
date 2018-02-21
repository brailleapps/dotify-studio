package shared;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

//TODO: move ManifestRetriever from dotify-cli to common and use that
public class BuildInfo {
	public final static String NAME;
	public final static String VERSION;
	public final static String BUILD;
	static {
		Class<BuildInfo> clazz = BuildInfo.class;
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		boolean failed = false;
		Attributes attr = null;
		if (!classPath.startsWith("jar")) {
		  // Class not from JAR
			failed = true;
		} else {
			String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + 
			    "/META-INF/MANIFEST.MF";
			Manifest manifest;
			try {
				manifest = new Manifest(new URL(manifestPath).openStream());
				attr = manifest.getMainAttributes();
			} catch (MalformedURLException e) {
				failed = true;
			} catch (IOException e) {
				failed = true;
			}
		}
		if (failed || attr == null) {
			NAME = "Dotify Studio";
			BUILD = "N/A";
			VERSION = "N/A";
		} else {
			NAME = attr.getValue("Implementation-Title");
			VERSION = attr.getValue("Implementation-Version");
			BUILD = attr.getValue("Repository-Revision");
		}
	}
}
