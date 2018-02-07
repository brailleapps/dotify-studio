package application.ui.template;

import application.l10n.Messages;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Provides a configuration item.
 * @author Joel Håkansson
 *
 */
public class ConfigurationItem extends BorderPane {
	private Label key;
	private Button delete;
	private Button apply;
	private Label description;

	/**
	 * Creates a new configuration item with the supplied details.
	 * @param name the name of the configuration
	 * @param desc the description
	 * @param action the action to perform when pressing the button
	 */
	public ConfigurationItem(String name, String desc, boolean removable) {
		key = new Label(name);
		setLeft(key);
		HBox right = new HBox();
		right.setSpacing(10);
		if (removable) {
			delete = new Button("DEL");
			right.getChildren().add(delete);
		}
		apply = new Button(Messages.BUTTON_SELECT.localize());

		right.getChildren().add(apply);
		setRight(right);
		description = new Label(desc);
		description.setTextAlignment(TextAlignment.RIGHT);
		description.setWrapText(true);
		description.setFont(new Font("System Italic", 12));
		setBottom(description);
		setAlignment(description, Pos.CENTER_RIGHT);
	}
	
	void setApplyAction(EventHandler<ActionEvent> action) {
		if (action!=null) {
			apply.setOnAction(action);
		}
	}
	
	void setRemoveAction(EventHandler<ActionEvent> remove) {
		if (remove!=null) {
			if (delete!=null) {
				delete.setOnAction(remove);
			} else {
				throw new IllegalStateException("Not a removable configuration");
			}
		}
	}
}
