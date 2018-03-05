package application.ui.find;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

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
	private final ResourceBundle resources;

	/**
	 * Creates a new dialog for information about the software.
	 */
	public FindView() {
		resources = ResourceBundle.getBundle(this.getClass().getName());
		try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Find.fxml"), resources);
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
		setTitle(resources.getString("find-window-title"));
	}
	
	public FindController getController() {
		return controller;
	}
	
}
