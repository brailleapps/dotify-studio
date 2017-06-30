package application.prefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.api.paper.CustomPaperCollection;
import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.Paper;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import shared.NiceName;
import shared.Tools;

/**
 * Provides a controller for a paper settings view.
 * @author Joel Håkansson
 *
 */
public class PaperSettingsController extends BorderPane {
	private static final Logger logger = Logger.getLogger(PaperSettingsController.class.getCanonicalName());
	private CustomPaperCollection coll;
	private final OptionNiceNames nn = new OptionNiceNames();
	@FXML private ListView<PaperAdapter> list;
	@FXML private TextField nameField;
	@FXML private TextField descriptionField;
	@FXML private TextField field1;
	@FXML private TextField field2;
	@FXML private Label label1;
	@FXML private Label label2;
	@FXML private ComboBox<NiceName> units1;
	@FXML private ComboBox<NiceName> units2;
	@FXML private RadioButton sheetPaper;
	@FXML private RadioButton tractorPaper;
	@FXML private RadioButton rollPaper;
	@FXML private ToggleGroup addPaper;

	/**
	 * Creates a new paper settings controller.
	 */
	public PaperSettingsController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PaperSettings.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
	}

	@FXML void initialize() {
		coll = CustomPaperCollection.getInstance();
		addLengths(units1);
		addLengths(units2);
		sheetPaper.setUserData(new SheetPaperToggle());
		tractorPaper.setUserData(new TractorPaperToggle());
		rollPaper.setUserData(new RollPaperToggle());
		updateList();
		addPaper.selectedToggleProperty().addListener((ov, t1, t2)->{
			((PaperToggle)t2.getUserData()).toggleState();
		});
		list.setOnKeyTyped(ev -> {
			if ("\u007F".equals(ev.getCharacter())) { //DEL
				PaperAdapter pa = list.getSelectionModel().getSelectedItem();
				try {
					hasUpdates = true;
					coll.remove(pa.getPaper());
					list.getItems().remove(pa);
				} catch (IOException e) {
					logger.log(Level.WARNING, "Failed to delete paper.", e);
				}
			} else {
				System.out.println(ev.getCharacter());
			}
		});
	}
	
	private void updateList() {
		list.getItems().clear();
		list.getItems().addAll(wrap(coll.list()));
	}
	
	private static List<PaperAdapter> wrap(Collection<Paper> props) {
		List<PaperAdapter> ad = new ArrayList<>();
		for (Paper p : props) {
			ad.add(new PaperAdapter(p));
		}
		return ad;
	}
	
	interface PaperToggle {
		void toggleState();
		void addPaper();
	}
	
	private abstract class AbstractPaperToggle implements PaperToggle {
		
		void updateToggle(String t1, String t2, boolean visible) {
			label1.setText(t1);
			label2.setText(t2);
			label2.setVisible(visible);
			field2.setVisible(visible);
			units2.setVisible(visible);
		}
		
		boolean validate() {
			if (!"".equals(nameField.getText())) {
				return true;
			} else {
				logger.warning("No name");
				return false;
			}
		}
		
		//TODO: improve error handling (all paper types, see below)
	}
	
	private class SheetPaperToggle extends AbstractPaperToggle {
		
		@Override
		public void toggleState() {
			updateToggle(Messages.LABEL_WIDTH.localize(), Messages.LABEL_HEIGHT.localize(), true);
		}

		@Override
		public void addPaper() {
			if (validate()) {
				Length l1 = Tools.parseLength(field1.getText(), units1.getSelectionModel().getSelectedItem().getKey());
				Length l2 = Tools.parseLength(field2.getText(), units2.getSelectionModel().getSelectedItem().getKey());
				try {
					coll.addNewSheetPaper(nameField.getText(), descriptionField.getText(), l1, l2);
				} catch (IOException e) {
					logger.log(Level.WARNING, "Failed to add paper", e);
				}
			}
		}
	}
	
	private class TractorPaperToggle extends AbstractPaperToggle {

		@Override
		public void toggleState() {
			updateToggle(Messages.LABEL_WIDTH.localize(), Messages.LABEL_HEIGHT.localize(), true);
		}

		@Override
		public void addPaper() {
			if (validate()) {
				Length l1 = Tools.parseLength(field1.getText(), units1.getSelectionModel().getSelectedItem().getKey());
				Length l2 = Tools.parseLength(field2.getText(), units2.getSelectionModel().getSelectedItem().getKey());
				try {
					coll.addNewTractorPaper(nameField.getText(), descriptionField.getText(), l1, l2);
				} catch (IOException e) {
					logger.log(Level.WARNING, "Failed to add paper", e);
				}
			}			
		}

	}
	
	private class RollPaperToggle extends AbstractPaperToggle {
		
		@Override
		public void toggleState() {
			updateToggle(Messages.LABEL_ROLL_SIZE.localize(), "", false);
		}


		@Override
		public void addPaper() {
			if (validate()) {
				Length l1 = Tools.parseLength(field1.getText(), units1.getSelectionModel().getSelectedItem().getKey());
				try {
					coll.addNewRollPaper(nameField.getText(), descriptionField.getText(), l1);
				} catch (IOException e) {
					logger.log(Level.WARNING, "Failed to add paper", e);
				}
			}
		}
		
	}
	
	private void addLengths(ComboBox<NiceName> box) {
		for (NiceName n : nn.getLengthNN()) {
			box.getItems().add(n);
		}
	}
	
	boolean hasUpdates;
	boolean hasUpdates() {
		if (hasUpdates) {
			hasUpdates = false;
			return true;
		} else {
			return false;
		}
	}
	
	@FXML void addPaper() {
		Toggle t = addPaper.getSelectedToggle();
		if (t!=null) {
			((PaperToggle)t.getUserData()).addPaper();
			hasUpdates = true;
			updateList();
		} else {
			logger.warning("No toggle");
		}
	}

}
