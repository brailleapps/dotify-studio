package application.ui.preview;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.option.UserOptionValue;
import org.daisy.streamline.api.tasks.CompiledTaskSystem;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskSystem;
import org.daisy.streamline.api.tasks.TaskSystemFactoryMaker;
import org.daisy.streamline.engine.RunnerResult;
import org.daisy.streamline.engine.TaskRunner;

import application.common.BuildInfo;
import application.common.FeatureSwitch;
import application.common.Singleton;
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
	private final Wrapper<Map<String, Object>> overrideParameters = new Wrapper<>();
	@FXML private ScrollPane options;
	@FXML private VBox tools;
	@FXML private Button toggle;
	@FXML private VBox vbox;
	@FXML private Button applyButton;
	@FXML private CheckBox monitorCheckbox;
	@FXML private ProgressIndicator progress;
	private final File input;
	private boolean refreshRequested;
	private Set<UserOption> values;
	private boolean closing;
	private ExecutorService exeService;
	private BooleanProperty showOptions;
	private BooleanProperty canRequestUpdate;

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
	public DotifyController(AnnotatedFile selected, String tag, String outputFormat, Map<String, Object> options, Function<File, Consumer<File>> onSuccess) throws IOException {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Dotify.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		this.input = selected.getPath().toFile();
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
		File outFile = File.createTempFile("dotify-studio", "."+outputFormat);
		outFile.deleteOnExit();
		Thread th = new Thread(new SourceDocumentWatcher(selected, outFile, tag, outputFormat, onSuccess));
		th.setDaemon(true);
		th.start();
		requestRefresh();
	}
	
	/**
	 * Sets options.
	 * @param ts the task system
	 * @param opts the runner results
	 * @param prvOpts the previous options
	 */
	public void setOptions(CompiledTaskSystem ts, List<RunnerResult> opts, Map<String, Object> prvOpts) {
		Map<String, Object> ui = getParams();
		clear();
		values = new HashSet<>();
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
					return o1.getKey().compareTo(o2.getKey());
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
	}

	class SourceDocumentWatcher extends DocumentWatcher {
		private final AnnotatedFile annotatedInput;
		private final File output;
		private final String locale;
		private final String outputFormat;
		// notify a caller about changes to the result file
		private final Function<File, Consumer<File>> onSuccess;
		private Consumer<File> resultWatcher;
		private boolean isRunning;

		SourceDocumentWatcher(AnnotatedFile input, File output, String locale, String outputFormat, Function<File, Consumer<File>> onSuccess) {
			super(input.getPath().toFile());
			this.annotatedInput = input;
			this.output = output;
			this.locale = locale;
			this.outputFormat = outputFormat;
			this.onSuccess = onSuccess;
			this.resultWatcher = null;
			this.isRunning = false;
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
				DotifyTask dt = new DotifyTask(annotatedInput, output, locale, outputFormat, opts);
				dt.setOnFailed(ev->{
					isRunning = false;
					setRunning(1);
					canRequestUpdate.set(true);
					logger.log(Level.WARNING, "Update failed.", dt.getException());
					Alert alert = new Alert(AlertType.ERROR, dt.getException().toString(), ButtonType.OK);
					alert.showAndWait();
				});
				dt.setOnSucceeded(ev -> {
					isRunning = false;
					setRunning(1);
					canRequestUpdate.set(true);
					Platform.runLater(() -> {
						if (onSuccess!=null) {
							if (resultWatcher==null) {
								resultWatcher = onSuccess.apply(output);
							}
							resultWatcher.accept(output);
						}
						DotifyResult dr = dt.getValue();
						setOptions(dr.getTaskSystem(), dr.getResults(), opts);
						setRunning(1);
					});
				});
				exeService.submit(dt);
			} catch (Exception e) { 
				logger.log(Level.SEVERE, "A severe error occurred.", e);
			}
		}
	}

	private class DotifyTask extends Task<DotifyResult> {
		private final AnnotatedFile inputFile;
		private final File outputFile;
		private final String locale;
		private final String outputFormat;
		private final Map<String, Object> params;

		DotifyTask(AnnotatedFile inputFile, File outputFile, String locale, String outputFormat, Map<String, Object> params) {
			this.inputFile = inputFile;
			this.outputFile = outputFile;
			this.locale = locale;
			this.outputFormat = outputFormat;
			this.params = new HashMap<>(params);
			this.params.put("systemName", BuildInfo.NAME);
			this.params.put("systemBuild", BuildInfo.BUILD);
			this.params.put("systemRelease", BuildInfo.VERSION);
			this.params.put("conversionDate", new Date().toString());
			this.params.put("allows-ending-volume-on-hyphen", "false");
		}

		@Override
		protected DotifyResult call() throws Exception {
			String inputFormat = getFormatString(inputFile);
			TaskSystem ts;
			ts = TaskSystemFactoryMaker.newInstance().newTaskSystem(inputFormat, outputFormat, locale);
			logger.info("About to run with parameters " + params);

			logger.info("Thread: " + Thread.currentThread().getThreadGroup());
			CompiledTaskSystem tl = ts.compile(params);
			if (vbox.getChildren().isEmpty()) {
				Platform.runLater(()->{
					setOptions(tl, null, params);
				});
			}
			
			TaskRunner.Builder builder = TaskRunner.withName(ts.getName());
			return new DotifyResult(tl, builder
					.addProgressListener(v->setRunning(v.getProgress()))
					.build()
					.runTasks(inputFile, outputFile, tl));
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

	@Override
	public ReadOnlyBooleanProperty isIdleProperty() {
		return canRequestUpdate;
	}

	@Override
	public boolean getIsIdle() {
		return canRequestUpdate.get();
	}

}
