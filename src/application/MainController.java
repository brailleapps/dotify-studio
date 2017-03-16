package application;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.daisy.braille.pef.PEFBook;
import org.daisy.braille.pef.PEFBookLoader;
import org.daisy.braille.pef.PEFLibrary;
import org.daisy.braille.pef.PEFSearchIndex;
import org.xml.sax.SAXException;

import com.googlecode.e2u.Settings;

import application.about.AboutView;
import application.l10n.Messages;
import application.prefs.PreferencesView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {
	@FXML private BorderPane root;
	@FXML private TabPane tabPane;
	@FXML private Button expandButton;
	@FXML private Button folderButton;
	@FXML private TextField searchFor;
	@FXML private ListView<PefBookAdapter> listView;
	@FXML private SplitPane splitPane;
	@FXML private ProgressBar searchProgress;
	BookScanner bookScanner;
	private final double dividerPosition = 0.2;
	ExecutorService exeService;

	@FXML
	public void initialize() {
		exeService = Executors.newWorkStealingPool();
		startScanning();
		splitPane.setDividerPosition(0, dividerPosition);
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

	void openArgs(String[] args) {
        if (args.length>0) {
        	addTab(null, args);
        }
	}
/*
    private MenuBar makeMenu(Window stage) {
    	MenuBar menuBar = new MenuBar();
		Menu menuFile = new Menu("File");
		MenuItem open = new MenuItem("Open...");
		//open.setAccelerator(KeyCombination.keyCombination("shortcut+O"));
		//open.setOnAction(e->showOpenDialog(stage));
		MenuItem saveAs = new MenuItem("Save...");
		MenuItem importItem = new MenuItem("Import...");
		menuFile.getItems().addAll(open, saveAs, importItem);
		Menu menuEdit = new Menu("Edit");
		MenuItem refresh = new MenuItem("Refresh");
		refresh.setAccelerator(KeyCombination.keyCombination("F5"));
			refresh.setOnAction(e-> {refresh();
		});

		menuEdit.getItems().addAll(refresh);
		Menu menuView = new Menu("View");
		Menu menuWindow = new Menu("Window");
		MenuItem preferences = new MenuItem(Messages.PREFERENCES_MENU_ITEM.localize());
		preferences.setOnAction(e -> {
			openPreferences();
		});
		menuWindow.getItems().addAll(preferences);
		Menu menuHelp = new Menu("Help");
		MenuItem about = new MenuItem("About");
		about.setOnAction(e -> {
			openAbout();
		});
		menuHelp.getItems().addAll(about);
		menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuWindow, menuHelp);
		return menuBar;
    }
  */  
    @FXML
    public void refresh() {
		Tab t = tabPane.getSelectionModel().getSelectedItem();
		if (t!=null) {
			((EmbosserBrowser)t.getContent()).reload();
		}
    }
    
    @FXML
    public void closeTab() {
    	Tab t = tabPane.getSelectionModel().getSelectedItem();
		if (t!=null) {
			tabPane.getTabs().remove(t);
		}
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
    public void toggleSearchArea() {
    	ObservableList<Divider> dividers = splitPane.getDividers();
    	//TODO: observe changes and restore to that value
    	if (dividers.get(0).getPosition()>dividerPosition/2) {
    		splitPane.setDividerPosition(0, 0);
    		expandButton.setText(">");
    		listView.setVisible(false);
    		searchFor.setVisible(false);
    	} else {
    		expandButton.setText("<");
    		splitPane.setDividerPosition(0, dividerPosition);
    		listView.setVisible(true);
    		searchFor.setVisible(true);
    	}
    }
    
    @FXML
    public void openPreferences() {
		PreferencesView dialog = new PreferencesView();
		dialog.initOwner(root.getScene().getWindow());
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.showAndWait();
    }
    
    @FXML
    public void openAbout() {
		AboutView dialog = new AboutView();
		dialog.initOwner(root.getScene().getWindow());
		dialog.initModality(Modality.APPLICATION_MODAL); 
		dialog.showAndWait();
    }
    
    @FXML
    public void showOpenDialog() {
    	Window stage = root.getScene().getWindow();
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Resource File");
    	fileChooser.getExtensionFilters().add(new ExtensionFilter("PEF-files", "*.pef"));
    	File selected = fileChooser.showOpenDialog(stage);
    	if (selected!=null) {
    		addTab(selected);
    	}
    }
    
    @FXML
    public void showOpenFolder() {
    	Window stage = root.getScene().getWindow();
    	DirectoryChooser chooser = new DirectoryChooser();
    	chooser.setTitle("Set search folder");
    	chooser.setInitialDirectory(Settings.getSettings().getLibraryPath());
    	File selected = chooser.showDialog(stage);
    	if (selected!=null) {
    		Settings.getSettings().setLibraryPath(selected.getAbsolutePath());
    		startScanning();
    	}
    }
    
    @FXML
    public void closeApplication() {
    	((Stage)root.getScene().getWindow()).close();
    }
    
    private void addTab(File f) { 
    	addTab(f.getName(), new String[]{"-open", f.getAbsolutePath()});
    }
    
    private void addTab(String title, String[] args) {
        Tab tab = new Tab();
        if (title==null && args.length>=2) {
        	title = args[1];
        }
        if (title!=null) {
        	tab.setText(title);
        }
        tab.setContent(new EmbosserBrowser(args));
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
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
	
	private static class PefBookAdapter {
		private final PEFBook book;
		private final String display;
		
		public PefBookAdapter(PEFBook book) {
			this.book = book;
	    	String untitled = Messages.MESSAGE_UNKNOWN_TITLE.localize();
	    	String unknown = Messages.MESSAGE_UNKNOWN_AUTHOR.localize();
			Iterable<String> title = book.getTitle(); 
			Iterable<String> authors = book.getAuthors();
			this.display = Messages.MESSAGE_SEARCH_RESULT.localize((title==null?untitled:title.iterator().next()), (authors==null?unknown:authors.iterator().next()));
		}
		
		PEFBook getBook() {
			return book;
		}
		
		@Override
		public String toString() {
			return display;
		}
	}

}
