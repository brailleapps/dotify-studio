package application;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.daisy.braille.api.embosser.Embosser;
import org.daisy.braille.api.embosser.EmbosserCatalogService;
import org.daisy.braille.api.embosser.EmbosserFeatures;
import org.daisy.braille.api.embosser.EmbosserWriter;
import org.daisy.braille.consumer.embosser.EmbosserCatalog;
import org.daisy.braille.consumer.table.TableCatalog;
import org.daisy.braille.pef.PEFBook;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.pef.TextConverterFacade;
import org.daisy.dotify.consumer.tasks.TaskGroupFactoryMaker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.googlecode.e2u.BookReader;

import application.about.AboutView;
import application.emboss.EmbossView;
import application.imports.ImportBrailleView;
import application.l10n.Messages;
import application.prefs.PreferencesView;
import application.preview.PreviewController;
import application.search.PefBookAdapter;
import application.search.SearchController;
import application.template.TemplateView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
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
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import shared.Settings;
import shared.Settings.Keys;

public class MainController {
	private static final Logger logger = Logger.getLogger(MainController.class.getCanonicalName());
	private static final Font CONSOLE_FONT = new Font("monospace", 12);
	private static final int CONSOLE_MESSAGE_LIMIT = 500;
	@FXML private BorderPane root;
	@FXML private TabPane tabPane;
	@FXML private TabPane toolsPane;
	@FXML private SplitPane splitPane;
	@FXML private WebView console;
	@FXML private ScrollPane consoleScroll;
	@FXML private MenuItem closeMenuItem;
	@FXML private MenuItem exportMenuItem;
	@FXML private MenuItem saveAsMenuItem;
	@FXML private MenuItem refreshMenuItem;
	@FXML private MenuItem openInBrowserMenuItem;
	@FXML private MenuItem embossMenuItem;
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
		console.getEngine().loadContent("<html><body></body></html>");
		console.getEngine().setUserStyleSheetLocation(this.getClass().getResource("resource-files/console.css").toString());
		
		exeService = Executors.newWorkStealingPool();
		System.setOut(new PrintStream(new ConsoleStream("out")));
		System.setErr(new PrintStream(new ConsoleStream("err")));
		try {
			//this is done to reinitialize the console logger so that it redirects to the new print streams set above
			LogManager.getLogManager().readConfiguration();
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		//add menu bindings
		closeMenuItem.disableProperty().bind(tabPane.getSelectionModel().selectedItemProperty().isNull());
		exportMenuItem.disableProperty().bind(tabPane.getSelectionModel().selectedItemProperty().isNull());
		saveAsMenuItem.disableProperty().bind(tabPane.getSelectionModel().selectedItemProperty().isNull());
		refreshMenuItem.disableProperty().bind(tabPane.getSelectionModel().selectedItemProperty().isNull());
		openInBrowserMenuItem.disableProperty().bind(tabPane.getSelectionModel().selectedItemProperty().isNull());
		embossMenuItem.disableProperty().bind(tabPane.getSelectionModel().selectedItemProperty().isNull());
	}

	private class ConsoleStream extends OutputStream {
		ByteArrayOutputStream bytes;
		private final String name;
		
		ConsoleStream(String name) {
			Objects.requireNonNull(name);
			this.bytes = new ByteArrayOutputStream();
			this.name = name;
		}
		
		void write(String s) {
			Platform.runLater(()->{
			     Document doc = console.getEngine().getDocument();
			     Node body = doc.getElementsByTagName("body").item(0);
			     Element p = doc.createElement("p");
			     p.setAttribute("class", name);
			     p.appendChild(doc.createTextNode(s));
			     body.appendChild(p);
			     while (body.getChildNodes().getLength()>CONSOLE_MESSAGE_LIMIT) {
			    	 body.removeChild(body.getChildNodes().item(0));
			     }
			     console.getEngine().executeScript("window.scrollTo(0, document.body.scrollHeight);");
			});
		}
		
		@Override
		public void write(int b) throws IOException {
			if (b=='\n') {
				bytes.write('\n');
				write(new String(bytes.toByteArray()));
				bytes.reset();
			} else if (b!='\r') {
				bytes.write(b);
			}
		}
	}
	
	void openArgs(String[] args) {
        if (args.length>0) {
        	addTab(null, args);
        }
	}

