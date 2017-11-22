package org.daisy.dotify.studio.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Provides an interface for editors.
 * @author Joel Håkansson
 */
public interface Editor {

	/**
	 * Returns true if this editor can be saved at its current location (it has
	 * a location that was selected by the user, either through save as or through import/open).
	 * 
	 * See also {@link #canSaveProperty()}, {@link #save()}.
	 * @return returns true if the file can be saved, false otherwise
	 */
	public default boolean canSave() {
		return canSaveProperty().get();
	}

	/**
	 * Indicates if this editor can be saved at its current location (it has
	 * a location that was selected by the user, either through save as or through import/open).
	 * 
	 * See also {@link #save()}.
	 * 
	 * @return returns a boolean property
	 */
	public ReadOnlyBooleanProperty canSaveProperty();

	/**
	 * Saves the file to the current location.
	 * @throws IllegalStateException if no location has been set by the user through import, open 
	 * or save as
	 */
	public void save();

	/**
	 * Saves the file to the specified location.
	 * @param f the file to save to
	 * @throws IOException if the file could not be saved
	 */
	public void saveAs(File f) throws IOException;

	/**
	 * Returns true if this file can be exported.
	 * 
	 * See also {@link #canExportProperty()}.
	 * 
	 * @return true if the file can be exported, false otherwise
	 */
	public default boolean canExport() {
		return canExportProperty().get();
	}
	
	/**
	 * Indicates if the file in this editor can be exported.
	 * 
	 * See also {@link #export(File)}.
	 * 
	 * @return returns a boolean property
	 */
	public ReadOnlyBooleanProperty canExportProperty();

	/**
	 * Exports the file.
	 * @param f the file to export
	 * @throws IOException if the file could not be exported
	 * @throws UnsupportedOperationException if the operation is not supported
	 */
	public void export(File f) throws IOException;

	/**
	 * Informs the controller that it should stop all activity and release any
	 * resources.
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

	/**
	 * Indicates the url for the book to be used when accessing the contents externally.
	 * 
	 * @return returns a string property, the string may be null
	 */
	public ReadOnlyStringProperty urlProperty();
	
	/**
	 * Gets a list of extension filters for the purpose of saving.
	 * 
	 * @return returns a list of available extensions
	 */
	public List<ExtensionFilter> getSaveAsFilters();

	/**
	 * Reloads the contents of this editor.
	 */
	public void reload();

	/**
	 * Returns true if this editor can emboss.
	 * @return true if the editor can emboss, false otherwise
	 */
	public default boolean canEmboss() {
		return canEmbossProperty().get();
	}
	
	/**
	 * Indicates if the editor can emboss or not.
	 * 
	 * See also {@link #showEmbossDialog()}.
	 * 
	 * @return a boolean property
	 */
	public ReadOnlyBooleanProperty canEmbossProperty();

	/**
	 * Shows the emboss dialog.
	 * @throws UnsupportedOperationException if embossing isn't supported
	 */
	public void showEmbossDialog();

	/**
	 * Returns true if this editor can be toggled, false otherwise.
	 * 
	 * See {@link #canEditProperty()}, {@link #toggleView()}.
	 * 
	 * @return returns true if this editor can be toggled, false otherwise
	 */
	public default boolean isEditable() {
		return canEditProperty().get();
	}

	/**
	 * Indicates that this editor has both a source and a preview.
	 * @return returns a boolean property
	 */
	//TODO: this is not what it means actually...
	public default ReadOnlyBooleanProperty canEditProperty() {
		return new SimpleBooleanProperty(false);
	}

	/**
	 * Toggles between the source and result views. If there are no
	 * views to toggle, nothing happens.
	 */
	public default void toggleView() { }
	
	/**
	 * Gets the options of this editor.
	 * @return returns the options.
	 */
	//TODO: this doesn't make much sense from an interface perspective
	public Map<String, Object> getOptions();
	
	/**
	 * Returns true if this editor has unsaved changes, false otherwise.
	 * 
	 * See also {@link #modifiedProperty()}
	 * @return true if the editor has unsaved changes, false otherwise
	 */
	public default boolean isModified() {
		return modifiedProperty().get();
	}
	
	/**
	 * Indicates if the editor has unsaved changes or not.
	 * @return returns a boolean property
	 */
	public default ReadOnlyBooleanProperty modifiedProperty() {
		return new SimpleBooleanProperty(false);
	}

}
