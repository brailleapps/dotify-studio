package application;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.CompiledTaskSystem;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.api.tasks.TaskSystemFactoryException;
import org.daisy.dotify.consumer.identity.IdentityProvider;
import org.daisy.dotify.consumer.tasks.TaskSystemFactoryMaker;
import org.daisy.dotify.tasks.runner.RunnerResult;
import org.daisy.dotify.tasks.runner.TaskRunner;

import application.about.AboutView;
import application.l10n.Messages;
import application.prefs.PreferencesView;
import application.search.SearchController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {
	private static final Logger logger = Logger.getLogger(MainController.class.getCanonicalName());
	private static final Font CONSOLE_FONT = new Font("monospace", 12);
	private static final int CONSOLE_MESSAGE_LIMIT = 500;
	@FXML private BorderPane root;
	@FXML private TabPane tabPane;
	@FXML private TabPane toolsPane;
	@FXML private SplitPane splitPane;
	@FXML private TextFlow console;
	@FXML private ScrollPane consoleScroll;
	private final double dividerPosition = 0.2;
	private Tab searchTab;
	private ExecutorService exeService;
	static final KeyCombination CTRL_F4 = new KeyCodeCombination(KeyCode.F4, KeyCombination.CONTROL_DOWN);

	@FXML
	public void initialize() {
		toolsPane.addEventHandler(KeyEvent.KEY_RELEASED, ev-> {
			if (CTRL_F4.match(ev)) {
				Tab t = toolsPane.getSelectionModel().getSelectedItem();
				if (t!=null) {
					toolsPane.getTabs().remove(t);
				}
				adjustToolsArea();
				ev.consume();
			}
		});
		tabPane.addEventHandler(KeyEvent.KEY_RELEASED, ev -> {
			if (CTRL_F4.match(ev)) {
				Tab t = tabPane.getSelectionModel().getSelectedItem();
				if (t!=null) {
					tabPane.getTabs().remove(t);
				}
				ev.consume();
			}
		});
		exeService = Executors.newWorkStealingPool();
		System.setOut(new PrintStream(new ConsoleStream(Color.BLACK)));
		System.setErr(new PrintStream(new ConsoleStream(Color.RED)));
		try {
			//this is done to reinitialize the console logger so that it redirects to the new print streams set above
			LogManager.getLogManager().readConfiguration();
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	private class ConsoleStream extends OutputStream {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		boolean hasNewLine;
		private final Color color;
		
		ConsoleStream(Color color) {
			this.color = color;
		}
		
		void write(String s) {
			Platform.runLater(()->{
			     Text text1 = new Text(s);
			     text1.setFill(color);
			     text1.setFont(CONSOLE_FONT);
			     console.getChildren().add(text1);
			     while (console.getChildren().size()>CONSOLE_MESSAGE_LIMIT) {
			    	 console.getChildren().remove(0);
			     }
			     consoleScroll.setVvalue(consoleScroll.getVmax());
			});
		}
		
		@Override
		public void write(int b) throws IOException {
			if (hasNewLine && !isNewLine(b)) {
				hasNewLine=false;
				write(new String(bytes.toByteArray()));
				bytes.reset();
			}
			bytes.write(b);
			if (isNewLine(b)) {
				hasNewLine = true;
			}
		}
		
		private boolean isNewLine(int b) {
			return b==13 || b==10;
		}
		
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
	public void openInBrowser() {
		if (Desktop.isDesktopSupported()) {
			try {
				Tab t = tabPane.getSelectionModel().getSelectedItem();
				if (t!=null) {
					Desktop.getDesktop().browse(new URI(((EmbosserBrowser)t.getContent()).getURL()));
				}				
			} catch (IOException | URISyntaxException e) {
			}
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
        	adjustToolsArea();
        });
        if (toolsPane.getTabs().size()==0) {
    		splitPane.setDividerPosition(0, dividerPosition);
    	}
        toolsPane.getTabs().add(tab);
        toolsPane.getSelectionModel().select(tab);
        return tab;
    }
    
    private void adjustToolsArea() {
    	if (toolsPane.getTabs().size()==0) {
    		splitPane.setDividerPosition(0, 0);
    	}
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
    public void showImportDialog() {
    	Window stage = root.getScene().getWindow();
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(Messages.TITLE_IMPORT_DIALOG.localize());
    	File selected = fileChooser.showOpenDialog(stage);
    	if (selected!=null) {
    		//convert then add tab
    		System.out.println("Selected: " + selected);
    		try {
				File out = File.createTempFile("dotify-studio", ".pef");
				// FIXME: Locale MUST be a setting
	    		DotifyTask dt = new DotifyTask(selected, out, Locale.getDefault().toString().replace('_', '-'), Collections.emptyMap());
	    		dt.setOnSucceeded(ev -> {
	    			addTab(out);
	    		});
	    		dt.setOnFailed(ev->{
	    			logger.log(Level.WARNING, "Import failed.", dt.getException());
		    		Alert alert = new Alert(AlertType.ERROR, dt.getException().toString(), ButtonType.OK);
		    		alert.showAndWait();
	    		});
	    		exeService.submit(dt);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    class DotifyTask extends Task<List<RunnerResult>> {
    	private final File inputFile;
    	private final File outputFile;
    	private final String locale;
    	private final Map<String, Object> params;
    	
    	DotifyTask(File inputFile, File outputFile, String locale, Map<String, Object> params) {
    		this.inputFile = inputFile;
    		this.outputFile = outputFile;
    		this.locale = locale;
    		this.params = new HashMap<>(params);
    	}
    	
    	@Override
    	protected List<RunnerResult> call() throws Exception {
    		AnnotatedFile ai = IdentityProvider.newInstance().identify(inputFile);
    		String inputFormat = getFormatString(ai);
    		TaskSystem ts;
			ts = TaskSystemFactoryMaker.newInstance().newTaskSystem(inputFormat, "pef", locale);
			logger.info("About to run with parameters " + params);
			
			logger.info("Thread: " + Thread.currentThread().getThreadGroup());
			CompiledTaskSystem tl = ts.compile(params);
			TaskRunner.Builder builder = TaskRunner.withName(ts.getName());
			return builder.build().runTasks(ai, outputFile, tl);
    	}

    	//FIXME: Duplicated from Dotify CLI. If this function is needed to run Dotify, find a home for it
    	private String getFormatString(AnnotatedFile f) {
    		if (f.getFormatName()!=null) {
    			return f.getFormatName();
    		} else if (f.getExtension()!=null) {
    			return f.getExtension();
    		} else if (f.getMediaType()!=null) {
    			return f.getMediaType();
    		} else {
    			return null;
    		}
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
