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

public class EmbossSettingsController {
	@FXML private Label deviceLabel;
	@FXML private Label embosserLabel;
	@FXML private Label printModeLabel;
	@FXML private Label tableLabel;
	@FXML private Label paperLabel;
	@FXML private Label orentationLabel;
	@FXML private Label zFoldingLabel;
	@FXML private Label alignLabel;
	@FXML private ComboBox<PrintServiceAdapter> deviceSelect;

	@FXML
	public void initialize() {
		// init labels
		deviceLabel.setText(Messages.LABEL_DEVICE.localize());
		embosserLabel.setText(Messages.LABEL_EMBOSSER.localize());
		printModeLabel.setText(Messages.LABEL_PRINT_MODE.localize());
		tableLabel.setText(Messages.LABEL_TABLE.localize());
		paperLabel.setText(Messages.LABEL_PAPER.localize());
		orentationLabel.setText(Messages.LABEL_ORIENTATION.localize());
		zFoldingLabel.setText(Messages.LABEL_Z_FOLDING.localize());
		alignLabel.setText(Messages.LABEL_ALIGNMENT.localize());
		DeviceScanner deviceScanner = new DeviceScanner();
		deviceScanner.setOnSucceeded(ev -> {
			deviceSelect.getItems().addAll(deviceScanner.getValue());
			if (deviceScanner.currentValue!=null) {
				deviceSelect.valueProperty().setValue(deviceScanner.currentValue);
			}
			deviceSelect.valueProperty().addListener((ov, t0, t1)-> { 
				Settings.getSettings().put(Keys.device, t1.p.getName());
			});
		});
		newThread(deviceScanner);
	}
	
	private void newThread(Runnable r) {
		Thread th = new Thread(r);
		th.setDaemon(true);
		th.start();
	}
	
	private static class DeviceScanner extends Task<List<PrintServiceAdapter>> {
		private final String currentDevice = Settings.getSettings().getString(Keys.device, "");
		private PrintServiceAdapter currentValue; 
		@Override
		protected List<PrintServiceAdapter> call() throws Exception {
			PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
			List<PrintServiceAdapter> ret = new ArrayList<>();
			for (PrintService p : printers) {
				PrintServiceAdapter ap = new PrintServiceAdapter(p);
				ret.add(ap);
				if (p.getName().equals(currentDevice)) {
					currentValue = ap;
				}
			}
			return ret;
		}
	}
	
	private static class PrintServiceAdapter {
		private final PrintService p;
		private PrintServiceAdapter(PrintService p) {
			this.p = p;
		}

		@Override
		public String toString() {
			return p.getName();
		}
	}	

}
