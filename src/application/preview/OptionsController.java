package application.preview;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.CompiledTaskSystem;
import org.daisy.dotify.api.tasks.TaskOption;
import org.daisy.dotify.tasks.runner.RunnerResult;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class OptionsController extends ScrollPane {
	private static final Logger logger = Logger.getLogger(OptionsController.class.getCanonicalName());
	@FXML
	public VBox vbox;

	public OptionsController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Options.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
	}
	
	public void addAll(CompiledTaskSystem ts, List<RunnerResult> opts, Map<String, Object> prvOpts) {
		displayItems(ts.getName(), ts.getOptions(), prvOpts);
		for (RunnerResult r : opts) {
			displayItems(r.getTask().getName(), r.getTask().getOptions(), prvOpts);
		}	
	}
	
	private void displayItems(String title, List<TaskOption> options, Map<String, Object> prvOpts) {
		if (options==null || options.isEmpty()) {
			return;
		}
		addGroupTitle(title);
		for (TaskOption o : options) {
			addItem(o, prvOpts);
		}
	}
	
	private void addGroupTitle(String name) {
		Label label = new Label(name);
		label.setTextFill(Paint.valueOf("#404040"));
		label.setFont(new Font("System Bold", 12));
		VBox.setMargin(label, new Insets(0, 0, 10, 0));
		vbox.getChildren().add(label);
	}
	
	public void addItem(TaskOption o, Map<String, Object> setOptions) {
		OptionItem item = new OptionItem(o);
		Object value = setOptions.get(o.getKey());
		if (value!=null) {
			item.setValue(value.toString());
		}
		VBox.setMargin(item, new Insets(0, 0, 10, 0));
		vbox.getChildren().add(item);
	}
	
	public void clear() {
		vbox.getChildren().clear();
	}
	
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
	
}
