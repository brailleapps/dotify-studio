package application.ui.prefs;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Provides a preferences view.
 * @author Joel Håkansson
 *
 */
public class PreferencesView extends Stage {
	private static final Logger logger = Logger.getLogger(PreferencesView.class.getCanonicalName());
	private PreferencesController controller;

	/**
	 * Creates a new preferences view.
	 */
	public PreferencesView() {
		try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Preferences.fxml"), Messages.getBundle());
			Parent root = loader.load();
			controller = loader.<PreferencesController>getController();
			Scene scene = new Scene(root);
	    	setScene(scene);
			scene.addEventHandler(KeyEvent.KEY_PRESSED, ev->{
				if (ev.getCode()==KeyCode.ESCAPE) {
					((Stage)scene.getWindow()).close();
				}
			});
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.PREFERENCES_WINDOW_TITLE.localize());
	}
	
	/**
	 * Returns a generated test file, or null if no file has been generated.
	 * @return returns a generated test file, or null
	 */
	public File generatedTestFile() {
		return controller.generatedTestFile();
	}
}
