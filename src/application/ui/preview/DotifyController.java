package application.ui.preview;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.daisy.dotify.studio.api.Converter;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.BaseFolder;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.media.FileSet;
import org.daisy.streamline.api.media.FileSetMaker;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.option.UserOptionValue;
import org.daisy.streamline.api.tasks.CompiledTaskSystem;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskSystem;
import org.daisy.streamline.api.tasks.TaskSystemException;
import org.daisy.streamline.api.tasks.TaskSystemFactoryException;
import org.daisy.streamline.api.tasks.TaskSystemFactoryMaker;
import org.daisy.streamline.engine.PathTools;
import org.daisy.streamline.engine.RunnerResult;
import org.daisy.streamline.engine.RunnerResults;
import org.daisy.streamline.engine.TaskRunner;

import application.common.BuildInfo;
import application.common.FeatureSwitch;
import application.common.Singleton;
import application.common.SupportedLocales;
import application.l10n.Messages;
import application.ui.template.TemplateView;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Modality;

/**
 * Provides a controller for Dotify.
 * @author Joel Håkansson
 *
 */
public class DotifyController extends BorderPane implements Converter {
	private static final Logger logger = Logger.getLogger(DotifyController.class.getCanonicalName());
	private static final String PRODUCT_LOCALE_KEY = "product-locale";
	private final Wrapper<Map<String, Object>> overrideParameters = new Wrapper<>();
	@FXML private ScrollPane options;
	@FXML private VBox tools;
	@FXML private Button toggle;
	@FXML private VBox vbox;
	@FXML private Button applyButton;
	@FXML private CheckBox monitorCheckbox;
	@FXML private ProgressIndicator progress;
	private final File input;
	private final BaseFolder outFolder;
	private boolean refreshRequested;
	private Set<UserOption> values;
	private boolean closing;
	private ExecutorService exeService;
	private BooleanProperty showOptions;
	private BooleanProperty canRequestUpdate;
	private String locale;

	/**
	 * Creates a new options controller.
	 * @param selected the input file
	 * @param tag target locale
	 * @param outputFormat output format
	 * @param options converter options
	 * @param onSuccess 	a function to call when conversion has completed successfully, the returned consumer 
	 * 						will be called if the result is updated.
	 * @throws IOException if an I/O error occurs
	 */
	public DotifyController(AnnotatedFile selected, String tag, FileDetails outputFormat, Map<String, Object> options, Function<File, Consumer<File>> onSuccess) throws IOException {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Dotify.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		this.input = selected.getPath().toFile();
		this.locale = tag;
		refreshRequested = false;
		closing = false;
		exeService = Executors.newWorkStealingPool();
		showOptions = new SimpleBooleanProperty(true);
		showOptions.addListener((o, ov, nv)->{
			if (nv.booleanValue()) {
				setBottom(tools);
				setCenter(this.options);
				toggle.setText("<");
				toggle.getTooltip().setText(Messages.TOOLTIP_HIDE_CONVERTER.localize());
			} else {
				setBottom(null);
				setCenter(null);
				toggle.setText(">");
				toggle.getTooltip().setText(Messages.TOOLTIP_SHOW_CONVERTER.localize());
			}		
		});
		canRequestUpdate = new SimpleBooleanProperty(false);
		setRunning(0);
		overrideParameters.setValue(options);
		Thread th;
		if (FeatureSwitch.PROCESS_FILE_SET.isOn()) {
			this.outFolder = BaseFolder.with(PathTools.createTempFolder());
			th = new Thread(new SourceDocumentWatcher(selected, outFolder, outputFormat, onSuccess));
		} else {
			this.outFolder = null;
			File outFile = File.createTempFile("dotify-studio", "."+outputFormat.getExtension());
			outFile.deleteOnExit();
			th = new Thread(new SourceDocumentWatcher(selected, outFile, outputFormat, onSuccess));
		}
		th.setDaemon(true);
		th.start();
		requestRefresh();
	}
	
	/**
	 * Sets options.
	 * @param ts the task system
	 * @param opts the runner resultsList
	 * @param prvOpts the previous options
	 */
	public void setOptions(CompiledTaskSystem ts, List<RunnerResult> opts, Map<String, Object> prvOpts) {
		Map<String, Object> ui = getParams();
		clear();
		values = new HashSet<>();
		UserOption.Builder optBuilder = new UserOption.Builder(PRODUCT_LOCALE_KEY)
				.displayName(Messages.LABEL_TARGET_LOCALE.localize())
				.defaultValue(locale);
		SupportedLocales.list().stream()
			.map(v->new UserOptionValue.Builder(v.toLanguageTag()).displayName(v.getDisplayName()).build())
			.forEach(v->optBuilder.addValue(v));
		displayItems(Messages.LABEL_GENERAL.localize(),
				Arrays.asList(optBuilder.build()), 
				prvOpts);
		displayItems(ts.getName(), ts.getOptions(), prvOpts);
		if (opts!=null) {
			for (RunnerResult r : opts) {
				displayItems(r.getTask().getName(), r.getTask().getOptions(), prvOpts);
			}
		} else {
			for (InternalTask r : ts) {
				displayItems(r.getName(), r.getOptions(), prvOpts);
			}
		}
		setParams(ui);
	}
	
