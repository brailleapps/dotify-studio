package application.ui.preview;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;
import org.daisy.dotify.common.xml.XmlEncodingDetectionException;
import org.daisy.dotify.studio.api.DocumentPosition;
import org.daisy.dotify.studio.api.Editor;
import org.daisy.dotify.studio.api.ExportAction;
import org.daisy.dotify.studio.api.SearchCapabilities;
import org.daisy.dotify.studio.api.SearchOptions;
import org.daisy.streamline.api.identity.IdentityProvider;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.media.InputStreamSupplier;
import org.daisy.streamline.api.validity.ValidationReport;
import org.daisy.streamline.api.validity.ValidatorFactoryMaker;
import org.daisy.streamline.api.validity.ValidatorFactoryMakerService;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.xml.sax.InputSource;

import application.common.BindingStore;
import application.common.Settings;
import application.l10n.Messages;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * Provides an editor controller.
 * @author Joel Håkansson
 *
 */
public class EditorController extends BorderPane implements Editor {
	private static final Logger logger = Logger.getLogger(EditorController.class.getCanonicalName());
	private static final TransformerFactory XSLT_FACTORY = TransformerFactory.newInstance();
	private static final char BYTE_ORDER_MARK = '\uFEFF';
	private static final int SYNTAX_HIGHLIGHTING_SIZE_LIMIT = 3_100_000; // This value has been tested
	private static final int VALIDATION_SIZE_LIMIT = 10_000_000; // This value is not tested, a lower or higher value may be better
	private static final int SIZE_WARNING_LIMIT = Math.min(SYNTAX_HIGHLIGHTING_SIZE_LIMIT, VALIDATION_SIZE_LIMIT);
	private static final ReadOnlyObjectProperty<SearchCapabilities> SEARCH_CAPABILITIES = new SimpleObjectProperty<>(
			new SearchCapabilities.Builder()
			.direction(false)
			.matchCase(true)
			.wrap(true)
			.find(true)
			.replace(true)
			.build()
	);

