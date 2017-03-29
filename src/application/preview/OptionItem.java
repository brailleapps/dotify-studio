package application.preview;

import org.daisy.dotify.api.tasks.TaskOption;
import org.daisy.dotify.api.tasks.TaskOptionValue;

import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class OptionItem extends BorderPane {
	private Label key;
	private ChoiceBox<TaskOptionValueAdapter> choiceValue;
	private TextField stringValue;
	private Label description;

	public OptionItem(TaskOption o) {
		key = new Label(o.getKey());
		setLeft(key);
		if (o.hasValues()) {
			choiceValue = new ChoiceBox<>();
			TaskOptionValueAdapter selected = null;
			for (TaskOptionValue v : o.getValues()) {
				TaskOptionValueAdapter current = new TaskOptionValueAdapter(v);
				choiceValue.getItems().add(current);
				if (v.getName().equals(o.getDefaultValue())) {
					selected = current;
				}
			}
			if (selected!=null) {
				choiceValue.getSelectionModel().select(selected);
			}
			setRight(choiceValue);
		} else {
			stringValue = new TextField();
			stringValue.setPromptText(o.getDefaultValue());
			setRight(stringValue);
		}
		description = new Label(o.getDescription());
		description.setTextAlignment(TextAlignment.RIGHT);
		description.setWrapText(true);
		description.setFont(new Font("System Italic", 12));
		setBottom(description);
		setAlignment(description, Pos.CENTER_RIGHT);
	}

}
