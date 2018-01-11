package application.preview;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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

import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;
import org.daisy.dotify.common.xml.XmlEncodingDetectionException;
import org.daisy.dotify.studio.api.Editor;
import org.daisy.streamline.api.media.FileDetails;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.xml.sax.InputSource;

import application.l10n.Messages;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Provides an editor controller.
 * @author Joel Håkansson
 *
 */
public class EditorController extends BorderPane implements Editor {
	private static final Logger logger = Logger.getLogger(EditorController.class.getCanonicalName());
	private static final char BYTE_ORDER_MARK = '\uFEFF';

	@FXML CheckBox wordWrap;
	@FXML CheckBox lineNumbers;
	@FXML Label encodingLabel;
	@FXML Label bomLabel;
	private CodeArea codeArea;
	private VirtualizedScrollPane<CodeArea> scrollPane;
	private FileInfo fileInfo = new FileInfo.Builder((File)null).build();
	private ExecutorService executor;
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private final ReadOnlyBooleanProperty canExportProperty;
	private final BooleanProperty isLoadedProperty;
	private final BooleanProperty canSaveProperty;
	private final ReadOnlyStringProperty urlProperty;
	private final SimpleBooleanProperty modifiedProperty;
	private final SimpleBooleanProperty hasCancelledUpdateProperty;
	private final BooleanProperty atMarkProperty;
	private ChangeWatcher changeWatcher;
	private boolean needsUpdate = false;
	private Long lastSaved = 0l;
	private boolean closing = false;
	//private String hash;

