package application.ui.find;


import org.daisy.dotify.studio.api.SearchCapabilities;
import org.daisy.dotify.studio.api.SearchOptions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Provides a controller for the dialog that displays information about the software.
 * @author Joel Håkansson
 *
 */
public class FindController {
	@FXML private Button closeButton;
	@FXML private Button findButton;
	@FXML private Button replaceButton;
	@FXML private Button replaceFindButton;
	@FXML private TextField findField;
	@FXML private TextField replaceField;
	@FXML private StackPane dirStackPane;
	@FXML private CheckBox caseSensitiveCheckbox;
	@FXML private CheckBox wrapCheckbox;
	@FXML private ToggleGroup directionToggleGroup;
	@FXML private RadioButton forwardRadioButton;
	@FXML private RadioButton backwardRadioButton;

	/**
	 * Initializes the controller.
	 */
	@FXML void initialize() {
		forwardRadioButton.setUserData(false);
		backwardRadioButton.setUserData(true);
	}

	/**
	 * Closes the window.
	 */
	@FXML void closeWindow() {
		((Stage)closeButton.getScene().getWindow()).close();
	}
	
	public String getFindText() {
		return findField.getText();
	}
	
	public String getReplaceText() {
		return replaceField.getText();
	}
	
	public void setOnFindAction(EventHandler<ActionEvent> value) {
		findButton.setOnAction(value);
	}
	
	public void setOnReplaceAction(EventHandler<ActionEvent> value) {
		replaceButton.setOnAction(value);
	}
	
	public void setOnFindReplaceAction(EventHandler<ActionEvent> value) {
		replaceFindButton.setOnAction(value);
	}
	
	public void setSearchCapabilities(SearchCapabilities capabilities) {
		dirStackPane.setDisable(!capabilities.supportsSearchDirection());
		caseSensitiveCheckbox.setDisable(!capabilities.supportsCaseMatching());
		wrapCheckbox.setDisable(!capabilities.supportsWrapping());
		findButton.setDisable(!capabilities.supportsFind());
		findField.setDisable(!capabilities.supportsFind());
		replaceButton.setDisable(!capabilities.supportsReplace());
		replaceFindButton.setDisable(!capabilities.supportsReplace());
		replaceField.setDisable(!capabilities.supportsReplace());
	}
	
	public SearchOptions getSearchOptions() {
		return new SearchOptions.Builder()
				.matchCase(caseSensitiveCheckbox.isSelected())
				.wrapAround(wrapCheckbox.isSelected())
				.reverseSearch((Boolean)directionToggleGroup.getSelectedToggle().getUserData())
				.build();
	}

}
