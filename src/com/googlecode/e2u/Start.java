package com.googlecode.e2u;

import com.googlecode.ajui.BrowserUI;
import com.googlecode.e2u.StartupDetails.Mode;

public class Start {
	private MainPage content;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new Start().start(StartupDetails.parse(args));
	}

	public String start(StartupDetails args) throws Exception  {
		if (args==null) {
			System.out.println("Supplied arguments do not match any of the following:");
			System.out.println("\t-setup");
			System.out.println("\t-open path-to-file");
			System.out.println("\t-print path-to-file");
			//System.out.println("\t-emboss path-to-file");
			//System.out.println("\t-view path-to-file");
			content = new MainPage(null);
			System.exit(-1);			
		}
		BrowserUI.Builder buildUi = new BrowserUI.Builder("com/googlecode/e2u/resource-files");
		buildUi.timeout(5000);
		if (!args.shouldLog()) { 
			buildUi.logStream(null);
		}
		String page = "";
		if (args.getMode()==Mode.UNDEFINED) {
			page = "";
			content = new MainPage(null);
		} else if (args.getMode()==Mode.SETUP) {
			page = "index.html?method=setup";
			content = new MainPage(args.getFile());
		} else if (args.getMode()==Mode.EMBOSS) {
			content = new MainPage(args.getFile());
			page = "index.html?method=do";
		} else if (args.getMode()==Mode.OPEN) {
			content = new MainPage(args.getFile());
			page = "view.html";
		} else if (args.getMode()==Mode.VIEW) {
			content = new MainPage(args.getFile());
			page = "";
		} else {
			throw new RuntimeException("Coding error.");
		}
		BrowserUI ui = buildUi.build();
		ui.registerContents(content);
		if (args.shouldDisplay()) {
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
