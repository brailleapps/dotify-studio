package application.prefs;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import application.template.TemplateController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PreferencesView extends Stage {
	private static final Logger logger = Logger.getLogger(PreferencesView.class.getCanonicalName());
	private FXMLLoader loader;

	public PreferencesView() {
		try {
			loader = new FXMLLoader(this.getClass().getResource("Preferences.fxml"), Messages.getBundle());
			Parent root = loader.load();
	    	setScene(new Scene(root));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.PREFERENCES_WINDOW_TITLE.localize());
	}
	
	public File generatedTestFile() {
		return loader.<PreferencesController>getController().generatedTestFile();
	}
}