	/**
	 * Creates a new preview controller.
	 */
	public EditorController() {
		modifiedProperty = new SimpleBooleanProperty();
		hasCancelledUpdateProperty = new SimpleBooleanProperty(false);
		atMarkProperty = new SimpleBooleanProperty();
		canEmbossProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		canExportProperty = BooleanProperty.readOnlyBooleanProperty(new SimpleBooleanProperty(false));
		isLoadedProperty = new SimpleBooleanProperty(false);
		canSaveProperty = new SimpleBooleanProperty();
		urlProperty = new SimpleStringProperty();
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Editor.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}
		executor = Executors.newWorkStealingPool();
	}
	
	@FXML void initialize() {
		codeArea = new CodeArea();
		codeArea.getStylesheets().add(this.getClass().getResource("resource-files/editor.css").toExternalForm());
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		/*
		codeArea.textProperty().addListener((obs, oldText, newText)-> {
			codeArea.setStyleSpans(0, XMLStyleHelper.computeHighlighting(newText));
		});*/
		codeArea.focusedProperty().addListener((o, ov, nv) -> {
			if (nv && needsUpdate) {
				askForUpdate();
			}
		});
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
		atMarkProperty.bind(codeArea.getUndoManager().atMarkedPositionProperty());
		modifiedProperty.bind(atMarkProperty.not().or(hasCancelledUpdateProperty));
		canSaveProperty.bind(isLoadedProperty.and(modifiedProperty));
		codeArea.setWrapText(true);
		scrollPane = new VirtualizedScrollPane<>(codeArea);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		setCenter(scrollPane);
	}
	
	public static boolean supportsFormat(FileDetails editorFormat) {
		// TODO: also support application/epub+zip
		return FormatChecker.isText(editorFormat) || FormatChecker.isHTML(editorFormat) || FormatChecker.isXML(editorFormat);
	}


	
    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return fileInfo.isXml()?XMLStyleHelper.computeHighlighting(text):XMLStyleHelper.noStyles(text);
            }
        };
        executor.execute(task);
        return task;
    }
    
	private synchronized void askForUpdate() {
		if (needsUpdate) {
			needsUpdate = false;
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.CONFIRMATION, Messages.MESSAGE_FILE_MODIFIED_BY_ANOTHER_APPLICATION.localize(), ButtonType.YES, ButtonType.CANCEL);
				Optional<ButtonType> res = alert.showAndWait();
				Optional<ButtonType> yes = res.filter(v->v.equals(ButtonType.YES));
				if (yes.isPresent()) {
					yes
					.ifPresent(v->{
						load(fileInfo.getFile(), fileInfo.isXml());
					});					
				} else {
					hasCancelledUpdateProperty.set(true);
				}
			});
		}
	}
	
	private synchronized void requestUpdate() {
		needsUpdate = true;
		if (codeArea.isFocused()) {
			askForUpdate();
		}
	}

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

	/**
	 * Converts and opens a file.
	 * @param f the file
	 * @param xml if the file is xml
	 */
	public void load(File f, boolean xml) {
		FileInfo.Builder builder = new FileInfo.Builder(f);
		try {
			String text = loadData(Files.readAllBytes(f.toPath()), builder, xml);
			codeArea.replaceText(0, codeArea.getLength(), text);
			if (fileInfo==null || !f.equals(fileInfo.getFile())) {
				codeArea.getUndoManager().forgetHistory();
			}
			codeArea.getUndoManager().mark();
			codeArea.selectRange(0, 0);
			codeArea.scrollToPixel(Point2D.ZERO);
			isLoadedProperty.set(true);
		} catch (IOException | XmlEncodingDetectionException e) {
			logger.warning("Failed to read: " + f);
			isLoadedProperty.set(false);
		} finally {
			this.fileInfo = builder.build();
			updateFileInfo(this.fileInfo);
			// Watch document
			if (changeWatcher!=null) {
				changeWatcher.stop();
			}
			changeWatcher = new ChangeWatcher(f);
			Thread th = new Thread(changeWatcher);
			th.setDaemon(true);
			th.start();
		}
	}
	
	static String loadData(byte[] data, FileInfo.Builder builder, boolean xml) throws IOException, XmlEncodingDetectionException {
		builder.xml(xml);
		Charset encoding;
		if (xml) {
			//TODO: Ask if there is an encoding mismatch
			encoding = Charset.forName(XMLTools.detectXmlEncoding(data));
		} else {
			encoding = XMLTools.detectBomEncoding(data).orElse(StandardCharsets.UTF_8);
		}
		builder.charset(encoding);
		String text = new String(data, encoding);
		if (!text.isEmpty() && text.charAt(0)==BYTE_ORDER_MARK) {
			builder.bom(true);
			text = text.substring(1);
		} else {
			builder.bom(false);
		}
		return text;
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
	public void save() {
		try {
			if (confirmSave()) {
				updateFileInfo(saveToFileSynchronized(fileInfo.getFile(), fileInfo, codeArea.getText()));
				codeArea.getUndoManager().mark();
			}
		} catch (IOException e) {
			logger.warning("Failed to write: " + fileInfo.getFile());
		}
	}

	@Override
	public void saveAs(File f) throws IOException {
		if (confirmSave()) {
			updateFileInfo(saveToFileSynchronized(f, fileInfo, codeArea.getText()));
		}
	}

	/**
	 * Confirms the save action with the user if needed. Specifically, if the file is XML and the contents is not well-formed.
	 * @return returns true if save should proceed.
	 */
	private boolean confirmSave() {
		try {
			if (fileInfo.isXml() && !XMLTools.isWellformedXML(new InputSource(new ByteArrayInputStream(prepareSaveToFile(FileInfo.with(fileInfo), fileInfo, codeArea.getText()))))) {
				Alert alert = new Alert(AlertType.WARNING, Messages.MESSAGE_CONFIRM_SAVE_MALFORMED_XML.localize(), ButtonType.YES, ButtonType.CANCEL);
				Optional<ButtonType> res = alert.showAndWait();
				return res.map(v->v.equals(ButtonType.YES)).orElse(false);
			} else {
				return true;
			}
		} catch (XMLToolsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void updateFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
		encodingLabel.setText(fileInfo.getCharset().name());
		bomLabel.setText(fileInfo.hasBom()?"BOM":"");
		hasCancelledUpdateProperty.set(false);
	}
	
	FileInfo saveToFileSynchronized(File f, FileInfo fileInfo, String text) throws IOException {
		synchronized (lastSaved) {
			FileInfo ret = saveToFile(f, fileInfo, text);
			lastSaved = fileInfo.getFile().lastModified();
			return ret;
		}
	}

	static FileInfo saveToFile(File f, FileInfo fileInfo, String text) throws IOException {
		FileInfo.Builder builder = FileInfo.with(fileInfo);
		builder.file(f);
		byte[] bytes = prepareSaveToFile(builder, fileInfo, text);
		// Creates a temporary file in the same directory as the target file, because renameTo might fail if
		// the new location is on a different file system.
		File ft = File.createTempFile("save", ".tmp", f.getParentFile());
		Files.write(ft.toPath(), bytes);
		if (!ft.renameTo(f)) {
			if (!f.delete() || !ft.renameTo(f)) {
				Platform.runLater(()->{
					Alert alert = new Alert(AlertType.WARNING, Messages.ERROR_FAILED_TO_WRITE_TO_FILE.localize(f.getName()), ButtonType.OK);
					alert.showAndWait();
				});
			}
		}
		return builder.build();
	}
	
	// TODO: This method has a side effect: it modifies the builder
	static byte[] prepareSaveToFile(FileInfo.Builder builder, FileInfo fileInfo, String text) throws IOException {
		Charset charset = StandardCharsets.UTF_8;
		Optional<String> _encoding;
		if (fileInfo.isXml() && (_encoding = XMLTools.getDeclaredEncoding(text)).isPresent()) {
			String encoding = _encoding.get();
			try {
				charset = Charset.forName(encoding);
			} catch (Exception e) {
				Platform.runLater(()-> {
					Alert alert = new Alert(AlertType.ERROR, Messages.ERROR_UNSUPPORTED_XML_ENCODING.localize(encoding), ButtonType.OK);
					alert.showAndWait();
				});
				return null;
			}
			if (StandardCharsets.UTF_16.equals(charset)) {
				// UTF-16 will append a BOM by itself
				builder.bom(true);
			} else if (fileInfo.hasBom() && (isStandardUnicodeCharset(charset) || isUtf32Charset(encoding))) {
				// Add BOM if the original file had it and the new encoding is a unicode charset
				text = BYTE_ORDER_MARK + text;
				builder.bom(true);
			} else {
				builder.bom(false);
			}
		} else {
			// Text file, or an XML-file without a declaration
			charset = fileInfo.getCharset();
			if (StandardCharsets.UTF_16.equals(charset)) {
				// UTF-16 will append a BOM by itself
				builder.bom(true);
			} else if (	(StandardCharsets.UTF_8.equals(charset) && fileInfo.hasBom()) ||
						(!StandardCharsets.UTF_8.equals(charset) && isStandardUnicodeCharset(charset) || isUtf32Charset(charset.name())) ) {
				// For text files, all unicode encodings require a BOM (unless it's utf-8)
				text = BYTE_ORDER_MARK + text;
				builder.bom(true);
			} else {
				builder.bom(false);
			}
		}
		builder.charset(charset);
		return text.getBytes(charset);
	}
	
	private static boolean isStandardUnicodeCharset(Charset charset) {
		return StandardCharsets.UTF_8.equals(charset) || StandardCharsets.UTF_16.equals(charset) || StandardCharsets.UTF_16LE.equals(charset)
				|| StandardCharsets.UTF_16BE.equals(charset);
	}
	
	private static boolean isUtf32Charset(String encoding) {
		return encoding.toLowerCase().startsWith("utf-32");
	}

	@Override
	public void export(File f) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closing() {
		closing = true;
		executor.shutdown();
	}

	@Override
	public List<ExtensionFilter> getSaveAsFilters() {
		if (fileInfo.getFile()!=null) {
			String name = fileInfo.getFile().getName();
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

	@Override
	public ReadOnlyBooleanProperty canSaveProperty() {
		return canSaveProperty;
	}

	private class ChangeWatcher extends DocumentWatcher {
		private boolean shouldMonitor = true;

		ChangeWatcher(File f) {
			super(f);
		}

		@Override
		boolean shouldMonitor() {
			return super.shouldMonitor() && !closing && shouldMonitor && file==fileInfo.getFile();
		}

		@Override
		boolean shouldPerformAction() {
			synchronized (lastSaved) {
				return super.shouldPerformAction() && lastSaved<file.lastModified();
			}
		}
		
		private void stop() {
			this.shouldMonitor = false;
		}

		@Override
		void performAction() {
			requestUpdate();
		}
		
	}

	@Override
	public ReadOnlyBooleanProperty modifiedProperty() {
		return modifiedProperty;
	}

	@Override
	public void activate() {
		codeArea.requestFocus();
	}
	
	@Override
	public Node getNode() {
		return this;
	}

}