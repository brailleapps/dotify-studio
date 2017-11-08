package application.preview;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.utils.pef.PEFBook;

import application.FeatureSwitch;
import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Provides an embosser view.
 * @author Joel Håkansson
 *
 */
public class EmbossView extends Stage {
	private static final Logger logger = Logger.getLogger(EmbossView.class.getCanonicalName());
	private EmbossController controller;

	/**
	 * Creates a new embosser view with the specified book.
	 * @param book the book
	 */
	public EmbossView(PEFBook book) {
		try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Emboss.fxml"), Messages.getBundle());
			Parent root = loader.load();
			controller = loader.<EmbossController>getController();
			Scene scene = new Scene(root);
	    	setScene(scene);
			scene.addEventHandler(KeyEvent.KEY_PRESSED, ev->{
				if (ev.getCode()==KeyCode.ESCAPE) {
					controller.closeWindow();
				}
			});
	    	controller.setBook(book);
	    	if (!FeatureSwitch.EMBOSSING.isOn()) {
	    		logger.info("Embossing is deactivated.");
	    	}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.EMBOSS_WINDOW_TITLE.localize());
	}
	
	/**
	 * Sets the book for this view.
	 * @param book the book
	 */
	public void setBook(PEFBook book) {
		controller.setBook(book);
	}
}
