package com.googlecode.e2u;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class BuildInfo {
	public final static String VERSION;
	public final static String BUILD;
	static {
		Class<MainPage> clazz = MainPage.class;
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
			BUILD = "N/A";
			VERSION = "N/A";
		} else {
			VERSION = attr.getValue("Implementation-Version");
			BUILD = attr.getValue("Repository-Revision");
		}
	}
}
