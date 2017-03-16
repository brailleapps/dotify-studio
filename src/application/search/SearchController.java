package application.search;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.daisy.braille.pef.PEFBook;
import org.daisy.braille.pef.PEFBookLoader;
import org.daisy.braille.pef.PEFLibrary;
import org.daisy.braille.pef.PEFSearchIndex;
import org.xml.sax.SAXException;

import com.googlecode.e2u.Settings;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class SearchController extends VBox {
	private static final Logger logger = Logger.getLogger(SearchController.class.getCanonicalName());
	@FXML private Button expandButton;
	@FXML private Button folderButton;
	@FXML private TextField searchFor;
	@FXML private Label currentFolder;
	@FXML private ListView<PefBookAdapter> listView;
	@FXML private ProgressBar searchProgress;
	BookScanner bookScanner;
	ExecutorService exeService;

	public SearchController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Search.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		configureFolderLabel(Settings.getSettings().getLibraryPath().getAbsolutePath());
		exeService = Executors.newWorkStealingPool();
		startScanning();
	}
	
	private void startScanning() {
		if (bookScanner!=null) {
			bookScanner.cancel();
		}
		bookScanner = new BookScanner();
		searchProgress.progressProperty().bind(bookScanner.progressProperty());
		bookScanner.setOnSucceeded(ev -> {
			if (!"".equals(searchFor.getText())) {
				searchChanged();
			}
		});
		exeService.submit(bookScanner);
	}
	
    @FXML
    public void searchChanged() {
    	Platform.runLater(()-> {
        	listView.getItems().clear();
	    	String search = searchFor.getText();
	    	for (PEFBook p : bookScanner.search.containsAll(search)) {
	    		listView.getItems().add(new PefBookAdapter(p));
	    	}
    	});
    }
    
    @FXML
    public void showOpenFolder() {
    	Window stage = folderButton.getScene().getWindow();
    	DirectoryChooser chooser = new DirectoryChooser();
    	chooser.setTitle(Messages.TITLE_SET_SEARCH_FOLDER.localize());
    	chooser.setInitialDirectory(Settings.getSettings().getLibraryPath());
    	File selected = chooser.showDialog(stage);
    	if (selected!=null) {
    		Settings.getSettings().setLibraryPath(selected.getAbsolutePath());
    		configureFolderLabel(Settings.getSettings().getLibraryPath().getAbsolutePath());
    		startScanning();
    	}
    }
    
    @FXML void openBook(KeyEvent event) {
    	if (event.getCode()==KeyCode.ENTER) {
    		fireEvent(new ActionEvent());
    	}
    }
    
    @FXML void openBookMouse(MouseEvent event) {
    	if (event.getClickCount()>1) {
    		fireEvent(new ActionEvent());
    	}
    }
    
    public PefBookAdapter getSelectedItem() {
    	return listView.getSelectionModel().getSelectedItem();
    }
    
    private void configureFolderLabel(String text) {
    	currentFolder.setText(text);
    	currentFolder.setTooltip(new Tooltip(text));
    }
	
	private class BookScanner extends Task<PEFSearchIndex> {
		PEFSearchIndex search = new PEFSearchIndex();

		@Override
		protected PEFSearchIndex call() throws Exception {
			PEFBookLoader loader = new PEFBookLoader();
			Collection<File> files = PEFLibrary.listFiles(Settings.getSettings().getLibraryPath(), true);
			int i = 0;
			updateProgress(i, files.size());
			for (File f : files) {
				if (isCancelled()) {
					break;
				}
				try {
					search.add(loader.load(f));
				} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
					//e.printStackTrace();
				}
				i++;
				updateProgress(i, files.size());
			}
			return null;
		}		
	}
	
}
