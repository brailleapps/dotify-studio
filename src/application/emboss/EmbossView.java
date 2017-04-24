package application.emboss;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.pef.PEFBook;

import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EmbossView extends Stage {
	private static final Logger logger = Logger.getLogger(EmbossView.class.getCanonicalName());

	public EmbossView(PEFBook book) {
		try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Emboss.fxml"), Messages.getBundle());
			Parent root = loader.load();
			EmbossController controller = loader.<EmbossController>getController();
	    	setScene(new Scene(root));
	    	controller.setBook(book);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.EMBOSS_WINDOW_TITLE.localize());
	}
}
