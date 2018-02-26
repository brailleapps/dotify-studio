package application.ui.prefs;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.daisy.braille.utils.api.table.BrailleConstants;
import org.daisy.braille.utils.api.table.TableCatalog;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import shared.FactoryPropertiesAdapter;
import shared.Settings;
import shared.Settings.Keys;

/**
 * Provides a controller for the general settings view.
 * @author Joel Håkansson
 *
 */
public class GeneralSettingsController {
	@FXML private Label previewTranslation;
	@FXML private Label brailleFont;
	@FXML private Label textFont;
	@FXML private Label previewDescription;
	@FXML private ComboBox<FactoryPropertiesAdapter> selectTable;
	@FXML private ComboBox<FontEntry> selectBrailleFont;
	@FXML private ComboBox<FontEntry> selectTextFont;
	@FXML private ComboBox<String> selectLocale;

	@FXML void initialize() {
		previewDescription.setText("");
		FactoryPropertiesScanner tableScanner = new FactoryPropertiesScanner(()->TableCatalog.newInstance().list(), Keys.charset);
		tableScanner.setOnSucceeded(ev -> {
			selectTable.getItems().addAll(tableScanner.getValue());
			if (tableScanner.getCurrentValue()!=null) {
				selectTable.setValue(tableScanner.getCurrentValue());
				previewDescription.setText(tableScanner.getCurrentValue().getProperties().getDescription());
			}
			selectTable.valueProperty().addListener((ov, t0, t1)-> { 
				Settings.getSettings().put(Keys.charset, t1.getProperties().getIdentifier());
				previewDescription.setText(t1.getProperties().getDescription());
			});
		});
		Thread th1 = new Thread(tableScanner);
		th1.setDaemon(true);
		th1.start();
		
		FontEntry defaultBrailleFont = new FontEntry("", Messages.VALUE_USE_DEFAULT.localize(), true);
		selectBrailleFont.getItems().add(defaultBrailleFont);
		selectBrailleFont.setValue(defaultBrailleFont);
		FontEntry defaultTextFont = new FontEntry("", Messages.VALUE_USE_DEFAULT.localize(), false);
		selectTextFont.getItems().add(defaultTextFont);
		selectTextFont.setValue(defaultTextFont);
		FontScanner fontScanner = new FontScanner();
		fontScanner.setOnSucceeded(t -> {
			selectBrailleFont.valueProperty().addListener((ov, t0, t1)-> Settings.getSettings().put(Keys.brailleFont, t1.key));
			selectTextFont.valueProperty().addListener((ov, t0, t1) -> Settings.getSettings().put(Keys.textFont, t1.key));
		});
		Thread th = new Thread(fontScanner);
		th.setDaemon(true);
		th.start();	
		
		selectLocale.getItems().addAll(toString(Locale.getAvailableLocales()));
		String tag = Settings.getSettings().getString(Keys.locale, Locale.getDefault().toLanguageTag());
		selectLocale.getSelectionModel().select(tag);
		selectLocale.valueProperty().addListener((ov, t0, t1)->Settings.getSettings().put(Keys.locale, t1));
	}
	
	private List<String> toString(Locale[] locales) {
		List<String> ret = new ArrayList<>();
		for (Locale l : locales) {
			ret.add(l.toLanguageTag());
		}
		Collections.sort(ret);
		return ret;
	}
	
	private class FontScanner extends Task<Void> {
		private final String currentBrailleFont = Settings.getSettings().getString(Keys.brailleFont, "");
		private final String currentTextFont = Settings.getSettings().getString(Keys.textFont, "");

		@Override
		protected Void call() throws Exception {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			for (String f : ge.getAvailableFontFamilyNames()) {
				if (isCancelled()) {
					break;
				}
				Font font = Font.decode(f);
				int inx = font.canDisplayUpTo(BrailleConstants.BRAILLE_PATTERNS_256);
				if (inx==-1) {
					process(new FontEntry(f, f, true));
				} else if (inx>=64) {
					process(new FontEntry(f, f + " ("+Messages.MESSAGE_SIX_DOT_ONLY.localize()+")", true));
				} else {
					process(new FontEntry(f, f, false));
				}
			}
			return null;
		}

		private void process(FontEntry f) {
			if (f.brailleFont) {
				Platform.runLater(()-> selectBrailleFont.getItems().add(f));
				if (f.key.equals(currentBrailleFont)) {
					Platform.runLater(()->selectBrailleFont.setValue(f));					
				}
			}
			Platform.runLater(()-> selectTextFont.getItems().add(f));
			if (f.key.equals(currentTextFont)) {
				Platform.runLater(()->selectTextFont.setValue(f));
			}
		}
		
	}

	private static class FontEntry {
		private final String key;
		private final String value;
		private final boolean brailleFont;
		
		private FontEntry(String key, String value, boolean brailleFont) {
			this.key = key;
			this.value = value;
			this.brailleFont = brailleFont;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
}
