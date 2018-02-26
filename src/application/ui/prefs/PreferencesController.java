package application.ui.prefs;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;

/**
 * Provides a controller for preferences.
 * @author Joel Håkansson
 *
 */
public class PreferencesController {

	@FXML private Tab previewTab;
	@FXML private Tab embossTab;
	@FXML private EmbossSettingsController embossSettings;
	@FXML private PaperSettingsController paperSettings;

	@FXML void initialize() {
		embossTab.setOnSelectionChanged(ev -> {
			if (embossTab.isSelected() && paperSettings.hasUpdates()) {
				embossSettings.updateComponents();
			}
		});
	}

	File generatedTestFile() {
		return embossSettings.generatedTestFile();
	}

}
