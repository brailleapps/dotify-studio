package application.ui.prefs;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.daisy.dotify.api.table.BrailleConstants;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.streamline.api.details.FormatDetailsProvider;
import org.daisy.streamline.api.details.FormatDetailsProviderService;
import org.daisy.streamline.api.tasks.TaskGroupFactoryMaker;

import application.common.FactoryPropertiesAdapter;
import application.common.FeatureSwitch;
import application.common.LocaleEntry;
import application.common.NiceName;
import application.common.Settings;
import application.common.Settings.Keys;
import application.common.SupportedLocales;
import application.l10n.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
	@FXML private CheckBox showTemplateDialogCheckbox;
	@FXML private CheckBox lineNumbersCheckbox;
	@FXML private CheckBox wordWrapCheckbox;
	@FXML private CheckBox autosaveCheckbox;
	@FXML private VBox rootVBox;
	@FXML private HBox hboxOutputFormat;
	@FXML private HBox hboxAutosave;
	@FXML private ComboBox<NiceName> selectOutputFormat;
	@FXML private ComboBox<LocaleEntry> selectLocale;

	@FXML void initialize() {
		if (FeatureSwitch.SELECT_OUTPUT_FORMAT.isOn()) {
			FormatDetailsProviderService detailsProvider = FormatDetailsProvider.newInstance();
			List<NiceName> nn = TaskGroupFactoryMaker.newInstance().listAll().stream()
					.map(v->detailsProvider.getDetails(v.getOutputType()))
					.filter(v->v.isPresent())
					.map(v->v.get())
					.distinct()
					.map(v->new NiceName(
							v.getIdentifier().getIdentifier(), 
							v.getDisplayName()))
					.collect(Collectors.toList());
			selectOutputFormat.getItems().addAll(nn);
			String current = Settings.getSettings().getConvertTargetFormatName();
			nn.stream()
				.filter(v->v.getKey().equals(current))
				.findFirst()
				.ifPresent(v->selectOutputFormat.getSelectionModel().select(v));
			selectOutputFormat.valueProperty().addListener((ov, t0, t1)->Settings.getSettings().setConvertTargetFormatName(t1.getKey()));
		} else {
			rootVBox.getChildren().remove(hboxOutputFormat);
		}
		wordWrapCheckbox.setSelected(Settings.getSettings().shouldWrapLines());
		wordWrapCheckbox.selectedProperty().addListener((o, ov, nv)->{
			Settings.getSettings().setWordWrap(nv.booleanValue());
		});
		
		lineNumbersCheckbox.setSelected(Settings.getSettings().shouldShowLineNumbers());
		lineNumbersCheckbox.selectedProperty().addListener((o, ov, nv)->{
			Settings.getSettings().setLineNumbers(nv.booleanValue());
		});
		
		if (FeatureSwitch.AUTOSAVE.isOn()) {
			autosaveCheckbox.setSelected(Settings.getSettings().shouldAutoSave());
			autosaveCheckbox.selectedProperty().addListener((o, ov, nv)->{
				Settings.getSettings().setAutoSave(nv.booleanValue());
			});
		} else {
			rootVBox.getChildren().remove(hboxAutosave);
		}
		
		showTemplateDialogCheckbox.setSelected(Settings.getSettings().getShowTemplateDialogOnImport());
		showTemplateDialogCheckbox.selectedProperty().addListener((o, ov, nv)->{
			Settings.getSettings().setShowTemplateDialogOnImport(nv.booleanValue());
		});

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
		
		List<LocaleEntry> locales = SupportedLocales.list()
			.stream()
			.map(LocaleEntry::new)
			.sorted()
			.collect(Collectors.toList());
		selectLocale.getItems().addAll(locales);
		String tag = Settings.getSettings().getString(Keys.locale, Locale.getDefault().toLanguageTag());
		locales.stream()
			.filter(v->v.getKey().equals(tag))
			.findFirst()
			.ifPresent(v->selectLocale.getSelectionModel().select(v));
		selectLocale.valueProperty().addListener((ov, t0, t1)->Settings.getSettings().put(Keys.locale, t1.getKey()));
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
