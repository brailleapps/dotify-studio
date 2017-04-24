package application.template;

import application.l10n.Messages;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class ConfigurationItem extends BorderPane {
	private Label key;
	private Button apply;
	private Label description;

	public ConfigurationItem(String name, String desc, EventHandler<ActionEvent> value) {
		key = new Label(name);
		setLeft(key);
		apply = new Button(Messages.BUTTON_SELECT.localize());
		if (value!=null) {
			apply.setOnAction(value);
		}
		setRight(apply);
		description = new Label(desc);
		description.setTextAlignment(TextAlignment.RIGHT);
		description.setWrapText(true);
		description.setFont(new Font("System Italic", 12));
		setBottom(description);
		setAlignment(description, Pos.CENTER_RIGHT);
	}
	
}
