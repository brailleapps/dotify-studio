package com.googlecode.e2u;

import java.io.File;

import com.googlecode.ajui.BrowserUI;

public class Start {
	private MainPage content;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		run(args, true);
	}
	
	public static String run(String[] args, boolean display) throws Exception {
		return run(args, display, true);
	}

	public static String run(String[] args, boolean display, boolean log) throws Exception {
		return new Start().start(args, display, log);
	}
	
	public String start(String[] args, boolean display, boolean log) throws Exception  {
		BrowserUI.Builder buildUi = new BrowserUI.Builder("com/googlecode/e2u/resource-files");
		buildUi.timeout(5000);
		if (!log) { 
			buildUi.logStream(null);
		}
		String page = "";
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
		BrowserUI ui = buildUi.build();
		ui.registerContents(content);
		if (display) {
			ui.display(page);
			return null;
		} else {
			return ui.start(page);
		}
	}
	
	public MainPage getMainPage() {
		return content;
	}

}
