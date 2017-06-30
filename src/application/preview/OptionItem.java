package application.preview;

import org.daisy.dotify.api.tasks.TaskOption;
import org.daisy.dotify.api.tasks.TaskOptionValue;

import application.l10n.Messages;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Provides an option item.
 * @author Joel Håkansson
 *
 */
public class OptionItem extends BorderPane {
	private final boolean disabled;
	private Label key;
	private ChoiceBox<TaskOptionValueAdapter> choiceValue;
	private TextField stringValue;
	private Label description;

	/**
	 * Creates a new option item with the supplied parameters.
	 * @param option the task option
	 * @param disabled true if the option should be disabled, false otherwise
	 */
	public OptionItem(TaskOption option, boolean disabled) {
		this.disabled = disabled;
		key = new Label(option.getKey());
		setLeft(key);
		if (disabled) {
			Label above = new Label(Messages.LABEL_EDIT_ABOVE.localize());
			above.setTextFill(Paint.valueOf("#808080"));
			setRight(above);
		} else if (option.hasValues()) {
			choiceValue = new ChoiceBox<>();
			TaskOptionValueAdapter selected = null;
			for (TaskOptionValue v : option.getValues()) {
				TaskOptionValueAdapter current = new TaskOptionValueAdapter(v);
				choiceValue.getItems().add(current);
				if (v.getName().equals(option.getDefaultValue())) {
					selected = current;
				}
			}
			if (selected!=null) {
				choiceValue.getSelectionModel().select(selected);
			}
			setRight(choiceValue);
		} else {
			stringValue = new TextField();
			stringValue.setPromptText(option.getDefaultValue());
			setRight(stringValue);
		}
		description = new Label(option.getDescription());
		description.setTextAlignment(TextAlignment.RIGHT);
		description.setWrapText(true);
		description.setFont(new Font("System Italic", 12));
		setBottom(description);
		setAlignment(description, Pos.CENTER_RIGHT);
	}

	/**
	 * Gets the option key.
	 * @return returns the key
	 */
	public String getKey() {
		return key.getText();
	}
	
	/**
	 * Sets the option value.
	 * @param value the value
	 */
	public void setValue(String value) {
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
