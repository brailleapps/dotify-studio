package application.ui.preview;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class TemplateDetailsController {
	@FXML TextField name;
	@FXML TextArea desc;
	@FXML Button okButton;
	@FXML Button cancelButton;
	private Optional<NameDesc> nameDesc = Optional.empty();
	
	@FXML void initialize() {
		okButton.disableProperty().bind(name.textProperty().isEmpty());
		desc.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
			if (ke.getCode() == KeyCode.TAB) {
				// Fake focus previous/next item...
				if (ke.isShiftDown()) {
					name.requestFocus();
				} else if (okButton.isDisabled()) {
					cancelButton.requestFocus();
				} else {
					okButton.requestFocus();
				}
				ke.consume();
			}
		});
	}
	
	@FXML void okAction() {
		nameDesc = Optional.of(new NameDesc(name.getText(), desc.getText()));
		closeWindow();
	}

	@FXML void closeWindow() {
		((Stage)name.getScene().getWindow()).close();
	}
	
	Optional<NameDesc> getNameDesc() {
		return nameDesc;
	}

}
