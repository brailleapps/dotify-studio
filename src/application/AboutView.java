package application;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AboutView extends Stage {
	private static final Logger logger = Logger.getLogger(AboutView.class.getCanonicalName());

	public AboutView() {
		try {
			Parent root = FXMLLoader.load(this.getClass().getResource("About.fxml"), Messages.getBundle());
	    	setScene(new Scene(root));
	    	setResizable(false);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.ABOUT_WINDOW_TITLE.localize());
	}
}
