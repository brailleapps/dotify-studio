package shared;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Provides a store for settings.
 * @author Joel Håkansson
 */
public class Settings {
	public enum Keys {version, device, embosser, printMode, table, paper, cutLengthValue, cutLengthUnit, orientation, zFolding, charset, align, brailleFont, textFont, libraryPath, locale,
		lastOpenPath, lastSavePath};
	/**
	 *  Modify this value when making incompatible changes to the settings structure
	 */
	private final static String PREFS_VERSION = "1";
    private Preferences p;
    private static Settings settings;
	
    /**
     * Creates a new settings node at the specified path and with the specified defaults.
     * Note that the defaults must only use keys from the {@link Keys} enum.
     * @param path the path
     * @param defaults the defaults
     * @throws IllegalArgumentException if a key in the defaults map is not found in the {@link Keys} enum
     */
    private Settings(String path, HashMap<String, String> defaults) {
        p = Preferences.userRoot().node(path); //$NON-NLS-1$
        if (!BuildInfo.VERSION.equals(p.get(Keys.version.toString(), ""))) {
        	// if no version information is found, clear and add defaults
        	if ("".equals(p.get(Keys.version.toString(), ""))) {
	        	try {
					p.clear();
					for (String key : defaults.keySet()) {
						// Is this a way of validating the key?
						Keys.valueOf(key);
						p.put(key, defaults.get(key));
					}
				} catch (BackingStoreException e) { 	}
        	}
        	// Update the version of the application used to write this
        	p.put(Keys.version.toString(), BuildInfo.VERSION);
        }
    }
    
    /**
     * Gets the default settings instance.
     * @return returns the settings
     */
    public synchronized static Settings getSettings() {
    	if (settings==null) {
    		HashMap<String, String> def = new HashMap<>();
    		def.put(Settings.Keys.align.toString(), "center_inner");
    		settings = new Settings("/DotifyStudio/prefs_v"+PREFS_VERSION, def);
    	}
    	return settings;
    }
    
    private String getHash(Keys key) {
    	String dk = getString(key);
    	if (dk==null) {
    		return "";
    	}
    	return Integer.toHexString(dk.hashCode()); 
    }
    
    private String getRegKey(Keys key) {
    	switch (key) {
    		case embosser: {
    			return getHash(Keys.device) + ":embosser";  
    		}
    		case table : {
    			return getHash(Keys.device) + ":" + getHash(Keys.embosser) + ":table";
    		}
    		case paper : {
    			return  getHash(Keys.device) + ":" + getHash(Keys.embosser) + ":paper";
    		}
    		case orientation : {
    			return  getHash(Keys.device) + ":" + getHash(Keys.embosser) + ":" + getHash(Keys.paper) + ":orientation";
    		}
    		default:break;
    	}
    	return key.toString();
    }

	public String getSetPref(Keys key, String cval) {
		return getSetPref(key, cval, "");
	}
	
	public String getSetPref(Keys key, String cval, String def) {
    	if (cval==null) {
    		cval=getString(key, def); //$NON-NLS-1$ //$NON-NLS-2$
    	} else {
    		put(key, cval);
    	}
    	return cval;		
	}
	
	/**
	 * Sets the value for the specified key.
	 * @param key the key
	 * @param value the value
	 */
	public void put(Keys key, String value) {
		p.put(getRegKey(key), value); //$NON-NLS-1$
	}
	
	/**
	 * Gets the string value for key
	 * @param key the key
	 * @return returns value for key, or null if not found
	 */
	public String getString(Keys key) {
		return getString(key, null);
	}
	
	public String getString(Keys key, String def) {
		return p.get(getRegKey(key), def);
	}
	
	public void resetKey(Keys key) {
		p.remove(getRegKey(key));
	}
	
	public File getLibraryPath() {
		String path = getString(Keys.libraryPath);
        if (path == null || !(new File(path).exists())) {
        	path = System.getProperty("user.home");
        	put(Keys.libraryPath, path);
        }
        return new File(path);
	}
	
	public boolean setLibraryPath(String path) {
		return setPath(path, Keys.libraryPath);
	}
	
	public Optional<File> getLastOpenPath() {
		return getPath(Keys.lastOpenPath);
	}
	
	public void setLastOpenPath(File path) {
		setPath(path, Keys.lastOpenPath);
	}
	
	public Optional<File> getLastSavePath() {
		return getPath(Keys.lastSavePath);
	}
	
	public void setLastSavePath(File path) {
		setPath(path, Keys.lastSavePath);
	}
	
	private Optional<File> getPath(Keys pathKey) {
		Objects.requireNonNull(pathKey);
		String path = getString(pathKey);
		if (path != null && (new File(path).exists())) {
			return Optional.of(new File(path));
		} else {
			return Optional.empty();
		}
	}
	
	private boolean setPath(File path, Keys pathKey) {
		Objects.requireNonNull(path);
		Objects.requireNonNull(pathKey);
		return setPath(path.getAbsolutePath(), pathKey);
	}
	
	private boolean setPath(String path, Keys pathKey) {
		if (path==null || path.equals("") || !new File(path).isDirectory()) {
			return false;
		} else {
			put(pathKey, path);
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Keys v : Keys.values()) {
			if (first) {
				first = false;
			} else {
				 sb.append(", ");
			}
			sb.append(v + "=" + getString(v));
		}
		return "Settings [" + sb + "]";
	}

}
