package application.ui.preview;

import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.option.UserOptionValue;

import application.l10n.Messages;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * Provides an option item.
 * @author Joel Håkansson
 *
 */
public class OptionItem extends BorderPane {
	private static final String OPTIONS_CLASS = "option-changed";
	private final boolean disabled;
	private final String originalValue;
	private String key;
	private ChoiceBox<TaskOptionValueAdapter> choiceValue;
	private TextField stringValue;
	private Text description;

	/**
	 * Creates a new option item with the supplied parameters.
	 * @param option the task option
	 * @param disabled true if the option should be disabled, false otherwise
	 * @param value an initial value
	 */
	public OptionItem(UserOption option, boolean disabled, Object value) {
		this.disabled = disabled;
		HBox keyValue = new HBox();
		keyValue.setSpacing(5);
		this.key = option.getKey();
		keyValue.getChildren().add(new Text(option.getDisplayName()));
		Region r = new Region();
		HBox.setHgrow(r, Priority.ALWAYS);
		keyValue.getChildren().add(r);
		if (disabled) {
			Label above = new Label(Messages.LABEL_EDIT_ABOVE.localize());
			above.setTextFill(Paint.valueOf("#808080"));
			keyValue.getChildren().add(above);
		} else if (option.hasValues()) {
			choiceValue = new ChoiceBox<>();
			TaskOptionValueAdapter selected = null;
			for (UserOptionValue v : option.getValues()) {
				TaskOptionValueAdapter current = new TaskOptionValueAdapter(v);
				choiceValue.getItems().add(current);
				if (v.getName().equals(option.getDefaultValue())) {
					selected = current;
				}
			}
			if (selected!=null) {
				choiceValue.getSelectionModel().select(selected);
			}
			choiceValue.getSelectionModel().selectedItemProperty().addListener((v, ov, nv)->{
				updateStyle(nv.getValue().getName());
			});
			keyValue.getChildren().add(choiceValue);
		} else {
			stringValue = new TextField();
			stringValue.setPromptText(option.getDefaultValue());
			stringValue.textProperty().addListener((v, ov, nv)->updateStyle(nv));
			keyValue.getChildren().add(stringValue);
		}
		setCenter(keyValue);
		description = new Text(option.getDescription());
		description.setWrappingWidth(200);
		description.setFont(new Font("System Italic", 12));
		description.setTextAlignment(TextAlignment.RIGHT);
		setBottom(description);
		setAlignment(description, Pos.CENTER_RIGHT);
		if (value!=null) {
			originalValue = value.toString();
			setValue(value.toString(), false);
		} else if (option.hasValues()) {
			originalValue = option.getDefaultValue();
		} else {
			originalValue = "";
		}
		getStyleClass().add("options");
	}

	/**
	 * Gets the option key.
	 * @return returns the key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Sets the option value.
	 * @param value the value
	 */
	public void setValue(String value) {
		setValue(value, true);
	}
	
	private void setValue(String value, boolean highlight) {
		if (choiceValue!=null) {
			for (TaskOptionValueAdapter tva : choiceValue.getItems()) {
				if (value.equals(tva.getValue().getName())) {
					choiceValue.getSelectionModel().select(tva);
					break;
				}
			}
		} else if (stringValue!=null) {
			stringValue.setText(value);
		} else if (!disabled) {
			throw new RuntimeException("Error in code.");
		}
		if (highlight) {
			updateStyle(value);
		} else {
			getStyleClass().remove(OPTIONS_CLASS);
		}
	}
	
	private void updateStyle(String currentValue) {
		if (!disabled && currentValue!=null && !originalValue.equals(currentValue)) {
			getStyleClass().remove(OPTIONS_CLASS);
			getStyleClass().add(OPTIONS_CLASS);
		} else {
			getStyleClass().remove(OPTIONS_CLASS);
		}
	}

	/**
	 * Gets the option value.
	 * @return returns the option value
	 */
	public String getValue() {
		if (choiceValue!=null) {
			return choiceValue.getSelectionModel().getSelectedItem().getValue().getName();
		} else if (stringValue!=null) {
			return stringValue.getText();
		} else if (disabled) {
			return "";
		} else {
			throw new RuntimeException("Error in code.");
		}
	}
}
