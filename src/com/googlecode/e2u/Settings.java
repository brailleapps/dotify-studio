package com.googlecode.e2u;

import java.io.File;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {
	public enum Keys {version, device, embosser, printMode, table, paper, cutLengthValue, cutLengthUnit, orientation, zFolding, charset, align, brailleFont, textFont, libraryPath, locale}; 
	public final static String VERSION = "2017-04-19";
    private Preferences p;
    private static Settings settings;
    //private PaperCatalog paperFactory;
  //  private EmbosserCatalog embosserFactory;
	
    public Settings(String node, HashMap<String, String> defaults) {
        p = Preferences.userRoot().node(node); //$NON-NLS-1$
        if (!VERSION.equals(p.get(Keys.version.toString(), ""))) {
        	try {
				p.clear();
				for (String key : defaults.keySet()) {
					Keys.valueOf(key);
					p.put(key, defaults.get(key));
				}
			} catch (BackingStoreException e) { 	}
        	p.put(Keys.version.toString(), VERSION);
        }
        //paperFactory = PaperCatalog.newInstance();
        //embosserFactory = EmbosserCatalog.newInstance();
    }
    
    public synchronized static Settings getSettings() {
    	if (settings==null) {
    		HashMap<String, String> def = new HashMap<>();
    		def.put(Settings.Keys.align.toString(), "center_inner");
    		settings = new Settings("/DotifyStudio"+BuildInfo.VERSION, def);
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
	
	public void put(Keys key, String value) {
		p.put(getRegKey(key), value); //$NON-NLS-1$
	}
	
	/**
	 * Gets the string value for key
	 * @param key
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
	
	/**
	 * Gets currently selected paper
	 * @return returns currently selected paper, or null if not found
	 *//*
	public Paper getPaper() {
		return paperFactory.get(getString(Keys.paper));
	}
	
	public Embosser getEmbosser() {
		return embosserFactory.get(getString(Keys.embosser));
	}*/
	
	public File getLibraryPath() {
		String path = getString(Keys.libraryPath);
        if (path == null || !(new File(path).exists())) {
        	path = System.getProperty("user.home");
        	put(Keys.libraryPath, path);
        }
        return new File(path);
	}
	
	public boolean setLibraryPath(String path) {
		if (path==null || path.equals("") || !new File(path).exists()) {
			return false;
		} else {
			put(Keys.libraryPath, path);
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
