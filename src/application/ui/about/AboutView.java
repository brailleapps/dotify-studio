package application.ui.about;

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
 * Provides a dialog that displays information about the software.
 * @author Joel Håkansson
 */
public class AboutView extends Stage {
	private static final Logger logger = Logger.getLogger(AboutView.class.getCanonicalName());

	/**
	 * Creates a new dialog for information about the software.
	 */
	public AboutView() {
		try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("About.fxml"), Messages.getBundle());
			Parent root = loader.load();
			AboutController controller = loader.<AboutController>getController();
			Scene scene = new Scene(root);
	    	setScene(scene);
			scene.addEventHandler(KeyEvent.KEY_PRESSED, ev->{
				if (ev.getCode()==KeyCode.ESCAPE) {
					controller.closeWindow();
				}
			});
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.ABOUT_WINDOW_TITLE.localize());
	}
}
