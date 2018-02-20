package application.ui.imports;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ImportMergeController {
	@FXML private Button ok;
	@FXML private Button cancel;
	@FXML private Button upButton;
	@FXML private Button downButton;
	@FXML private TextField identifier;
	@FXML private ListView<FileCell> filesList;
	private List<File> files = null;

	private static class FileCell {
		private final File file;
		private FileCell(File f) {
			this.file = f;
		}
		@Override
		public String toString() {
			return file.getName();
		}
		
	}
	@FXML public void moveUp() {
		int index = filesList.getSelectionModel().getSelectedIndex();
		if (index>0) {
			FileCell r = filesList.getItems().remove(index);
			filesList.getItems().add(index-1, r);
			filesList.getSelectionModel().select(index-1);
		}
	}
	
	@FXML public void moveDown() {
		int index = filesList.getSelectionModel().getSelectedIndex();
		if (index>=0 && index<filesList.getItems().size()-1) {
			FileCell r = filesList.getItems().remove(index);
			filesList.getItems().add(index+1, r);
			filesList.getSelectionModel().select(index+1);
		}
	}

	/**
	 * Closes the dialog.
	 */
	@FXML
	public void closeWindow() {
		((Stage)cancel.getScene().getWindow()).close();
	}
	
	/**
	 * Sets the state of the dialog to perform import.
	 */
	@FXML
	public void doImport() {
		files = filesList.getItems().stream().map(v->v.file).collect(Collectors.toList());
		((Stage)ok.getScene().getWindow()).close();
	}

	void setFiles(List<File> files) {
		for (File f : files) {
			filesList.getItems().add(new FileCell(f));
		}
	}
	
	List<File> getFiles() {
		if (isCancelled()) {
			throw new IllegalStateException();
		}
		return files;
	}
	
	Optional<String> getIdentifier() {
		if (isCancelled()) {
			throw new IllegalStateException();
		}
		if (!"".equals(identifier.getText())) {
			return Optional.of(identifier.getText());
		} else {
			return Optional.empty();
		}
	}
	
	boolean isCancelled() {
		return files==null;
	}

}
