package application.prefs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PreferencesView extends Stage {
	private static final Logger logger = Logger.getLogger(PreferencesView.class.getCanonicalName());

	public PreferencesView() {
		try {
			Parent root = FXMLLoader.load(this.getClass().getResource("Preferences.fxml"), Messages.getBundle());
	    	setScene(new Scene(root));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.PREFERENCES_WINDOW_TITLE.localize());
	}
}
