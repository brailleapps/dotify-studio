package application.prefs;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.table.BrailleConstants;
import org.daisy.braille.consumer.table.TableCatalog;

import com.googlecode.e2u.Settings;
import com.googlecode.e2u.Settings.Keys;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;

public class PreferencesController {

	@FXML private Tab previewTab;
	@FXML private Tab embossTab;

	@FXML
	public void initialize() {
		previewTab.setText(Messages.TAB_PREVIEW.localize());
		embossTab.setText(Messages.TAB_EMBOSS.localize());
	}

}