	private void displayItems(String title, List<UserOption> options, Map<String, Object> prvOpts) {
		if (options==null || options.isEmpty()) {
			return;
		}
		addGroupTitle(title);
		List<UserOption> sortedOptions = options.stream()
				.sorted((o1, o2) -> {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				})
				.collect(Collectors.toList());
		for (UserOption o : sortedOptions) {
			addItem(o, prvOpts);
		}
	}
	
	private void addGroupTitle(String name) {
		Label label = new Label(name);
		label.setTextFill(Paint.valueOf("#404040"));
		label.setFont(new Font("System Bold", 12));
		VBox.setMargin(label, new Insets(0, 0, 10, 5));
		vbox.getChildren().add(label);
	}
	
	private void addItem(UserOption o, Map<String, Object> setOptions) {
		OptionItem item = new OptionItem(o, values.contains(o), setOptions.get(o.getKey()));
		vbox.getChildren().add(item);
		values.add(o);
	}
	
	private void clear() {
		vbox.getChildren().clear();
	}
	
	/**
	 * Gets the parameters.
	 * @return returns the parameters
	 */
	public Map<String, Object> getParams() {
		Map<String, Object> opts = new HashMap<>();
		for (Node n : vbox.getChildren()) {
			if (n instanceof OptionItem) {
				OptionItem o = (OptionItem)n;
				String value = o.getValue();
				if (value!=null&&!"".equals(value)) {
					opts.put(o.getKey(), value);
				}
				//TODO: warn if key already is included
			}
		}
		return opts;
	}
	
	public void setParams(Map<String, Object> opts) {
		Map<String, Object> optsCopy = new HashMap<>(opts);
		for (Node n : vbox.getChildren()) {
			if (n instanceof OptionItem) {
				OptionItem o = (OptionItem)n;
				Object value = optsCopy.remove(o.getKey());
				if (value!=null) {
					o.setValue(value.toString());
				}
			}
		}
		displayItems("Template", optsCopy.entrySet().stream()
				.map(v->new UserOption.Builder(v.getKey()).defaultValue("").addValue(new UserOptionValue.Builder(v.getValue().toString()).build()).build())
				.collect(Collectors.toList()), optsCopy);
	}
	
	@Override
	public void selectTemplateAndApply() {
		TemplateView dialog = new TemplateView(input);
		dialog.initOwner(getScene().getWindow());
		dialog.initModality(Modality.APPLICATION_MODAL); 
		dialog.showAndWait();
		if (dialog.getSelectedConfiguration().isPresent()) {
			synchronized (overrideParameters) {
				overrideParameters.setValue(dialog.getSelectedConfiguration().get());
			}
			vbox.getChildren().clear();
			requestRefresh();
		}
	}

	@Override
	public void apply() {
		requestRefresh();
	}
	
	@Override
	public boolean getWatchSource() {
		return monitorCheckbox.selectedProperty().get();
	}

	@Override
	public void setWatchSource(boolean value) {
		monitorCheckbox.selectedProperty().set(value);
	}

	@Override
	public BooleanProperty watchSourceProperty() {
		return monitorCheckbox.selectedProperty();
	}

	
	@FXML void requestRefresh() {
		refreshRequested = true;
		canRequestUpdate.set(false);
		setRunning(0);
	}
	
	@Override
	public final boolean getShowOptions() {
		return showOptions.get();
	}

	@Override
	public final void setShowOptions(boolean value) {
		showOptions.set(value);
	}

	@FXML public void toggleOptions() {
		setShowOptions(getBottom()!=tools);
	}
	
	@Override
	public BooleanProperty showOptionsProperty() {
		return showOptions;
	}
	
	@Override public void saveTemplate() {
		TemplateDetailsView dialog = new TemplateDetailsView();
		dialog.initOwner(this.getScene().getWindow());
		dialog.initModality(Modality.APPLICATION_MODAL); 
		dialog.showAndWait();
		if (dialog.getResult().isPresent()) {
			NameDesc nameDesc = dialog.getResult().get();
			Singleton.getInstance().getConfigurationsCatalog().addConfiguration(nameDesc.getName(), nameDesc.getDesc(), getParams());
		}
	}
	