    @FXML
    public void refresh() {
		Tab t = tabPane.getSelectionModel().getSelectedItem();
		if (t!=null) {
			((PreviewController)t.getContent()).reload();
		}
    }
    
    @FXML
	public void openInBrowser() {
		if (Desktop.isDesktopSupported()) {
			try {
				Tab t = tabPane.getSelectionModel().getSelectedItem();
				if (t!=null) {
					Desktop.getDesktop().browse(new URI(((PreviewController)t.getContent()).getURL()));
				}				
			} catch (IOException | URISyntaxException e) {
			}
		}
	}
    
    @FXML void emboss() {
    	Tab t = tabPane.getSelectionModel().getSelectedItem();
		if (t!=null) {
			Platform.runLater(()->{
				PreviewController controller = ((PreviewController)t.getContent());
				Optional<BookReader.BookReaderResult> reader = controller.getBookReaderResult();
				if (reader.isPresent() && reader.get().isValid()) {
					PEFBook book = reader.get().getBook();
					EmbossView ev = new EmbossView(book);
					ev.showAndWait();
				} else {
					Alert alert = new Alert(AlertType.ERROR, Messages.ERROR_CANNOT_EMBOSS_INVALID_FILE.localize(), ButtonType.OK);
		    		alert.showAndWait();
				}
			});
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
    public void saveAs() {
    	Tab t = tabPane.getSelectionModel().getSelectedItem();
		if (t!=null) {
			PreviewController controller = ((PreviewController)t.getContent());
			// display save dialog
			Window stage = root.getScene().getWindow();
	    	FileChooser fileChooser = new FileChooser();
	    	fileChooser.setTitle(Messages.TITLE_SAVE_AS_DIALOG.localize());
	    	fileChooser.getExtensionFilters().add(new ExtensionFilter("PEF-file", ".pef"));
	    	File selected = fileChooser.showSaveDialog(stage);
	    	if (selected!=null) {
				// get the url of the current tab
				Optional<URI> _uri = controller.getBookURI();
				if (_uri.isPresent()) {
					URI uri = _uri.get();
					// store to selected location
					try {
						Files.copy(Paths.get(uri), new FileOutputStream(selected));
					} catch (IOException e) {
						// Display error
						e.printStackTrace();
					}
					// stop watching
					controller.closing();
					// update contents of tab
					setTab(t, selected.getName(), toArgs(selected));
					// TODO: Restore document position
				} else {
					// TODO: Display error
				}
	    	}
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
		File generated = dialog.generatedTestFile();
		if (generated!=null) {
			addTab(generated.getName(), toArgs(generated));
		}
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
    			PefBookAdapter book = controller.getSelectedItem();
    			if (book!=null) {
    				addTab(new File(book.getBook().getURI()));
    			}
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
    public void showImportBrailleDialog() {
    	Window stage = root.getScene().getWindow();
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(Messages.TITLE_IMPORT_BRAILLE_TEXT_DIALOG.localize());
    	File selected = fileChooser.showOpenDialog(stage);
    	if (selected!=null) {
    		ImportBrailleView brailleView = new ImportBrailleView(selected);
    		brailleView.showAndWait();
    		Map<String, String> settings = brailleView.getOptions();
    		if (settings!=null) {
				try {
					File output = File.createTempFile(selected.getName(), ".pef");
					output.deleteOnExit();
			    	Task<Void> importTask = new Task<Void>(){
						@Override
						protected Void call() throws Exception {
				    		TextConverterFacade f = new TextConverterFacade(TableCatalog.newInstance());
				    		f.parseTextFile(selected, output, settings);
							return null;
						}
			    	};
			    	importTask.setOnFailed(e->{
			    		logger.log(Level.WARNING, "Import failed.", importTask.getException());
			    		Alert alert = new Alert(AlertType.ERROR, importTask.getException().toString(), ButtonType.OK);
			    		alert.showAndWait();
			    	});
			    	importTask.setOnSucceeded(e->{
			    		Platform.runLater(()->{
			    			addTab(output);
			    		});
			    	});
			    	exeService.submit(importTask);
				} catch (IOException e1) {
					logger.log(Level.WARNING, "Failed to create temporary file.", e1);
				}
    		}
    	}
    }

    @FXML
    public void showImportDialog() {
    	Window stage = root.getScene().getWindow();
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(Messages.TITLE_IMPORT_SOURCE_DOCUMENT_DIALOG.localize());
		List<String> exts = TaskGroupFactoryMaker.newInstance().listAll().stream()
			.filter(spec -> !"pef".equals(spec.getInputFormat()))
			.map(spec -> "*."+spec.getInputFormat())
			.distinct().collect(Collectors.toList());
		// TODO: not all formats in this list are actually extensions, but it will have to do for now, since the TaskGroupFactory doesn't provide this information
		fileChooser.getExtensionFilters().add(new ExtensionFilter(Messages.EXTENSION_FILTER_SUPPORTED_FILES.localize(), exts));
		// TODO: add all extensions individually as well, requires additional information from the TaskGroupFactory about the formats (i.e. descriptions)
		fileChooser.getExtensionFilters().add(new ExtensionFilter(Messages.EXTENSION_FILTER_ALL_FILES.localize(), "*.*"));
    	File selected = fileChooser.showOpenDialog(stage);
    	if (selected!=null) {
    		// choose template
    		TemplateView dialog = new TemplateView();
    		if (dialog.hasTemplates()) {
    			dialog.initOwner(root.getScene().getWindow());
    			dialog.initModality(Modality.APPLICATION_MODAL); 
    			dialog.showAndWait();
    		}

    		// convert then add tab
    		addSourceTab(selected, dialog.getSelectedConfiguration());
    	}
    }
    
    @FXML
    public void exportFile() {
    	Tab t = tabPane.getSelectionModel().getSelectedItem();
		if (t!=null) {
			Optional<URI> bookUri = ((PreviewController)t.getContent()).getBookURI();
			if (bookUri.isPresent()) {
				File input = new File(bookUri.get());
		    	Window stage = root.getScene().getWindow();
		    	FileChooser fileChooser = new FileChooser();
		    	fileChooser.setTitle(Messages.TITLE_EXPORT_DIALOG.localize());
		    	File selected = fileChooser.showSaveDialog(stage);
		    	if (selected!=null) {
			    	Task<Void> exportTask = new Task<Void>(){
						@Override
						protected Void call() throws Exception {
							//TODO: sync this with the embossing code and its settings
					    	OutputStream os = new FileOutputStream(selected);
					    	EmbosserCatalogService ef = EmbosserCatalog.newInstance();
					    	Embosser emb = ef.newEmbosser("org_daisy.GenericEmbosserProvider.EmbosserType.NONE");
					    	String table = Settings.getSettings().getString(Keys.charset);
					    	if (table!=null) {
					    		emb.setFeature(EmbosserFeatures.TABLE, table);
					    	}
							EmbosserWriter embosser = emb.newEmbosserWriter(os);
							PEFHandler ph = new PEFHandler.Builder(embosser).build();
							FileInputStream is = new FileInputStream(input);
							SAXParserFactory spf = SAXParserFactory.newInstance();
							spf.setNamespaceAware(true);
							SAXParser sp = spf.newSAXParser();
							sp.parse(is, ph);
							return null;
						}
			    	};
			    	exportTask.setOnFailed(e->{
			    		exportTask.getException().printStackTrace();
			    		Alert alert = new Alert(AlertType.ERROR, exportTask.getException().toString(), ButtonType.OK);
			    		alert.showAndWait();
			    	});
			    	exeService.submit(exportTask);
				}	
	    	}
		}
    }
 
    @FXML
    public void closeApplication() {
    	((Stage)root.getScene().getWindow()).close();
    }
    
    private void addTab(File f) { 
    	addTab(f.getName(), toArgs(f));
    }
    
    private String[] toArgs(File f) {
    	return new String[]{"-open", f.getAbsolutePath()};
    }
    
    private void addTab(String title, String[] args) {
        Tab tab = new Tab();
        setTab(tab, title, args);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }
    
    private void setTab(Tab tab, String title, String[] args) {
    	if (title==null && args.length>=2) {
        	title = args[1];
        }
        if (title!=null) {
        	tab.setText(title);
        }
        PreviewController prv = new PreviewController();
        tab.setOnClosed(ev ->  {
        	prv.closing();
        });
        prv.open(args);
        tab.setContent(prv);

    }
    
    private void addSourceTab(File source, Map<String, Object> options) {
        Tab tab = new Tab();
        tab.setText(source.getName());
        PreviewController prv = new PreviewController();
        tab.setOnClosed(ev ->  {
        	prv.closing();
        });
        prv.convertAndOpen(source, options);
        tab.setContent(prv);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

}
