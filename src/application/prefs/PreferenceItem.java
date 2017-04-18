package application.prefs;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

class PreferenceItem extends BorderPane {
	private static final NiceName empty = new NiceName("", "");
	private Label key;
	private ChoiceBox<NiceName> choiceValue;
	private TextField stringValue;
	private Label description;
	
	PreferenceItem(String label, Collection<? extends NiceName> values, String def, ChangeListener<? super NiceName> listener) {
		this(label, null, values, def, listener);
	}

	PreferenceItem(String label, String desc, Collection<? extends NiceName> values, String def, ChangeListener<? super NiceName> listener) {
		setKey(label);
		if (values!=null) {
			NiceName selected = setOptions(values, def, listener);
			if (selected!=null) {
				setDescription(selected.getDescription());
			}
		} else {
			setOptions(def);
			setDescription(desc);
		}
	}
	
	void setDescription(String desc) {
		if (desc!=null && !"".equals(desc)) {
			description = newDescriptionLabel(desc);
			setAlignment(description, Pos.CENTER_RIGHT);
			setBottom(description);
		} else {
			setBottom(null);
		}
	}
	
	static Label newDescriptionLabel(String text) {
		Label ret = new Label(text);
		ret.setTextAlignment(TextAlignment.RIGHT);
		ret.setWrapText(true);
		ret.setFont(new Font("System Italic", 12));
		return ret;
	}
	
	void setOptions(String def) {
		if (def!=null) {
			stringValue = new TextField();
			stringValue.setPromptText(def);
			setRight(stringValue);
		}
	}

	NiceName setOptions(Collection<? extends NiceName> nn, String def, ChangeListener<? super NiceName> listener) {
		choiceValue = new ChoiceBox<>();
		choiceValue.setPrefWidth(210);
		choiceValue.setMaxWidth(USE_PREF_SIZE);
		choiceValue.getItems().add(empty);
		NiceName selected = null;
		if ("".equals(def)) {
			selected = empty;
		}
		for (NiceName v : nn) {
			choiceValue.getItems().add(v);
			if (v.getKey().equals(def)) {
				selected = v;
			}
		}
		if (selected!=null) {
			choiceValue.getSelectionModel().select(selected);
		}
		if (listener!=null) {
			choiceValue.valueProperty().addListener(listener);
		}
		setRight(choiceValue);
		return selected;
	}
	
	void setKey(String text) {
		if (text!=null) {
			key = new Label(text);
			key.setFont(Font.font("System", FontWeight.BOLD, 12));
			setLeft(key);
		}
	}

	public String getKey() {
		return key.getText();
	}
	
	public void setValue(String value) {
		if (choiceValue!=null) {
			for (NiceName tva : choiceValue.getItems()) {
				if (value.equals(tva.getKey())) {
					choiceValue.getSelectionModel().select(tva);
					break;
				}
			}
		} else if (stringValue!=null) {
			stringValue.setText(value);
		}
	}
	
	public String getValue() {
		if (choiceValue!=null) {
			return choiceValue.getSelectionModel().getSelectedItem().getKey();
		} else if (stringValue!=null) {
			return stringValue.getText();
		} else {
			throw new RuntimeException("Error in code.");
		}
	}
}
