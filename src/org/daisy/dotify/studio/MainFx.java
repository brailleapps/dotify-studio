package org.daisy.dotify.studio;

import java.io.File;
import java.util.Optional;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
 
public class MainFx extends Application {
	private TabPane tabPane;

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dotify Studio");
        
        VBox root = new VBox();
        Scene scene = new Scene(root, 800, 450);

        tabPane = new TabPane();

        
        String[] args = getParameters().getRaw().toArray(new String[]{});
        if (args.length>0) {
        	addTab(null, args);
        }

        ((VBox)scene.getRoot()).getChildren().addAll(makeMenu(primaryStage), tabPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private MenuBar makeMenu(Stage stage) {
    	MenuBar menuBar = new MenuBar();
		Menu menuFile = new Menu("File");
		MenuItem open = new MenuItem("Open...");
		open.setAccelerator(KeyCombination.keyCombination("CTRL+O"));
		open.setOnAction(e->showOpenDialog(stage));
		MenuItem saveAs = new MenuItem("Save...");
		MenuItem importItem = new MenuItem("Import...");
		menuFile.getItems().addAll(open, saveAs, importItem);
		Menu menuEdit = new Menu("Edit");
		MenuItem refresh = new MenuItem("Refresh");
		refresh.setAccelerator(KeyCombination.keyCombination("F5"));

		refresh.setOnAction(e-> {
			Tab t = tabPane.getSelectionModel().getSelectedItem();
			if (t!=null) {
				((EmbosserBrowser)t.getContent()).reload();
			}
		});

		menuEdit.getItems().addAll(refresh);
		Menu menuView = new Menu("View");
		menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
		return menuBar;
    }
    
    private void showOpenDialog(Stage stage) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Resource File");
    	fileChooser.getExtensionFilters().add(new ExtensionFilter("PEF-files", "*.pef"));
    	File selected = fileChooser.showOpenDialog(stage);
    	if (selected!=null) {
    		addTab(selected);
    	}
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