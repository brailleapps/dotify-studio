package application.preview;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import application.l10n.Messages;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Provides an editor controller.
 * @author Joel Håkansson
 *
 */
public class EditorController extends BorderPane implements Preview {
	private static final Logger logger = Logger.getLogger(EditorController.class.getCanonicalName());
	@FXML CheckBox wordWrap;
	@FXML CheckBox lineNumbers;
	private CodeArea codeArea;
	private VirtualizedScrollPane<CodeArea> scrollPane;
	private File file;
	private ExecutorService executor;
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private final ReadOnlyBooleanProperty canExportProperty;
	private final ReadOnlyStringProperty urlProperty;
	//private String hash;

	/**
	 * Creates a new preview controller.
	 */
	public EditorController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Editor.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		executor = Executors.newWorkStealingPool();
		canEmbossProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		canExportProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		urlProperty = new SimpleStringProperty();
	}
	
	@FXML void initialize() {
		codeArea = new CodeArea();
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		/*
		codeArea.textProperty().addListener((obs, oldText, newText)-> {
			codeArea.setStyleSpans(0, XMLStyleHelper.computeHighlighting(newText));
		});*/
		codeArea.setWrapText(true);
		scrollPane = new VirtualizedScrollPane<>(codeArea);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		setCenter(scrollPane);
	}
	
    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return XMLStyleHelper.computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

	/**
	 * Converts and opens a file.
	 * @param f the file
	 */
	public void load(File f, boolean xmlMarkup) {
		this.file = f;
		codeArea.clear();
		if (xmlMarkup) {
			codeArea.richChanges()
			.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
	        .successionEnds(Duration.ofMillis(500))
	        .supplyTask(this::computeHighlightingAsync)
	        .awaitLatest(codeArea.richChanges())
	        .filterMap(t -> {
	            if(t.isSuccess()) {
	                return Optional.of(t.get());
	            } else {
	                t.getFailure().printStackTrace();
	                return Optional.empty();
	            }
	        })
	        .subscribe(this::applyHighlighting);
		} else {
			// TODO: clear colors?
		}
		try {
			codeArea.replaceText(0, 0, new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.warning("Failed to read: " + f);
		}
	}

	@FXML void toggleWordWrap() {
		scrollPane.setHbarPolicy(wordWrap.isSelected()?ScrollBarPolicy.NEVER:ScrollBarPolicy.AS_NEEDED);
		codeArea.setWrapText(wordWrap.isSelected());
	}

	@FXML void toggleLineNumbers() {
		if (lineNumbers.isSelected()) {
			codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		} else {
			codeArea.setParagraphGraphicFactory(null);
		}
	}

	private String makeHash(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return DatatypeConverter.printHexBinary(md.digest(data.getBytes())).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			logger.warning("Failed to create checksum");
		}
		return null;
	}

	@Override
	public boolean canSave() {
		return file!=null;
	}

	@Override
	public void save() {
		try {
			Files.write(file.toPath(), codeArea.getText().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.warning("Failed to write: " + file);
		}
	}

	@Override
	public void saveAs(File f) throws IOException {
		Files.copy(file.toPath(), f.toPath());
	}

	@Override
	public void export(File f) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closing() {
		executor.shutdown();
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		if (file!=null) {
			String name = file.getName();
			int dot = name.lastIndexOf('.');			
			if (dot>=0 && dot<name.length()) {
				String ext = name.substring(dot+1, name.length());
				return Arrays.asList(new ExtensionFilter(ext + "-files", "*." + ext));
			}
		}
		return Collections.emptyList();
	}

	@Override
	public void reload() {
		//
	}

	@Override
	public void showEmbossDialog() {
		throw new UnsupportedOperationException();
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
	public ReadOnlyStringProperty urlProperty() {
		return urlProperty;
	}

}