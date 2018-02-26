package application.common;

import org.daisy.dotify.common.java.ManifestRetriever;

/**
 * Provides common properties for the system
 * @author Joel Håkansson
 */
public class BuildInfo {
	private static final ManifestRetriever retriever = new ManifestRetriever(BuildInfo.class);
	/**
	 * Defines the system name
	 */
	public final static String NAME = getWithDefault(retriever.getManifest().getMainAttributes().getValue("Implementation-Title"), "Dotify Studio");
	/**
	 * Defines the system release
	 */
	public final static String VERSION = getWithDefault(retriever.getManifest().getMainAttributes().getValue("Implementation-Version"), "N/A");
	/**
	 * Defines the system build
	 */
	public final static String BUILD = getWithDefault(retriever.getManifest().getMainAttributes().getValue("Repository-Revision"), "N/A");
	
	private static final <T> T getWithDefault(T val, T def) {
		return (val!=null?val:def);
	}
	
}
