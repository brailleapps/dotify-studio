package application.imports;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ImportBrailleView extends Stage {
	private static final Logger logger = Logger.getLogger(ImportBrailleView.class.getCanonicalName());
	private FXMLLoader loader;

	public ImportBrailleView(File path) {
		try {
			loader = new FXMLLoader(this.getClass().getResource("ImportBraille.fxml"), Messages.getBundle());
			Parent root = loader.load();
	    	setScene(new Scene(root));
	    	setResizable(false);
	    	loader.<ImportBrailleController>getController().setFile(path);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.TITLE_IMPORT_BRAILLE_OPTIONS_DIALOG.localize());
	}
	
	public Map<String, String> getOptions() {
		return loader.<ImportBrailleController>getController().getOptions();
	}

}
