package application;

import java.io.File;

import application.about.AboutView;
import application.l10n.Messages;
import application.prefs.PreferencesView;
import application.search.SearchController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {
	@FXML private BorderPane root;
	@FXML private TabPane tabPane;
	@FXML private TabPane toolsPane;
	@FXML private SplitPane splitPane;
	private final double dividerPosition = 0.2;
	private Tab searchTab;


	@FXML
	public void initialize() {
		splitPane.setDividerPosition(0, dividerPosition);
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
    public void toggleSearchArea() {
    	ObservableList<Divider> dividers = splitPane.getDividers();
    	//TODO: observe changes and restore to that value
    	if (dividers.get(0).getPosition()>dividerPosition/2) {
    		splitPane.setDividerPosition(0, 0);
    		/*
    		expandButton.setText(">");
    		listView.setVisible(false);
    		searchFor.setVisible(false);*/
    	} else {
    		//expandButton.setText("<");
    		splitPane.setDividerPosition(0, dividerPosition);
    		/*
    		listView.setVisible(true);
    		searchFor.setVisible(true);*/
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
    public void showSearch() {
    	if (searchTab==null || !toolsPane.getTabs().contains(searchTab)) {
    		SearchController controller = new SearchController();
    		controller.addEventHandler(ActionEvent.ACTION, ev -> {
    			addTab(new File(controller.getSelectedItem().getBook().getURI()));
    		});
    		searchTab = addTabToTools(controller, Messages.TAB_SEARCH.localize());
    	} else {
    		//focus
    		toolsPane.getSelectionModel().select(searchTab);
    		if (splitPane.getDividerPositions()[0]<dividerPosition/3) {
    			splitPane.setDividerPosition(0, dividerPosition);
    		}
    	}
    }
    
    private Tab addTabToTools(Parent component, String title) {
        Tab tab = new Tab();
        if (title!=null) {
        	tab.setText(title);
        }
        tab.setContent(component);
        tab.setOnClosed(ev -> {
        	if (toolsPane.getTabs().size()==0) {
        		splitPane.setDividerPosition(0, 0);
        	}
        });
        if (toolsPane.getTabs().size()==0) {
    		splitPane.setDividerPosition(0, dividerPosition);
    	}
        toolsPane.getTabs().add(tab);
        toolsPane.getSelectionModel().select(tab);
        return tab;
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

}
