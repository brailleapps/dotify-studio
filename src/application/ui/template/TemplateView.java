package application.ui.template;

import java.io.File;
import java.io.IOException;
import java.util.Map;
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
 * Provides a template view.
 * @author Joel Håkansson
 */
public class TemplateView extends Stage {
	private static final Logger logger = Logger.getLogger(TemplateView.class.getCanonicalName());
	private FXMLLoader loader;

	public TemplateView() {
		this(null);
	}
	/**
	 * Creates a new template view.
	 * @param file the file target
	 */
	public TemplateView(File file) {
		String title = Messages.TITLE_TEMPLATES_DIALOG.localize(file==null?"":file.getName());
		try {
			loader = new FXMLLoader(this.getClass().getResource("Template.fxml"), Messages.getBundle());
			Parent root = loader.load();
			TemplateController controller = loader.<TemplateController>getController();
			Scene scene = new Scene(root);
	    	setScene(scene);
			scene.addEventHandler(KeyEvent.KEY_PRESSED, ev->{
				if (ev.getCode()==KeyCode.ESCAPE) {
					controller.closeWindow();
				}
			});
	    	loader.<TemplateController>getController().setHeading(title);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(title);
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
