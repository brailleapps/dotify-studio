package application.preview;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.stage.FileChooser.ExtensionFilter;

public interface Preview {

	/**
	 * Returns true if this preview can be saved at it's current location (it has one that makes sense
	 * to a user)
	 */
	public boolean canSave();

	/**
	 * Saves the file to the current location.
	 * @throws IllegalStateException if no location has been set
	 */
	public void save();

	/**
	 * Saves the file to another location.
	 * @throws IOException if the file could not be saved.
	 */
	public void saveAs(File f) throws IOException;

	/**
	 * Returns true if this file can be exported.
	 * @return true if the file can be exported, false otherwise
	 */
	public default boolean canExport() {
		return canExportProperty().get();
	}
	
	public ReadOnlyBooleanProperty canExportProperty();

	/**
	 * Exports the file.
	 * @param f the file to export
	 * @throws IOException if the file could not be exported
	 */
	public void export(File f) throws IOException;

	/**
	 * Informs the controller that it should close.
	 */
	public void closing();

	/**
	 * Gets the url for the book for the purpose of opening in a web browser.
	 * If the file type cannot be opened in a web browser, an empty
	 * optional should be returned.
	 * 
	 * @return returns the url
	 */
	public default Optional<String> getURL() {
		return Optional.ofNullable(urlProperty().get());
	}
	
	public ReadOnlyStringProperty urlProperty();
	
	public List<ExtensionFilter> getSaveAsFilters();
	
	/**
	 * Reload content
	 */
	public void reload();
	
	public default boolean canEmboss() {
		return canEmbossProperty().get();
	}
	
	public ReadOnlyBooleanProperty canEmbossProperty();

	/**
	 * Shows the emboss dialog.
	 */
	public void showEmbossDialog();

}
