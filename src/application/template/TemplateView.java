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

/**
 * Provides a template view.
 * @author Joel Håkansson
 */
public class TemplateView extends Stage {
	private static final Logger logger = Logger.getLogger(TemplateView.class.getCanonicalName());
	private FXMLLoader loader;

	/**
	 * Creates a new template view.
	 */
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
	
	/**
	 * Returns true if the view has templates in it, false otherwise.
	 * @return return true if the view has templates, false otherwise
	 */
	public boolean hasTemplates() {
		return loader.<TemplateController>getController().hasTemplates();
	}
	
	/**
	 * Gets the selected configuration, or an empty map if none is selected.
	 * @return returns the selected configuration, or an empty map
	 */
	public Map<String, Object> getSelectedConfiguration() {
		return loader.<TemplateController>getController().getSelectedConfiguration();
	}
}
