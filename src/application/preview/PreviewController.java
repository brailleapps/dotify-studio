package application.preview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.daisy.braille.utils.api.embosser.Embosser;
import org.daisy.braille.utils.api.embosser.EmbosserCatalog;
import org.daisy.braille.utils.api.embosser.EmbosserCatalogService;
import org.daisy.braille.utils.api.embosser.EmbosserFeatures;
import org.daisy.braille.utils.api.embosser.EmbosserWriter;
import org.daisy.braille.utils.pef.PEFBook;
import org.daisy.braille.utils.pef.PEFHandler;
import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.CompiledTaskSystem;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.consumer.tasks.TaskSystemFactoryMaker;
import org.daisy.dotify.studio.api.Editor;
import org.daisy.dotify.tasks.runner.RunnerResult;
import org.daisy.dotify.tasks.runner.TaskRunner;
import org.xml.sax.SAXException;

import com.googlecode.e2u.BookReader;
import com.googlecode.e2u.Start;
import com.googlecode.e2u.StartupDetails;
import com.googlecode.e2u.StartupDetails.Mode;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import shared.BuildInfo;
import shared.Settings;
import shared.Settings.Keys;

/**
 * Provides a preview controller.
 * @author Joel Håkansson
 *
 */
public class PreviewController extends BorderPane implements Editor {
	private static final Logger logger = Logger.getLogger(PreviewController.class.getCanonicalName());
	@FXML WebView browser;
	private OptionsController options;
	private Start start;
	private ExecutorService exeService;
	private boolean closing;
	private EmbossView embossView;
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private final ReadOnlyBooleanProperty canExportProperty;
	private final ReadOnlyBooleanProperty canSaveProperty;
	private StringProperty urlProperty;
	

