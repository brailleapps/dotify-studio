package application.ui.tools;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.common.braille.BrailleNotationConverter;

import application.l10n.Messages;
import application.ui.tools.CodePointHelper.Mode;
import application.ui.tools.CodePointHelper.Style;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 * Provides a controller for the search view.
 * @author Joel Håkansson
 *
 */
public class CharacterToolController extends VBox {
	private static final Logger logger = Logger.getLogger(CharacterToolController.class.getCanonicalName());
	@FXML TextField brailleInput;
	@FXML TextField textField;
	@FXML TextArea textArea;
	@FXML ToggleGroup style;
	@FXML ToggleGroup radix;
	@FXML RadioButton xmlButton;
	@FXML RadioButton commaButton;
	@FXML RadioButton namesButton;
	@FXML RadioButton hexButton;
	@FXML RadioButton decimalButton;
	private final BrailleNotationConverter bnc;

	/**
	 * Creates a new search view controller.
	 */
	public CharacterToolController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CharacterTool.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		this.bnc = new BrailleNotationConverter("\\s*,\\s*");
	}
	
	@FXML void initialize() {
		hexButton.setUserData(Mode.HEX);
		decimalButton.setUserData(Mode.DECIMAL);
		xmlButton.setUserData(Style.XML);
		commaButton.setUserData(Style.COMMA);
		namesButton.setUserData(Style.LINE);
		radix.selectedToggleProperty().addListener((o, ov, nv)->{
			String v = CodePointHelper.parse(textArea.getText(), (Mode)ov.getUserData());
			textArea.setText(CodePointHelper.format(v, getSelectedStyle().orElse(Style.COMMA), (Mode)nv.getUserData()));
		});
		style.selectedToggleProperty().addListener((o, ov, nv)->{
			String v = CodePointHelper.parse(textArea.getText(), getSelectedMode());
			textArea.setText(CodePointHelper.format(v, getSelectedStyle().orElse(Style.COMMA), getSelectedMode()));
		});
	}
	
	@FXML void updateBraille() {
		try {
			textField.setText(bnc.parseBrailleNotation(brailleInput.getText()));
		} catch (IllegalArgumentException e) {
			//JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@FXML void updateTextArea() {
		getSelectedStyle().ifPresent(v->
			textArea.setText(CodePointHelper.format(textField.getText(), v, getSelectedMode()))
		);
	}
	
	@FXML void updateTextField() {
		textField.setText(CodePointHelper.parse(textArea.getText(), getSelectedMode()));
	}

	private Optional<Style> getSelectedStyle() {
		return Optional.ofNullable(style.getSelectedToggle())
				.flatMap(v->Optional.ofNullable((Style)v.getUserData()));
	}
	
	private Mode getSelectedMode() {
		return Optional.ofNullable(radix.getSelectedToggle())
				.map(v->(Mode)v.getUserData())
				.orElse(Mode.HEX);
	}
	
}
