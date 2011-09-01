package com.googlecode.e2u;

import java.io.File;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {
	public enum Keys {version, device, embosser, printMode, table, paper, cutLengthValue, cutLengthUnit, orientation, zFolding, charset, align, brailleFont, textFont, libraryPath}; 
	public final static String VERSION = "2011-09-01";
    private Preferences p;
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
	
	private void put(Keys key, String value) {
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

}
