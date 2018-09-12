package application.ui.impl.export;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.dotify.studio.api.ExportAction;
import org.daisy.dotify.studio.api.ExportActionDescription;
import org.daisy.dotify.studio.api.ExportActionProvider;
import org.daisy.streamline.api.media.FileDetails;

import application.common.FactoryPropertiesAdapter;
import application.common.Settings;
import application.common.Settings.Keys;
import application.ui.preview.FileDetailsCatalog;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Provides an export action provider for PEF-files.
 * @author Joel Håkansson
 */
public class PefExportProviderImpl implements ExportActionProvider {
	private static final Logger logger = Logger.getLogger(PefExportProviderImpl.class.getCanonicalName());
	private enum PefExportActionDescription {
		TO_TEXT(Messages.MENU_ITEM_EXPORT_TO_TEXT, ExportToText::new),
		SPLIT_PEF(Messages.MENU_ITEM_SPLIT_PEF, SplitPef::new);
		private final Supplier<ExportAction> action;
		private final Messages resourceKey;
		private final String identifier;
		private PefExportActionDescription(Messages name, Supplier<ExportAction> action) {
			this.action = action;
			this.resourceKey = name;
			this.identifier = String.format("%s.%s", this.getClass().getCanonicalName(), this.name());
		}

		ExportAction newAction() {
			return action.get();
		}
		
		String getIdentifier() {
			return identifier;
		}
		
		Messages getResourceKey() {
			return resourceKey;
		}

	}

	private static final ExecutorService EXE_SERVICE = Executors.newWorkStealingPool();
	
	private final Map<String, PefExportActionDescription> actions;
	
	/**
	 * Creates a new instance.
	 */
	public PefExportProviderImpl() {
		this.actions = new LinkedHashMap<>();
		for (PefExportActionDescription d : PefExportActionDescription.values()) {
			actions.put(d.getIdentifier(), d);
		}
	}

	@Override
	public List<ExportActionDescription> listActions() {
		return actions.values().stream()
				.map(v->new ExportActionDescription.Builder(v.getIdentifier())
						.name(v.getResourceKey().localize())
						.build()
				)
				.collect(Collectors.toList());
	}

	@Override
	public boolean supportsFormat(FileDetails format) {
		return FileDetailsCatalog.PEF_FORMAT.getMediaType().equals(format.getMediaType());
	}

	@Override
	public boolean supportsAction(String id) {
		return actions.containsKey(id);
	}

	@Override
	public Optional<ExportAction> newExportAction(String id) {
		return Optional.ofNullable(actions.get(id))
				.map(v->v.newAction());
	}
	
	private static class ExportToText implements ExportAction {

		@Override
		public void export(Window ownerWindow, File source) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(Messages.DIALOG_TITLE_EXPORT_TO_TEXT.localize());
			Settings.getSettings().getLastSavePath().ifPresent(v->fileChooser.setInitialDirectory(v));
			File target = fileChooser.showSaveDialog(ownerWindow);
			if (target!=null) {
				Settings.getSettings().setLastSavePath(target.getParentFile());
				// Use the preview table as the default
				Optional<String> tableId = Optional.ofNullable(Settings.getSettings().getString(Keys.charset));
				// List available tables
				Collection<FactoryProperties> fp = TableCatalog.newInstance().list();
				// Only display options if there is something to choose from
				if (fp.size()>1) {
					// Map tables to an adapter that can be displayed in a choice dialog
					List<FactoryPropertiesAdapter> fpa = fp.stream()
							.map(v->new FactoryPropertiesAdapter(v))
							.sorted()
							.collect(Collectors.toList());
					// Make a final variable reference for lambda
					final Optional<String> _tableId = tableId;
					// Find a default object in the list. If the selected table is in the list, choose that.
					// Otherwise, use the first item in the list.
					FactoryPropertiesAdapter selected = fpa.stream()
								.filter(v->_tableId.isPresent()&&v.getProperties().getIdentifier().equals(_tableId.get()))
								.findFirst()
								.orElse(fpa.get(0));
					ChoiceDialog<FactoryPropertiesAdapter> dialog = new ChoiceDialog<>(selected, fpa);
					dialog.setTitle(Messages.DIALOG_TITLE_EXPORT_OPTIONS.localize());
					dialog.setHeaderText(Messages.MESSAGE_SELECT_BRAILLE_TABLE.localize());
					dialog.setContentText(Messages.LABEL_BRAILLE_TABLE.localize());
					tableId = dialog.showAndWait()
							.map(v->v.getProperties().getIdentifier());
				} else if (fp.size()==1) {
					// Nothing to choose from, just use the one item that's in the list.
					// At this point it doesn't matter if this isn't the same as the preferred table
					// above, because it either doesn't exist in the list and the export will fail,
					// or the preferred table is in fact the item in the list.
					tableId = Optional.of(fp.iterator().next().getIdentifier());
				}
				if (tableId.isPresent()) {
					final String _tableId = tableId.get();
					Task<Void> exportTask = new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							PefExportActions.toText(source, target, _tableId);
							return null;
						}
					};
					exportTask.setOnSucceeded(e->{
						logger.info("Export completed.");
					});
					exportTask.setOnFailed(e->{
						exportTask.getException().printStackTrace();
						Alert alert = new Alert(AlertType.ERROR, exportTask.getException().toString(), ButtonType.OK);
						alert.showAndWait();
					});
					EXE_SERVICE.submit(exportTask);
				}
			}
		}
		
	}
	
	private static class SplitPef implements ExportAction {

		@Override
		public void export(Window ownerWindow, File source) {
			DirectoryChooser dirChooser = new DirectoryChooser();
			dirChooser.setTitle(Messages.DIALOG_TITLE_SPLIT_PEF.localize());
			Settings.getSettings().getLastSavePath().ifPresent(v->dirChooser.setInitialDirectory(v));
			File target = dirChooser.showDialog(ownerWindow);
			if (target!=null) {
				Settings.getSettings().setLastSavePath(target);
				if (target.listFiles().length>0) {
					Alert alert = new Alert(AlertType.CONFIRMATION, Messages.MESSAGE_CONFIRM_OVERWRITE.localize(), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
					alert.setHeaderText(Messages.LABEL_FOLDER_NOT_EMPTY.localize());
					Optional<ButtonType> ret = alert.showAndWait();
					if (ret.isPresent()) {
						ButtonType bt = ret.get();
						if (bt==ButtonType.CANCEL) {
							return;
						} else if (bt==ButtonType.NO) {
							export(ownerWindow, source);
							return;
						}
					}
				}
				Task<Void> exportTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						if (!PefExportActions.split(source, target)) {
							throw new RuntimeException("Failed to split");
						}
						return null;
					}
				};
				exportTask.setOnFailed(e->{
					exportTask.getException().printStackTrace();
					Alert alert = new Alert(AlertType.ERROR, exportTask.getException().toString(), ButtonType.OK);
					alert.showAndWait();
				});
				EXE_SERVICE.submit(exportTask);
			}
		}
		
	}

}
