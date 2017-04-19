package application.prefs;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;

public class PreferencesController {

	@FXML private Tab previewTab;
	@FXML private Tab embossTab;
	@FXML private EmbossSettingsController embossSettings;

	@FXML
	public void initialize() {
	}
	
	File generatedTestFile() {
		return embossSettings.generatedTestFile();
	}

}
