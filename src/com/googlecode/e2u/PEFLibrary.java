package com.googlecode.e2u;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PEFLibrary {
	private static final Logger logger = Logger.getLogger(PEFLibrary.class.getCanonicalName());
    private final ArrayList<File> files;
    private static FileFilter ff = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() || pathname.getName().endsWith(".pef");
		}
    };
    
	public PEFLibrary(File dir) {
    	files = new ArrayList<>();
    	listFiles(dir);
	}
	
	public Collection<File> getFileList() {
		return files;
	}
	
    private void listFiles(File dir) {
    	File[] listFiles = dir.listFiles(ff);
    	if (listFiles==null) {
    		return;
    	}
		for (File f : listFiles) {
			if (f.isDirectory()) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Scanning dir " + f);
				}
				listFiles(f);
			} else if (f.exists()) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Adding file: " + f);
				}
				files.add(f);
			} else {
				// ignore
			}
		}
    }

}