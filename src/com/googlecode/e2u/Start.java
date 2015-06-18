package com.googlecode.e2u;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.daisy.braille.pef.FileTools;

import com.googlecode.ajui.BrowserUI;

public class Start {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		BrowserUI.Builder buildUi = new BrowserUI.Builder("com/googlecode/e2u/resource-files");
		buildUi.timeout(5000);
		String page = "";
		MainPage content;
		if (args.length==0) {
			page = "";
			content = new MainPage(null);
		} else if (args.length==1 && args[0].equalsIgnoreCase("-setup")) {
			page = "index.html?method=setup";
			content = new MainPage(null);
		} else if (args.length==2 && args[0].equalsIgnoreCase("-emboss")) {
			content = new MainPage(new File(args[1]));
			page = "index.html?method=do";
		} else if (args.length==2 && args[0].equalsIgnoreCase("-open")) {
			content = new MainPage(new File(args[1]));
			page = "view.html";
		} else if (args.length==2 && (args[0].equalsIgnoreCase("-view") || args[0].equalsIgnoreCase("-print"))) {
			content = new MainPage(new File(args[1]));
			page = "";
		} else {
			System.out.println("Supplied arguments do not match any of the following:");
			System.out.println("\t-setup");
			System.out.println("\t-open path-to-file");
			System.out.println("\t-print path-to-file");
			//System.out.println("\t-emboss path-to-file");
			//System.out.println("\t-view path-to-file");
			content = new MainPage(null);
			System.exit(-1);
		}
		//TODO: check error conditions, such as null
		File parent = new File((Start.class.getProtectionDomain().getCodeSource().getLocation()).toURI()).getParentFile();
		System.out.println(parent);
		// list jars and convert to URL's
		URL[] jars = FileTools.toURL(FileTools.listFiles(new File(parent, "plugins"), ".jar"));
		for (URL u : jars) {
			System.out.println("Found jar " + u);
		}
		// set context class loader
		if (jars.length>0) {
			Thread.currentThread().setContextClassLoader(new URLClassLoader(jars));
		}
		BrowserUI ui = buildUi.build();
		ui.registerContents(content);
		ui.display(page);
	}

}
