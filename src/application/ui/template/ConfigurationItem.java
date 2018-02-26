package application.ui.template;

import application.l10n.Messages;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Provides a configuration item.
 * @author Joel Håkansson
 *
 */
public class ConfigurationItem extends BorderPane {
	private Label key;
	private Button apply;
	private Label description;

	/**
	 * Creates a new configuration item with the supplied details.
	 * @param name the name of the configuration
	 * @param desc the description
	 * @param action the action to perform when pressing the button
	 */
	public ConfigurationItem(String name, String desc, EventHandler<ActionEvent> action) {
		key = new Label(name);
		setLeft(key);
		apply = new Button(Messages.BUTTON_SELECT.localize());
		if (action!=null) {
			apply.setOnAction(action);
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
