package application.preview;

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

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.tasks.CompiledTaskSystem;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskSystem;
import org.daisy.streamline.api.tasks.TaskSystemFactoryMaker;
import org.daisy.streamline.engine.RunnerResult;
import org.daisy.streamline.engine.TaskRunner;

import application.l10n.Messages;
import javafx.application.Platform;
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
import shared.BuildInfo;

/**
 * Provides a controller for Dotify.
 * @author Joel Håkansson
 *
 */
public class DotifyController extends BorderPane {
	private static final Logger logger = Logger.getLogger(DotifyController.class.getCanonicalName());
	@FXML private ScrollPane options;
	@FXML private VBox tools;
	@FXML private Button toggle;
	@FXML private VBox vbox;
	@FXML private Button applyButton;
	@FXML private CheckBox monitorCheckbox;
	@FXML private ProgressIndicator progress;
	private boolean refreshRequested;
	private Set<UserOption> values;
	private boolean closing;
	private ExecutorService exeService;

	/**
	 * Creates a new options controller.
	 * @param selected
	 * @param out
	 * @param tag
	 * @param options
	 * @param onSuccess 	a function to call when conversion has completed successfully, the returned consumer 
	 * 						will be called if the result is updated.
	 */
	public DotifyController(AnnotatedFile selected, File out, String tag, String outputFormat, Map<String, Object> options, Function<File, Consumer<File>> onSuccess) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Options.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		refreshRequested = false;
		closing = false;
		exeService = Executors.newWorkStealingPool();
		setRunning(true);
		DotifyTask dt = new DotifyTask(selected, out, tag, outputFormat, options);
		dt.setOnSucceeded(ev -> {
			Consumer<File> resultWatcher = onSuccess.apply(out);
			DotifyResult dr = dt.getValue();
			setOptions(dr.getTaskSystem(), dr.getResults(), options);
			setRunning(false);
			Thread th = new Thread(new SourceDocumentWatcher(selected, out, tag, outputFormat, resultWatcher));
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
		for (Node n : vbox.getChildren()) {
			if (n instanceof OptionItem) {
				OptionItem o = (OptionItem)n;
				Object value = opts.get(o.getKey());
				if (value!=null) {
					o.setValue(value.toString());
				}
			}
		}
	}
	
	@FXML void requestRefresh() {
		refreshRequested = true;
	}
	
	@FXML void toggleOptions() {
		if (getBottom()==tools) {
			setBottom(null);
			setCenter(null);
			toggle.setText(">");
			toggle.getTooltip().setText(Messages.TOOLTIP_SHOW_OPTIONS.localize());
		} else {
			setBottom(tools);
			setCenter(options);
			toggle.setText("<");
			toggle.getTooltip().setText(Messages.TOOLTIP_HIDE_OPTIONS.localize());
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
	
	void setRunning(boolean running) {
		Platform.runLater(()->{
			progress.setProgress(running?ProgressIndicator.INDETERMINATE_PROGRESS:1);
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
		private final Consumer<File> resultWatcher;
		private boolean isRunning;

		SourceDocumentWatcher(AnnotatedFile input, File output, String locale, String outputFormat, Consumer<File> resultWatcher) {
			super(input.getFile());
			this.annotatedInput = input;
			this.output = output;
			this.locale = locale;
			this.outputFormat = outputFormat;
			this.resultWatcher = resultWatcher;
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
				setRunning(true);
				Map<String, Object> opts = getParams();
				DotifyTask dt = new DotifyTask(annotatedInput, output, locale, outputFormat, opts);
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
						if (resultWatcher!=null) {
							resultWatcher.accept(output);
						}
						DotifyResult dr = dt.getValue();
						setOptions(dr.getTaskSystem(), dr.getResults(), opts);
						setRunning(false);
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

}