	@FXML HBox optionsBox;
	@FXML CheckBox wordWrap;
	@FXML CheckBox lineNumbers;
	@FXML Label encodingLabel;
	@FXML Label bomLabel;
	@FXML HBox xmlTools;
	private CodeArea codeArea;
	private VirtualizedScrollPane<CodeArea> scrollPane;
	private FileInfo fileInfo = new FileInfo.Builder((File)null).build();
	private ObjectProperty<FileDetails> fileDetails = new SimpleObjectProperty<>();
	private ObjectProperty<Optional<ValidationReport>> validationReport = new SimpleObjectProperty<>(Optional.empty());
	private ExecutorService executor;
	private final ReadOnlyBooleanProperty canEmbossProperty;
	private final BooleanProperty isLoadedProperty;
	private final BooleanProperty canSaveProperty;
	private final ReadOnlyStringProperty urlProperty;
	private final SimpleBooleanProperty modifiedProperty;
	private final SimpleBooleanProperty hasCancelledUpdateProperty;
	private final BooleanProperty atMarkProperty;
	private final BindingStore bindings;
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
		isLoadedProperty = new SimpleBooleanProperty(false);
		canSaveProperty = new SimpleBooleanProperty();
		urlProperty = new SimpleStringProperty();
		bindings = new BindingStore();
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
		// CodeArea doesn't appear to have a zoomProperty like WebView,
		// instead the css property below is used to change the size of everything
		codeArea.styleProperty().bind(Bindings.format("-fx-font-size: %.2fpt;", Settings.getSettings().zoomLevelProperty().multiply(15)));
		codeArea.getStylesheets().add(this.getClass().getResource("resource-files/codearea.css").toExternalForm());
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
		codeArea.richChanges()
			.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
			.successionEnds(Duration.ofMillis(1200))
			.supplyTask(this::computeValidationAsync)
			.awaitLatest()
			.filterMap(v -> {
				if (v.isSuccess()) {
					return Optional.of(v.get());
				} else {
					v.getFailure().printStackTrace();
					return Optional.empty();
				}
			})
			.subscribe(v -> validationReport.setValue(v));
		atMarkProperty.bind(codeArea.getUndoManager().atMarkedPositionProperty());
		modifiedProperty.bind(bindings.add(atMarkProperty.not().or(hasCancelledUpdateProperty)));
		canSaveProperty.bind(bindings.add(isLoadedProperty.and(modifiedProperty)));
		codeArea.setWrapText(true);
		scrollPane = new VirtualizedScrollPane<>(codeArea);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		setCenter(scrollPane);
	}
	
	public static boolean supportsFormat(FileDetails editorFormat) {
		// TODO: also support application/epub+zip
		return FormatChecker.isText(editorFormat) || FormatChecker.isHTML(editorFormat) || FormatChecker.isXML(editorFormat);
	}
	
	ValidatorFactoryMakerService factory = ValidatorFactoryMaker.newInstance();
	
	private Task<Optional<ValidationReport>> computeValidationAsync() {
		FileInfo info = fileInfo;
		boolean run = info.isXml() && codeArea.getLength()<VALIDATION_SIZE_LIMIT;
		String text = run?codeArea.getText():"";
		Task<Optional<ValidationReport>> task = new Task<Optional<ValidationReport>>() {
			@Override
			protected Optional<ValidationReport> call() throws Exception {
				if (run) {
					byte[] data = prepareSaveToFile(FileInfo.with(info), info, text);
					
					InputStreamSupplier source = new ByteInputStreamSupplier(fileInfo.getFile().toURI().toASCIIString(), data);
					return factory.newValidator(IdentityProvider.newInstance().identify(source)).map(pv->pv.validate(source));
				}
				return Optional.empty();
			}
		};
		executor.execute(task);
		return task;
	}
	
	private static class ByteInputStreamSupplier implements InputStreamSupplier {
		private final String systemId;
		private final byte[] data;
		private ByteInputStreamSupplier(String systemId, byte[] data) {
			this.systemId = systemId;
			this.data = data;
		}

		@Override
		public InputStream newInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		@Override
		public String getSystemId() {
			return systemId;
		}
	}
	
    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
    	boolean run = fileInfo.isXml()&&codeArea.getLength()<SYNTAX_HIGHLIGHTING_SIZE_LIMIT;
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return run?XMLStyleHelper.computeHighlighting(text):XMLStyleHelper.noStyles(text);
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
		if (!xml) {
			optionsBox.getChildren().remove(xmlTools);
		} else {
			if (!optionsBox.getChildren().contains(xmlTools)) {
				optionsBox.getChildren().add(2, xmlTools);
			}
		}
		xmlTools.setVisible(xml);
		FileInfo.Builder builder = new FileInfo.Builder(f);
		try {
			String text = loadData(Files.readAllBytes(f.toPath()), builder, xml);
			if (text.length()>SIZE_WARNING_LIMIT) {
				Platform.runLater(()->{
					Alert alert = new Alert(AlertType.WARNING,
							Messages.MESSAGE_WARNING_OPENING_LARGE_FILE_IN_EDITOR.localize(text.length()),
							ButtonType.OK);
					alert.showAndWait();
				});
			}
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
	
	@FXML void correctFormatting() {
		if (fileInfo.isXml()) {
			try {
				FileInfo.Builder builder = FileInfo.with(fileInfo);
				Source source = new StreamSource(new ByteArrayInputStream(prepareSaveToFile(builder, fileInfo, codeArea.getText())));
				source.setSystemId(fileInfo.getFile().toURI().toASCIIString());
				ByteArrayOutputStream result = new ByteArrayOutputStream();
				XMLTools.transform(source, new StreamResult(result), this.getClass().getResource("resource-files/pretty-print.xsl"), Collections.emptyMap(), XSLT_FACTORY);
				codeArea.replaceText(0, codeArea.getLength(), loadData(result.toByteArray(), builder, fileInfo.isXml()));
			} catch (XMLToolsException | IOException | XmlEncodingDetectionException e) {
				//TODO: show alert
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean findNext(String text, SearchOptions opts) {
		int pos = codeArea.getCaretPosition();
		Pattern pattern = Pattern.compile(
				(opts.shouldMatchCase()?"":"(?i)")
				+Pattern.quote(text)
				);
		Matcher m = pattern.matcher(codeArea.getText());
		boolean wrap = false;
		if (m.find(pos) || (opts.shouldWrapAround() && (wrap=m.find(0)))) {
			int s = m.start();
			int e = m.end();
			if (wrap && s > pos) {
				// no more matches;
				return false;
			}
			codeArea.selectRange(s, e);
			codeArea.showParagraphInViewport(codeArea.getCurrentParagraph());
			return true;
		}
		return false;
	}
	
	@Override
	public void replace(String replace) {
		if (codeArea.getSelection().getLength()>0) {
			codeArea.replaceSelection(replace);
		}
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
	public boolean saveAs(File f) throws IOException {
		if (confirmSave()) {
			updateFileInfo(saveToFileSynchronized(f, fileInfo, codeArea.getText()));
			return true;
		} else {
			return false;
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
		fileDetails.set(IdentityProvider.newInstance().identify(fileInfo.getFile()));
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
				return Arrays.asList(new ExtensionFilter(Messages.EXTENSION_FILTER_FILE.localize(ext), "*." + ext));
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
	public ObservableBooleanValue canEmboss() {
		return canEmbossProperty;
	}

	@Override
	public ReadOnlyStringProperty urlProperty() {
		return urlProperty;
	}

	@Override
	public ObservableBooleanValue canSave() {
		return canSaveProperty;
	}
	
	@Override
	public ObservableBooleanValue canSaveAs() {
		return isLoadedProperty;
	}; 

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

	@Override
	public void export(Window ownerWindow, ExportAction action) {
		//TODO: save file first!
		//See https://github.com/brailleapps/dotify-studio/issues/64
		action.export(ownerWindow, fileInfo.getFile());
	}

	@Override
	public ObservableObjectValue<FileDetails> fileDetails() {
		return fileDetails;
	}

	@Override
	public ObservableObjectValue<Optional<ValidationReport>> validationReport() {
		return validationReport;
	}

	@Override
	public boolean scrollTo(DocumentPosition msg) {
		if (msg.getLineNumber()>-1) {
			codeArea.moveTo(msg.getLineNumber()-1, Math.max(msg.getColumnNumber()-1, 0));
			codeArea.requestFollowCaret();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ObservableObjectValue<SearchCapabilities> searchCapabilities() {
		return SEARCH_CAPABILITIES;
	}

	@Override
	public String getSelectedText() {
		return codeArea.getSelectedText();
	}

}