	boolean isWatching() {
		return monitorCheckbox.isSelected();
	}
	
	boolean refreshRequested() {
		if (refreshRequested) {
			refreshRequested = false;
			return true;
		}
		return false;
	}
	
	void setRunning(double p) {
		boolean running = p<1&&p>=0;
		Platform.runLater(()->{
			progress.setProgress(
				FeatureSwitch.REPORT_PROGRESS.isOn()?p:
					p<1?ProgressIndicator.INDETERMINATE_PROGRESS:1
				);
			progress.setVisible(running);
			applyButton.setVisible(!running);
		});
	}
	
	public void closing() {
		closing = true;
		if (canRequestUpdate.get()) {
			new Thread(()->{
				deleteOutputFolder();
			}).start();
		}
	}
	
	private void deleteOutputFolder() {
		if (outFolder==null) {
			return;
		}
		int maxTries = 9;
		for (int tries = 0; tries<=maxTries; tries++) {
			try {
				PathTools.deleteRecursive(outFolder.getPath());
				logger.info("Deleted folder: " + outFolder.getPath());
				break;
			} catch (IOException e) {
				if (tries==maxTries) {
					logger.log(Level.WARNING, "Failed to clean up temporary folder: " + outFolder.getPath(), e);
				} else {
					if (logger.isLoggable(Level.FINE)) {
						logger.log(Level.FINE, "Failed to clean up temporary folder: " + outFolder.getPath(), e);
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e2) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}		
	}

	class SourceDocumentWatcher extends DocumentWatcher {
		private final AnnotatedFile annotatedInput;
		private final File outputFile;
		private final BaseFolder outputFolder;
		private final FileDetails outputFormat;
		// notify a caller about changes to the result file
		private final Function<File, Consumer<File>> onSuccess;
		private Consumer<File> resultWatcher = null;
		private boolean isRunning = false;

		SourceDocumentWatcher(AnnotatedFile input, File output, FileDetails outputFormat, Function<File, Consumer<File>> onSuccess) {
			super(input.getPath().toFile());
			this.annotatedInput = input;
			this.outputFile = output;
			this.outputFolder = null;
			this.outputFormat = outputFormat;
			this.onSuccess = onSuccess;
		}
		
		SourceDocumentWatcher(AnnotatedFile input, BaseFolder output, FileDetails outputFormat, Function<File, Consumer<File>> onSuccess) {
			super(input.getPath().toFile());
			this.annotatedInput = input;
			this.outputFile = null;
			this.outputFolder = output;
			this.outputFormat = outputFormat;
			this.onSuccess = onSuccess;
		}

		@Override
		boolean shouldMonitor() {
			return super.shouldMonitor() && !closing;
		}

		@Override
		boolean shouldPerformAction() {
			return !isRunning && ((super.shouldPerformAction() && isWatching()) || refreshRequested());
		}

		@Override
		void performAction() {
			try {
				isRunning = true;
				setRunning(0);
				Map<String, Object> opts;
				// Using a local variable as intermediary storage here to avoid synchronization
				// on the else statement
				Map<String, Object> overrides = null;
				synchronized(overrideParameters) {
					overrides = overrideParameters.getValue();
					overrideParameters.setValue(null);
				}
				if (overrides!=null) {
					opts = overrides;
				} else {
					opts = getParams();
				}
				DotifyTaskBase dt;
				String loc = Optional.ofNullable(opts.get(PRODUCT_LOCALE_KEY)).map(v->v.toString()).orElse(locale);
				if (outputFile!=null) {
					dt = new DotifyFileTask(annotatedInput, outputFile, loc, outputFormat, opts);
				} else if (outputFolder!=null) {
					dt = new DotifyFileSetTask(annotatedInput, outputFolder, loc, outputFormat, opts);
				} else {
					throw new AssertionError("Error in code");
				}
				dt.setOnFailed(ev->{
					isRunning = false;
					setRunning(1);
					canRequestUpdate.set(true);
					logger.log(Level.WARNING, "Update failed.", dt.getException());
					Alert alert = new Alert(AlertType.ERROR, dt.getException().toString(), ButtonType.OK);
					alert.showAndWait();
				});
				dt.setOnSucceeded(ev -> {
					Platform.runLater(() -> {
						DotifyResult dr = dt.getValue();
						if (onSuccess!=null) {
							if (outputFile!=null) {
								applyResultWatcher(outputFile);
							} else if (outputFolder!=null) {
								dr.getFileSet().ifPresent(v->applyResultWatcher(v.getManifest().getPath().toFile()));
							} else {
								throw new AssertionError("Error in code");
							}
						}
						setOptions(dr.getTaskSystem(), dr.getResults(), opts);
						setRunning(1);
						isRunning = false;
						canRequestUpdate.set(true);
						if (closing) {
							deleteOutputFolder();
						}
					});
				});
				exeService.submit(dt);
			} catch (Exception e) { 
				logger.log(Level.SEVERE, "A severe error occurred.", e);
			}
		}
		
		private void applyResultWatcher(File f) {
			if (resultWatcher==null) {
				// Create it
				resultWatcher = onSuccess.apply(f);
			}
			resultWatcher.accept(f);
		}
	}
	
	private abstract class DotifyTaskBase extends Task<DotifyResult> {
		protected final AnnotatedFile inputFile;
		protected final String locale;
		protected final FileDetails outputFormat;
		protected final Map<String, Object> params;
		
		DotifyTaskBase(AnnotatedFile inputFile, String locale, FileDetails outputFormat, Map<String, Object> params) {
			this.inputFile = inputFile;
			this.locale = locale;
			this.outputFormat = outputFormat;
			this.params = new HashMap<>(params);
			this.params.put("systemName", BuildInfo.NAME);
			this.params.put("systemBuild", BuildInfo.BUILD);
			this.params.put("systemRelease", BuildInfo.VERSION);
			this.params.put("conversionDate", new Date().toString());
			this.params.put("allows-ending-volume-on-hyphen", "false");
		}
		
		protected TaskSystem makeTaskSystem() throws TaskSystemFactoryException {
			String inputFormat = getFormatString(inputFile);
			TaskSystem ts = TaskSystemFactoryMaker.newInstance().newTaskSystem(inputFormat, outputFormat.getFormatName(), locale);
			logger.info("About to run with parameters " + params);
			logger.info("Thread: " + Thread.currentThread().getThreadGroup());
			return ts;
		}
		
		protected CompiledTaskSystem setupTaskSystem(TaskSystem ts) throws TaskSystemException {
			CompiledTaskSystem tl = ts.compile(params);
			if (vbox.getChildren().isEmpty()) {
				Platform.runLater(()->{
					setOptions(tl, null, params);
				});
			}
			return tl;
		}
		
		protected TaskRunner makeTaskRunner(TaskSystem ts) {
			return TaskRunner.withName(ts.getName())
				.addProgressListener(v->setRunning(v.getProgress()))
				.build();
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

	private class DotifyFileTask extends DotifyTaskBase {
		private final File outputFile;

		DotifyFileTask(AnnotatedFile inputFile, File outputFile, String locale, FileDetails outputFormat, Map<String, Object> params) {
			super(inputFile, locale, outputFormat, params);
			this.outputFile = outputFile;
		}

		@Override
		protected DotifyResult call() throws Exception {
			TaskSystem ts = makeTaskSystem();
			CompiledTaskSystem tl = setupTaskSystem(ts);
			return new DotifyResult(tl, makeTaskRunner(ts).runTasks(inputFile, outputFile, tl));
		}

	}
	
	private class DotifyFileSetTask extends DotifyTaskBase {
		private final BaseFolder outputFile;

		DotifyFileSetTask(AnnotatedFile inputFile, BaseFolder outputFile, String locale, FileDetails outputFormat, Map<String, Object> params) {
			super(inputFile, locale, outputFormat, params);
			this.outputFile = outputFile;
		}

		@Override
		protected DotifyResult call() throws Exception {
			TaskSystem ts = makeTaskSystem();
			CompiledTaskSystem tl = setupTaskSystem(ts);
			return new DotifyResult(tl, makeTaskRunner(ts).runTasks(FileSetMaker.newInstance().create(inputFile, params), outputFile, "result."+outputFormat.getExtension(), tl));
		}

	}

	private static class DotifyResult {
		private final CompiledTaskSystem taskSystem;
		private final List<RunnerResult> resultsList;
		private final Optional<FileSet> fileSet;

		private DotifyResult(CompiledTaskSystem taskSystem, List<RunnerResult> results) {
			this.taskSystem = taskSystem;
			this.resultsList = results;
			this.fileSet = Optional.empty();
		}
		
		private DotifyResult(CompiledTaskSystem taskSystem, RunnerResults results) {
			this.taskSystem = taskSystem;
			this.resultsList = results.getResults();
			this.fileSet = results.getFileSet();
		}

		private CompiledTaskSystem getTaskSystem() {
			return taskSystem;
		}

		private List<RunnerResult> getResults() {
			return resultsList;
		}
		
		private Optional<FileSet> getFileSet() {
			return fileSet;
		}
	}

	@Override
	public ReadOnlyBooleanProperty isIdleProperty() {
		return canRequestUpdate;
	}

	@Override
	public boolean getIsIdle() {
		return canRequestUpdate.get();
	}

}
