package application.common;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Provides a store for settings.
 * @author Joel Håkansson
 */
public enum Settings {
	INSTANCE;
	public enum Keys {version, device, embosser, printMode, table, paper, cutLengthValue, cutLengthUnit, orientation, zFolding, charset, align, brailleFont, textFont, libraryPath, locale,
		lastOpenPath, lastSavePath, convertTargetFormat, templateDialogOnImport, zoomLevel, autosave, lineNumbers, wordWrap};
		
	/**
	 *  Modify this value when making incompatible changes to the settings structure
	 */
	private final static String PREFS_VERSION = "1";
	private Preferences p;
	private DoubleProperty zoom;
	
    /**
     * Creates a new settings node at the specified path and with the specified defaults.
     * Note that the defaults must only use keys from the {@link Keys} enum.
     * @throws IllegalArgumentException if a key in the defaults map is not found in the {@link Keys} enum
     */
    private Settings() {
		HashMap<String, String> defaults = new HashMap<>();
		defaults.put(Settings.Keys.align.toString(), "center_inner");
		String path = "/DotifyStudio/prefs_v"+PREFS_VERSION;

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
		zoom = new SimpleDoubleProperty(getDouble(Keys.zoomLevel, 1.0));
		zoom.addListener((o, ov, nv)->{
			if (!ov.equals(nv)) {
			put(Keys.zoomLevel, Double.toString(nv.doubleValue()));
			}
		});
	}
    
    /**
     * Gets the settings instance.
     * @return returns the settings
     */
    public static Settings getSettings() {
    	return INSTANCE;
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
	
	private double getDouble(Keys key, double def) {
		try {
			String value = getString(key);
			return value!=null?Double.parseDouble(value):def;
		} catch (NullPointerException | NumberFormatException e) {
			return def;
		}
	}
	
	public void resetKey(Keys key) {
		p.remove(getRegKey(key));
	}
	
	public String getConvertTargetMediaType() {
		return getString(Keys.convertTargetFormat, "application/x-pef+xml");
	}
	
	public void setConvertTargetMediaType(String mediaType) {
		put(Keys.convertTargetFormat, mediaType);
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
	
	/**
	 * Returns true if the template dialog should be displayed on import, false otherwise.
	 * @return true if the template dialog should be displayed on import, false otherwise
	 */
	public boolean getShowTemplateDialogOnImport() {
		return Boolean.parseBoolean(getString(Keys.templateDialogOnImport, "true"));
	}
	
	/**
	 * Returns true if editors should wrap lines, false otherwise.
	 * @return true if editors should wrap lines, false otherwise
	 */
	public boolean shouldWrapLines() {
		return Boolean.parseBoolean(getString(Keys.wordWrap, "true"));
	}
	
	/**
	 * Returns true if editors should display line numbers changes, false otherwise.
	 * @return true if editors should display line numbers  changes, false otherwise
	 */
	public boolean shouldShowLineNumbers() {
		return Boolean.parseBoolean(getString(Keys.lineNumbers, "true"));
	}
	
	/**
	 * Returns true if editors should auto-save changes, false otherwise.
	 * @return true if editors should auto-save changes, false otherwise
	 */
	public boolean shouldAutoSave() {
		return Boolean.parseBoolean(getString(Keys.autosave, "false"));
	}
	
	public double getZoomLevel() {
		return zoom.get();
	}
	
	public void setZoomLevel(double value) {
		zoom.set(value);
	}
	
	public DoubleProperty zoomLevelProperty() {
		return zoom;
	}
	
	/**
	 * Sets if the template dialog should be displayed on import. When true, it should.
	 * When false, it should not. 
	 * @param value the value
	 */
	public void setShowTemplateDialogOnImport(boolean value) {
		put(Keys.templateDialogOnImport, ""+value);
	}

	/**
	 * Sets if line numbers should be visible in the editors. When true, they should.
	 * When false, they should not. 
	 * @param value the value
	 */
	public void setLineNumbers(boolean value) {
		put(Keys.lineNumbers, ""+value);
	}
	
	/**
	 * Sets if word wrap should be active in the editors. When true, it should.
	 * When false, it should not. 
	 * @param value the value
	 */
	public void setWordWrap(boolean value) {
		put(Keys.wordWrap, ""+value);
	}

	/**
	 * Sets if auto-save should be active. When true, it should.
	 * When false, it should not. 
	 * @param value the value
	 */
	public void setAutoSave(boolean value) {
		put(Keys.autosave, ""+value);
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
