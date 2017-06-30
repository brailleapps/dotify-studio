package application.prefs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.pef.PEFGenerator;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import shared.Configuration;
import shared.FactoryPropertiesAdapter;
import shared.NiceName;
import shared.Settings;
import shared.Settings.Keys;

/**
 * Provides a controller for the embosser settings view.
 * @author Joel Håkansson
 *
 */
public class EmbossSettingsController extends BorderPane {
	private static final Logger logger = Logger.getLogger(EmbossSettingsController.class.getCanonicalName());
	@FXML private Label deviceLabel;
	@FXML private Label embosserLabel;
	@FXML private Label embosserDetailsLabel;
	@FXML private Label printModeLabel;
	@FXML private Label tableLabel;
	@FXML private Label tableDetailsLabel;
	@FXML private Label paperLabel;
	@FXML private Label paperDetailsLabel;
	@FXML private Label orentationLabel;
	@FXML private Label zFoldingLabel;
	@FXML private Label alignLabel;
	@FXML private Button testButton;
	@FXML private ComboBox<PrintServiceAdapter> deviceSelect;
	@FXML private ComboBox<FactoryPropertiesAdapter> embosserSelect;
	@FXML private ComboBox<NiceName> printModeSelect;
	@FXML private ComboBox<FactoryPropertiesAdapter> tableSelect;
	@FXML private ComboBox<FactoryPropertiesAdapter> paperSelect;
	@FXML private ComboBox<NiceName> orentationSelect;
	@FXML private ComboBox<NiceName> zFoldingSelect;
	@FXML private ComboBox<NiceName> alignSelect;
	@FXML private VBox parent;
	private PreferenceItem deviceItem;
	private PreferenceItem embosserItem;
	private PreferenceItem printModeItem;
	private PreferenceItem tableItem;
	private PreferenceItem paperItem;
	private PreferenceItem orientationItem;
	private PreferenceItem zFoldingItem;
	private PreferenceItem alignItem;
	private ExecutorService exeService;
	private final OptionNiceNames nn = new OptionNiceNames();
	private DeviceScanner deviceScanner;
	private File generatedFile;

