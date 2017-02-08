package org.daisy.dotify.studio;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import com.googlecode.e2u.Start;
 
public class MainFx extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dotify Studio");
        VBox root = new VBox();
        Scene scene = new Scene(root, 800, 450);
        String url = getClass().getResource("resource-files/fail.html").toString();
        try {
			url = Start.run(getParameters().getRaw().toArray(new String[]{}), false);
		} catch (Exception e1) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, "Failed to load server.", e1);;
		}
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        ((VBox)scene.getRoot()).getChildren().addAll(makeMenu(), browser);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private static MenuBar makeMenu() {
    	MenuBar menuBar = new MenuBar();
		Menu menuFile = new Menu("File");
		MenuItem open = new MenuItem("Open...");
		open.setAccelerator(KeyCombination.keyCombination("CTRL+O"));
		open.setOnAction(e->System.out.println("Open"));
		MenuItem saveAs = new MenuItem("Save...");
		MenuItem importItem = new MenuItem("Import...");
		menuFile.getItems().addAll(open, saveAs, importItem);
		Menu menuEdit = new Menu("Edit");
		Menu menuView = new Menu("View");
		menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
		return menuBar;
    }
}