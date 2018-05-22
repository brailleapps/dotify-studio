package application.ui.validation;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.streamline.api.validity.ValidationReport;
import org.daisy.streamline.api.validity.ValidatorMessage;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * Provides a controller for the validation view.
 * @author Joel Håkansson
 *
 */
public class ValidationController extends VBox {
	private static final Logger logger = Logger.getLogger(ValidationController.class.getCanonicalName());
	private static final Image FATAL_IMG = new Image(ValidationController.class.getResource("resource-files/fatal-error.png").toString());
	private static final Image ERROR_IMG = new Image(ValidationController.class.getResource("resource-files/error.png").toString());
	private static final Image WARNING_IMG = new Image(ValidationController.class.getResource("resource-files/warning.png").toString());
	private static final Image INFO_IMG = new Image(ValidationController.class.getResource("resource-files/info.png").toString());
	private static final Image OK_IMG = new Image(ValidationController.class.getResource("resource-files/ok.png").toString());
	@FXML private ListView<ValidatorMessageAdapter> listView;

	/**
	 * Creates a new validation view controller.
	 */
	public ValidationController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Validation.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
	}
	
	@FXML void initialize() {
		listView.setCellFactory(v-> new ListCell<ValidatorMessageAdapter>(){
			private ImageView imageView = new ImageView();
			@Override
			public void updateItem(ValidatorMessageAdapter msg, boolean empty) {
				super.updateItem(msg, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(msg.getText());
					Optional<Image> img = msg.getImage();
					if (img.isPresent()) {
						imageView.setImage(img.get());
						setGraphic(imageView);
					} else {
						setGraphic(null);
					}
				}
			}
		});
	}
	
	private static Optional<Image> getImageForMessage(ValidatorMessage msg) {
		switch (msg.getType()) {
			case ERROR: return Optional.of(ERROR_IMG);
			case FATAL_ERROR: return Optional.of(FATAL_IMG);
			case WARNING: return Optional.of(WARNING_IMG);
			case NOTICE: return Optional.of(INFO_IMG);
			default: return Optional.empty();
		}
	}

	/**
	 * Sets the data.
	 * @param report the validation report
	 * @param action an action to take when an item is selected
	 */
	public void setModel(ValidationReport report, Consumer<ValidatorMessage> action) {
		listView.getItems().clear();
		if (report.getMessages().size()>0) {
			report.getMessages().forEach(v->listView.getItems().add(new ValidatorMessageAdapter(v.toString(), getImageForMessage(v).orElse(null), v)));
		} else {
			// No localization here, since other messages are not localized either.
			listView.getItems().add(
				report.isValid()
					? new ValidatorMessageAdapter(report.getSource() + " is valid.", OK_IMG)
					: new ValidatorMessageAdapter(report.getSource() + " is not valid.", null)
			);
		}
		listView.setOnMouseClicked(ev -> {
			if (ev.getClickCount() == 2) {
				ValidatorMessage msg = getSelectedItem();
				if (msg != null) {
					action.accept(msg);
					ev.consume();
				}
			}
		});
		listView.setOnKeyReleased(ev -> {
			if (ev.getCode() == KeyCode.ENTER) {
				ValidatorMessage msg = getSelectedItem();
				if (msg != null) {
					action.accept(msg);
					ev.consume();
				}
			}
			;
		});
	}
	
	/**
	 * Removes all items.
	 */
	public void clear() {
		listView.getItems().clear();
		listView.setOnMouseClicked(null);
		listView.setOnKeyReleased(null);
	}

	/**
	 * Gets the selected result, or null if no result is selected.
	 * @return returns the selected result, or null
	 */
	public ValidatorMessage getSelectedItem() {
		return Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).flatMap(v->v.getValidatorMessage()).orElse(null);
	}

}
