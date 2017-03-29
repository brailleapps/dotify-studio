package application.preview;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.TaskOption;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class OptionsController extends ScrollPane {
	private static final Logger logger = Logger.getLogger(OptionsController.class.getCanonicalName());
	@FXML
	public VBox vbox;

	public OptionsController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Options.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
	}
	
	public void addItem(TaskOption o) {
		OptionItem item = new OptionItem(o);
		vbox.setMargin(item, new Insets(0, 0, 10, 0));
		vbox.getChildren().add(item);
	}
	
	public void clear() {
		vbox.getChildren().clear();
	}
	
}
