package application.ui.imports;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.l10n.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Provides a dialog that merges PEF-files.
 * @author Joel Håkansson
 */
public class ImportMergeView extends Stage {
	private static final Logger logger = Logger.getLogger(ImportMergeView.class.getCanonicalName());
	private ImportMergeController controller;

	/**
	 * Creates a new merge dialog with the specified files.
	 * @param selected the files
	 */
	public ImportMergeView(List<File> selected) {
		try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("ImportMerge.fxml"), Messages.getBundle());
			Parent root = loader.load();
			controller = loader.<ImportMergeController>getController();
			Scene scene = new Scene(root);
	    	setScene(scene);
			scene.addEventHandler(KeyEvent.KEY_PRESSED, ev->{
				if (ev.getCode()==KeyCode.ESCAPE) {
					controller.closeWindow();
				}
			});
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP, KeyCodeCombination.ALT_DOWN), ()->{
				controller.moveUp();
			});
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN, KeyCodeCombination.ALT_DOWN), ()->{
				controller.moveDown();
			});
	    	controller.setFiles(selected);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		setTitle(Messages.TITLE_IMPORT_BRAILLE_OPTIONS_DIALOG.localize());
	}
	
	public List<File> getFiles() {
		return controller.getFiles();
	}
	
	public Optional<String> getIdentifier() {
		return controller.getIdentifier();
	}
	
	public boolean isCancelled() {
		return controller.isCancelled();
	}

}
