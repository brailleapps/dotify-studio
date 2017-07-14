package application.search;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.daisy.braille.pef.PEFBook;
import org.daisy.braille.pef.PEFBookLoader;
import org.daisy.braille.pef.PEFLibrary;
import org.daisy.braille.pef.PEFSearchIndex;
import org.xml.sax.SAXException;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import shared.Settings;

/**
 * Provides a controller for the search view.
 * @author Joel Håkansson
 *
 */
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

	/**
	 * Creates a new search view controller.
	 */
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
	
    /**
     * Updates the list of results.
     */
    @FXML void searchChanged() {
    	Platform.runLater(()-> {
    		PEFSearchIndex index = bookScanner.getValue();
    		if (index!=null) {
	        	listView.getItems().clear();
		    	String search = searchFor.getText();
		    	for (PEFBook p : index.containsAll(search)) {
		    		listView.getItems().add(new PefBookAdapter(p));
		    	}
    		}
    	});
    }
    
    /**
     * Opens a directory chooser dialog. If a directory is selected,
     * the results are updated.
     */
    @FXML void showOpenFolder() {
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
    
    /**
     * Gets the selected result, or null if no result is selected.
     * @return returns the selected result, or null
     */
    public PefBookAdapter getSelectedItem() {
    	return listView.getSelectionModel().getSelectedItem();
    }
    
	/**
	 * Performs an action when a book is selected to be opened.
	 * 
	 * @param action
	 */
	public void setOnOpen(Consumer<PefBookAdapter> action) {
		listView.setOnMouseClicked(ev -> {
			if (ev.getClickCount() == 2) {
				PefBookAdapter book = getSelectedItem();
				if (book != null) {
					action.accept(book);
					ev.consume();
				}
			}
		});
		listView.setOnKeyReleased(ev -> {
			if (ev.getCode() == KeyCode.ENTER) {
				PefBookAdapter book = getSelectedItem();
				if (book != null) {
					action.accept(book);
					ev.consume();
				}
			}
			;
		});
	}
    
    private void configureFolderLabel(String text) {
    	currentFolder.setText(text);
    	currentFolder.setTooltip(new Tooltip(text));
    }
	
	private class BookScanner extends Task<PEFSearchIndex> {
		
		@Override
		protected PEFSearchIndex call() throws Exception {
			PEFSearchIndex search = new PEFSearchIndex(1);
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
			return search;
		}
	}
	
}
