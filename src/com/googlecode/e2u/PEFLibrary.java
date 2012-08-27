package com.googlecode.e2u;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

public class PEFLibrary {
	private final static boolean debug = false;
    private final ArrayList<File> files;
    private static FileFilter ff = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() || pathname.getName().endsWith(".pef");
		}
    };
    
	public PEFLibrary(File dir) {
    	files = new ArrayList<File>();
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
				if (debug) System.err.println("Scanning dir " + f);
				listFiles(f);
			} else if (f.exists()) {
				if (debug) System.err.println("Adding file: " + f);
				files.add(f);
			} else {
				// ignore
			}
		}
    }

}