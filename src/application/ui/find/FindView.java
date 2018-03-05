package application.ui.find;

import java.io.IOException;
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
public class FindView extends Stage {
	private static final Logger logger = Logger.getLogger(FindView.class.getCanonicalName());
	private final FindController controller;

	/**
	 * Creates a new dialog for information about the software.
	 */
	public FindView() {
		try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Find.fxml"), Messages.getBundle());
			Parent root = loader.load();
			controller = loader.<FindController>getController();
			Scene scene = new Scene(root);
	    	setScene(scene);
			scene.addEventHandler(KeyEvent.KEY_PRESSED, ev->{
				if (ev.getCode()==KeyCode.ESCAPE) {
					controller.closeWindow();
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		setTitle(Messages.FIND_WINDOW_TITLE.localize());
	}
	
	public FindController getController() {
		return controller;
	}
	
}