	/**
	 * Creates a new preview controller.
	 */
	public PreviewController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Preview.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}

        WebEngine webEngine = browser.getEngine();
        browser.setOnDragOver(event->event.consume());
        webEngine.setCreatePopupHandler(p-> {
                Stage stage = new Stage(StageStyle.UTILITY);
                WebView wv2 = new WebView();
                stage.setScene(new Scene(wv2));
                stage.show();
                return wv2.getEngine();
            }
        );
		exeService = Executors.newWorkStealingPool();
		closing = false;
		canEmbossProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(true));
		canExportProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(true));
		canSaveProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		urlProperty = new SimpleStringProperty();
	}
	
	/**
	 * Converts and opens a file.
	 * @param selected the file
	 * @param options the options
	 */
	public void open(AnnotatedFile selected, Map<String, Object> options) {
		if (options==null) {
			open(selected.getFile());
		} else {
			try {
				File out = File.createTempFile("dotify-studio", ".pef");
				String tag = Settings.getSettings().getString(Keys.locale, Locale.getDefault().toLanguageTag());
				makeOptions();
				setRunning(true);
				DotifyTask dt = new DotifyTask(selected, out, tag, options);
				dt.setOnSucceeded(ev -> {
					Thread pefWatcher = open(out);
					updateOptions(dt.getValue(), options);
		    		Thread th = new Thread(new SourceDocumentWatcher(selected, out, tag, pefWatcher));
		    		th.setDaemon(true);
		    		th.start();
				});
				dt.setOnFailed(ev->{
					setRunning(false);
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
	
	private void updateOptions(DotifyResult dr, Map<String, Object> opts) {
		makeOptions();
		options.setOptions(dr.getTaskSystem(), dr.getResults(), opts);
		setRunning(false);
	}
	
	private void makeOptions() {
		if (options==null) {
			options = new OptionsController();
			setLeft(options);
		}
	}
	
	private void setRunning(boolean value) {
		if (options!=null) {
			options.setRunning(value);
		}
	}
	
    class SourceDocumentWatcher extends DocumentWatcher {
    	private final AnnotatedFile annotatedInput;
    	private final File output;
    	private final String locale;
    	private final Thread pefWatcher;
    	private boolean isRunning;

    	SourceDocumentWatcher(AnnotatedFile input, File output, String locale, Thread pefWatcher) {
    		super(input.getFile());
    		this.annotatedInput = input;
    		this.output = output;
    		this.locale = locale;
    		this.pefWatcher = pefWatcher;
    		this.isRunning = false;
    	}

		@Override
		boolean shouldMonitor() {
			return super.shouldMonitor() && !closing;
		}

		@Override
		boolean shouldPerformAction() {
			return !isRunning && ((super.shouldPerformAction() && options.isWatching()) || options.refreshRequested());
		}

		@Override
		void performAction() {
			try {
				isRunning = true;
				setRunning(true);
				Map<String, Object> opts = options.getParams();
	    		DotifyTask dt = new DotifyTask(annotatedInput, output, locale, opts);
	    		dt.setOnFailed(ev->{
	    			isRunning = false;
	    			setRunning(false);
	    			logger.log(Level.WARNING, "Update failed.", dt.getException());
		    		Alert alert = new Alert(AlertType.ERROR, dt.getException().toString(), ButtonType.OK);
		    		alert.showAndWait();
	    		});
	    		dt.setOnSucceeded(ev -> {
	    			isRunning = false;
	    			setRunning(false);
	    			Platform.runLater(() -> {
	    				if (pefWatcher!=null) {
	    					pefWatcher.interrupt();
	    				}
	    				updateOptions(dt.getValue(), opts);
	    			});
	    		});
	    		exeService.submit(dt);
			} catch (Exception e) { 
				logger.log(Level.SEVERE, "A severe error occurred.", e);
			}
		}
    }
    
    class PefDocumentWatcher extends DocumentWatcher {
    	PefDocumentWatcher(File pef) {
    		super(pef);
    	}

		@Override
		boolean shouldMonitor() {
			return super.shouldMonitor() && !closing;
		}

		@Override
		void performAction() {
			Platform.runLater(()->reload());
		}

    }
    
    private static class DotifyResult {
    	private final CompiledTaskSystem taskSystem;
    	private final List<RunnerResult> results;
    	
    	private DotifyResult(CompiledTaskSystem taskSystem, List<RunnerResult> results) {
    		this.taskSystem = taskSystem;
    		this.results = results;
    	}

		private CompiledTaskSystem getTaskSystem() {
			return taskSystem;
		}

		private List<RunnerResult> getResults() {
			return results;
		}
    	
    }
	
    class DotifyTask extends Task<DotifyResult> {
    	private final AnnotatedFile inputFile;
    	private final File outputFile;
    	private final String locale;
    	private final Map<String, Object> params;
    	
    	DotifyTask(AnnotatedFile inputFile, File outputFile, String locale, Map<String, Object> params) {
    		this.inputFile = inputFile;
    		this.outputFile = outputFile;
    		this.locale = locale;
    		this.params = new HashMap<>(params);
    		this.params.put("systemName", BuildInfo.NAME);
    		this.params.put("systemBuild", BuildInfo.BUILD);
    		this.params.put("systemRelease", BuildInfo.VERSION);
    		this.params.put("conversionDate", new Date().toString());
    	}
    	
    	@Override
    	protected DotifyResult call() throws Exception {
    		String inputFormat = getFormatString(inputFile);
    		TaskSystem ts;
			ts = TaskSystemFactoryMaker.newInstance().newTaskSystem(inputFormat, "pef", locale);
			logger.info("About to run with parameters " + params);
			
			logger.info("Thread: " + Thread.currentThread().getThreadGroup());
			CompiledTaskSystem tl = ts.compile(params);
			TaskRunner.Builder builder = TaskRunner.withName(ts.getName());
			return new DotifyResult(tl, builder.build().runTasks(inputFile, outputFile, tl));
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

	/**
	 * Starts a new preview server.
	 * @param file the file
	 * @return returns a thread that watches for changes in the pef file
	 */
	private Thread open(File file) {
		Thread pefWatcherThread = null;
		if (file!=null) {
			PefDocumentWatcher pefWatcher = new PefDocumentWatcher(file);
    		pefWatcherThread = new Thread(pefWatcher);
    		pefWatcherThread.setDaemon(true);
    		pefWatcherThread.start();
		}
		Task<String> startServer = new Task<String>() {

			@Override
			protected String call() throws Exception {
		        try {
		        	start = new Start();
		        	return start.start(new StartupDetails.Builder().mode(Mode.OPEN).file(file).log(false).display(false).build());
				} catch (Exception e1) {
					Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, "Failed to load server.", e1);;
				}  
		        return null;
			}
		};
		startServer.setOnSucceeded(ev -> {
				String url = startServer.getValue();
				this.urlProperty.set(url);
				if (url!=null) {
					browser.getEngine().load(url);
				} else {
					browser.getEngine().load(getClass().getResource("resource-files/fail.html").toString());
				}
			}
		);
		Thread th = new Thread(startServer);
		th.setDaemon(true);
		th.start();
		return pefWatcherThread;
	}
	
	/**
	 * Reloads the web view.
	 */
	public void reload() {
		browser.getEngine().reload();
	}

	public ReadOnlyStringProperty urlProperty() {
		return urlProperty;
	}
	
	/**
	 * Informs the controller that it should close.
	 */
	public void closing() {
		closing = true;
		if (start!=null) {
			start.stopServer();
		}
	}
	
	private Optional<URI> getBookURI() {
		if (start!=null) {
			return start.getMainPage().getBookURI();
		} else {
			return Optional.<URI>empty();
		}
	}
	
	/**
	 * Shows the emboss dialog.
	 */
	public void showEmbossDialog() {
		if (start!=null) {
			Optional<BookReader.BookReaderResult> reader = start.getMainPage().getBookReaderResult();
			if (reader.isPresent() && reader.get().isValid()) {
				PEFBook book = reader.get().getBook();
				if (embossView==null) {
					embossView = new EmbossView(book);
					embossView.initOwner(this.getScene().getWindow());
					embossView.initModality(Modality.APPLICATION_MODAL); 
				} else {
					embossView.setBook(book);
				}
				embossView.showAndWait();
			} else {
				Alert alert = new Alert(AlertType.ERROR, Messages.ERROR_CANNOT_EMBOSS_INVALID_FILE.localize(), ButtonType.OK);
	    		alert.showAndWait();
			}
		}
	}

	@Override
	public void save() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveAs(File f) throws IOException {
		URI uri = getBookURI().orElseThrow(()->new IOException("Nothing to save."));		
		try {
			Files.copy(Paths.get(uri), new FileOutputStream(f));
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public void export(File f) throws IOException {
		URI uri = getBookURI().orElseThrow(()->new IOException("Nothing to export."));
		File input = new File(uri);
		//TODO: sync this with the embossing code and its settings
    	OutputStream os = new FileOutputStream(f);
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
		SAXParser sp;
		try {
			sp = spf.newSAXParser();
			sp.parse(is, ph);
		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException("Failed to export", e);
		}
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		return Arrays.asList(new ExtensionFilter("PEF-file", "*.pef"));
	}

	@Override
	public ReadOnlyBooleanProperty canEmbossProperty() {
		return canEmbossProperty;
	}

	@Override
	public ReadOnlyBooleanProperty canExportProperty() {
		return canExportProperty;
	}

	@Override
	public ReadOnlyBooleanProperty canSaveProperty() {
		return canSaveProperty;
	}

	@Override
	public Map<String, Object> getOptions() {
		return options!=null?options.getParams():null;
	}

	@Override
	public void activate() {
		browser.requestFocus();
	}

}