	/**
	 * Creates a new embosser settings controller.
	 */
	public EmbossSettingsController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EmbossSettings.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
	}

	@FXML void initialize() {
		exeService = Executors.newSingleThreadExecutor();
		deviceScanner = new DeviceScanner();
		deviceScanner.setOnSucceeded(ev -> {
			updateComponents();
		});
		exeService.submit(deviceScanner);
		exeService.shutdown();
	}
	
	void updateComponents() {
		Task<Configuration> readConfig = new Task<Configuration>() {
			@Override
			protected Configuration call() throws Exception {
				return Configuration.getConfiguration(Settings.getSettings());
			}
		};
		readConfig.setOnSucceeded(ev -> {
			Platform.runLater(()-> {
				updateComponentsInner(readConfig.getValue());
			});
		});
		Thread th = new Thread(readConfig);
		th.setDaemon(true);
		th.start();
	}

	private void updateComponentsInner(Configuration conf) {
		Config config = new Config();
		config.update();
		parent.getChildren().clear();
		
		deviceItem = new PreferenceItem(Messages.LABEL_DEVICE.localize(), deviceScanner.getValue(), config.device, (o, t0, t1) -> {
			Settings.getSettings().put(Keys.device, t1.getKey());
			updateComponents();
		});
		parent.getChildren().add(deviceItem);

		if (!"".equals(config.device)) {
			embosserItem = new PreferenceItem(Messages.LABEL_EMBOSSER.localize(), wrap(conf.getEmbossers()), config.embosser, (o, t0, t1) -> {
				Settings.getSettings().put(Keys.embosser, t1.getKey());
				updateComponents();
			});
			parent.getChildren().add(embosserItem);
			
			if (!"".equals(config.embosser)) {
				if (conf.supportsBothPrintModes()) {
					printModeItem = new PreferenceItem(Messages.LABEL_PRINT_MODE.localize(), nn.getPrintModeNN(), config.printMode, (o, t0, t1) -> {
						Settings.getSettings().put(Keys.printMode, t1.getKey());
						updateComponents();
					});
					parent.getChildren().add(printModeItem);
				} else {
					printModeItem = null;
				}
		
				if (conf.getSupportedTables().size()>1) {
					tableItem = new PreferenceItem(Messages.LABEL_TABLE.localize(), wrap(conf.getSupportedTables()), config.table, (o, t0, t1) -> {
						Settings.getSettings().put(Keys.table, t1.getKey());
						updateComponents();
					});
					parent.getChildren().add(tableItem);
				} else {
					tableItem = null;
				}
				
				paperItem = new PreferenceItem(Messages.LABEL_PAPER.localize(), wrap(conf.getSupportedPapers()), config.paper, (o, t0, t1) -> {
					Settings.getSettings().put(Keys.paper, t1.getKey());
					updateComponents();
				});
				parent.getChildren().add(paperItem);
	
				if (conf.isRollPaper()) {
					parent.getChildren().add(new PreferenceItem(Messages.LABEL_CUT_LENGTH.localize(), nn.getLengthNN(), config.lengthValue, config.lengthUnit, (f1, f2)->{
						Settings.getSettings().put(Keys.cutLengthValue, f1);
						Settings.getSettings().put(Keys.cutLengthUnit, f2);
						updateComponents();
					}));
				}
				
				if (conf.supportsOrientation()) {
					orientationItem = new PreferenceItem(Messages.LABEL_ORIENTATION.localize(), nn.getOrientationNN(), config.orientation, (o, t0, t1) -> {
								Settings.getSettings().put(Keys.orientation, t1.getKey());
								updateComponents();
							});
					parent.getChildren().add(orientationItem);
				} else {
					orientationItem = null;
				}

    			if (conf.settingOK()) {
    				// this is a way to add a second description which isn't dependent on any of the above
    				parent.getChildren().add(
    						new PreferenceItem(null, Messages.LABEL_PAPER_DIMENSIONS.localize( 
    								conf.getShape()==null?"":nn.getShapeNN().get(conf.getShape().name()),
    										conf.getPaperWidth(), conf.getPaperHeight(), conf.getMaxWidth(), conf.getMaxHeight()), null, null, null)
    						
    				);
    			}
				
				if (conf.supportsZFolding()) {
					zFoldingItem = new PreferenceItem(Messages.LABEL_Z_FOLDING.localize(), nn.getZfoldingNN(), config.zFolding, (o, t0, t1) -> {
						Settings.getSettings().put(Keys.zFolding, t1.getKey());
						updateComponents();
					});
					parent.getChildren().add(zFoldingItem);
				} else {
					zFoldingItem = null;
				}
				if (conf.supportsAligning()) {
					alignItem = new PreferenceItem(Messages.LABEL_ALIGNMENT.localize(), nn.getAlignNN(), config.align, (o, t0, t1) -> {
						Settings.getSettings().put(Keys.align, t1.getKey());
						updateComponents();
					});
					parent.getChildren().add(alignItem);
				} else {
					alignItem = null;
				}
			}
		}
		
		if (conf.settingOK()) {
			testButton.setDisable(false);
			testButton.setText(Messages.LABEL_CREATE_TEST_DOCUMENT.localize());
			testButton.setOnAction(ev->{
				if (conf.settingOK() && generatedFile==null) {
					Map<String,String> keys = new HashMap<>();
			        keys.put(PEFGenerator.KEY_COLS, String.valueOf(conf.getMaxWidth()));
			        keys.put(PEFGenerator.KEY_ROWS, String.valueOf(conf.getMaxHeight()));
			        keys.put(PEFGenerator.KEY_DUPLEX, String.valueOf(true));
			        keys.put(PEFGenerator.KEY_EIGHT_DOT, String.valueOf(false));
			        PEFGenerator generator = new PEFGenerator(keys);
			        try {
						File file = File.createTempFile("generated-", ".pef");
						file.deleteOnExit();
			            generator.generateTestPages(file);
			            generatedFile = file;
			            ((Stage)testButton.getScene().getWindow()).close();
			        } catch (Exception e) {
			        	logger.log(Level.WARNING, "Failed to generate document.", e);
			        }
				}
			});
		} else {
			testButton.setDisable(true);
			testButton.setText(Messages.LABEL_SETUP_INVALID.localize());
		}
	}

	File generatedTestFile() {
		return generatedFile;
	}
	
	private static List<FactoryPropertiesAdapter> wrap(Collection<? extends FactoryProperties> props) {
		return props.stream().sorted((o1, o2)->o1.getDisplayName().compareTo(o2.getDisplayName())).map(p->new FactoryPropertiesAdapter(p)).collect(Collectors.toList());
	}

	private static class DeviceScanner extends Task<List<PrintServiceAdapter>> {
		@Override
		protected List<PrintServiceAdapter> call() throws Exception {
			PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
			List<PrintServiceAdapter> ret = new ArrayList<>();
			for (PrintService p : printers) {
				ret.add(new PrintServiceAdapter(p));
			}
			return ret;
		}
	}
	
	private class Config {
		String device;
		String embosser;
		String printMode;
		String paper;
		String lengthValue;
		String lengthUnit;
		String align;
		String table;
		String orientation;
		String zFolding;
		private void update() {
			Settings settings = Settings.getSettings();
	    	device = settings.getString(Keys.device, "");
	    	embosser = settings.getString(Keys.embosser, "");
	    	printMode = settings.getString(Keys.printMode, "");
	    	paper = settings.getString(Keys.paper, "");
	    	lengthValue = settings.getString(Keys.cutLengthValue, "");
	    	lengthUnit = settings.getString(Keys.cutLengthUnit, "");
	    	align = settings.getString(Keys.align, "");
	    	table = settings.getString(Keys.table, "");
	    	orientation = settings.getString(Keys.orientation, "DEFAULT");
	    	zFolding = settings.getString(Keys.zFolding, "OFF");
		}
	}
	
	private static class PrintServiceAdapter extends NiceName {
		private PrintServiceAdapter(PrintService p) {
			super(p.getName(), p.getName());
		}
	}	

}
