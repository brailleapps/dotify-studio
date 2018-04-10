package application.ui.preview;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class TemplateDetailsView extends Stage {
	private static final Logger logger = Logger.getLogger(TemplateDetailsView.class.getCanonicalName());
	private FXMLLoader loader;
	
	public TemplateDetailsView() {
		try {
			loader = new FXMLLoader(this.getClass().getResource("TemplateDetails.fxml"), Messages.getBundle());
			Parent root = loader.load();
			TemplateDetailsController controller = loader.<TemplateDetailsController>getController();
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
		setTitle(Messages.TITLE_SAVE_TEMPLATES_DIALOG.localize());
	}
	
	public Optional<NameDesc> getResult() {
		return loader.<TemplateDetailsController>getController().getNameDesc();
	}
}
