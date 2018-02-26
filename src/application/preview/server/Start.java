package application.preview.server;

import java.util.Objects;

import com.googlecode.ajui.BrowserUI;

public class Start {
	private static final String RESOURCES_PATH = Start.class.getPackage().getName().replace('.', '/')+"/resource-files";
	private MainPage content;
	private BrowserUI ui;

	public String start(StartupDetails args) throws Exception  {
		Objects.requireNonNull(args);
		BrowserUI.Builder buildUi = new BrowserUI.Builder(RESOURCES_PATH);
		buildUi.timeout(5000);
		if (!args.shouldLog()) { 
			buildUi.logStream(null);
		}
		String page = "";
		content = new MainPage(args.getFile());
		page = "view.html";
		ui = buildUi.build();
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
	
	public void stopServer() {
		ui.stopServer();
	}

}
