package application.template;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TemplateView extends Stage {
	private static final Logger logger = Logger.getLogger(TemplateView.class.getCanonicalName());
	private FXMLLoader loader;

	public TemplateView() {
		try {
			loader = new FXMLLoader(this.getClass().getResource("Template.fxml"), Messages.getBundle());
			Parent root = loader.load();
	    	setScene(new Scene(root));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.TITLE_TEMPLATES_DIALOG.localize());
	}
	
	public boolean hasTemplates() {
		return loader.<TemplateController>getController().hasTemplates();
	}
	
	public Map<String, Object> getSelectedConfiguration() {
		return loader.<TemplateController>getController().getSelectedConfiguration();
	}
}
