package application.ui.imports;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.TableCatalog;

import application.common.FactoryPropertiesAdapter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Provides a controller for the dialog that imports formatted braille.
 * @author Joel Håkansson
 *
 */
public class ImportBrailleController {
	@FXML private Label filePath;
	@FXML private Button ok;
	@FXML private Button cancel;
	@FXML private ComboBox<FactoryPropertiesAdapter> tables;
	@FXML private TextField identifier;
	@FXML private TextField date;
	@FXML private TextField author;
	@FXML private TextField title;
	@FXML private TextField language;
	@FXML private CheckBox duplex;
	private boolean cancelled = true;

	/**
	 * Initializes the controller.
	 */
	@FXML
	public void initialize() {
		Task<Collection<FactoryPropertiesAdapter>> readConfig = new Task<Collection<FactoryPropertiesAdapter>>() {
			@Override
			protected Collection<FactoryPropertiesAdapter> call() throws Exception {
				TableCatalog tc = TableCatalog.newInstance();
				Collection<FactoryPropertiesAdapter> ret = new ArrayList<>();
				for (FactoryProperties fp : tc.list()) {
					ret.add(new FactoryPropertiesAdapter(fp));
				}
				return ret.stream().sorted().collect(Collectors.toList());
			}
		};
		readConfig.setOnSucceeded(ev -> {
			Platform.runLater(()-> {
				tables.getItems().addAll(readConfig.getValue());
			});
		});
		Thread th = new Thread(readConfig);
		th.setDaemon(true);
		th.start();
	}
	
	/**
	 * Closes the dialog.
	 */
	@FXML
	public void closeWindow() {
		((Stage)cancel.getScene().getWindow()).close();
	}
	
	/**
	 * Sets the state of the dialog to perform import.
	 */
	@FXML
	public void doImport() {
		cancelled = false;
		((Stage)ok.getScene().getWindow()).close();
	}
	
	void setFile(File f) {
		filePath.setText(f.getAbsolutePath());
	}
	
	Map<String, String> getOptions() {
		if (cancelled) {
			return null;
		}
		Map<String, String> options = new HashMap<>();
		if (tables.getSelectionModel().getSelectedItem()!=null) {
			options.put("mode", tables.getSelectionModel().getSelectedItem().getKey());
		}
		if (!"".equals(identifier.getText())) {
			options.put("identifier", identifier.getText());
		}
		if (!"".equals(author.getText())) {
			options.put("author", author.getText());
		}
		if (!"".equals(title.getText())) {
			options.put("title", title.getText());
		}
		if (!"".equals(language.getText())) {
			options.put("language", language.getText());
		}
		if (!"".equals(date.getText())) {
			options.put("date", date.getText());
		}
		options.put("duplex", duplex.isSelected()+"");
		return options;
	}
	
}
