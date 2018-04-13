package org.daisy.dotify.studio.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.daisy.streamline.api.media.FileDetails;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.Node;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * Provides an interface for editors.
 * @author Joel Håkansson
 */
public interface Editor {

	/**
	 * Indicates if this editor can be saved at its current location (it has
	 * a location that was selected by the user, either through save as or through import/open).
	 * 
	 * See also {@link #save()}.
	 * 
	 * @return an observable boolean value
	 */
	public ObservableBooleanValue canSave();

	/**
	 * Indicates if this editor can be saved to a new location.
	 * 
	 * See also {@link #saveAs(File)}.
	 * 
	 * @return an observable boolean value
	 */
	public ObservableBooleanValue canSaveAs();


	/**
	 * Saves the file to the current location.
	 * @throws IllegalStateException if no location has been set by the user through import, open 
	 * or save as
	 */
	public void save();

	/**
	 * Saves the file to the specified location.
	 * @param f the file to save to
	 * @return returns false if the save operation was cancelled
	 * @throws IOException if the file could not be saved
	 */
	public boolean saveAs(File f) throws IOException;

	/**
	 * Exports the file.
	 * @param ownerWindow the owner window
	 * @param action the export action
	 * @throws IOException if the file could not be exported
	 */
	public void export(Window ownerWindow, ExportAction action) throws IOException;
	
	/**
	 * Gets the file details for the file in the editor.
	 * @return returns the file details
	 */
	public ObservableObjectValue<FileDetails> fileDetails();

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
	 * Indicates if the editor can emboss or not.
	 * 
	 * See also {@link #showEmbossDialog()}.
	 * 
	 * @return an observable boolean value
	 */
	public ObservableBooleanValue canEmboss();

	/**
	 * Shows the emboss dialog.
	 * @throws UnsupportedOperationException if embossing isn't supported
	 */
	public void showEmbossDialog();

	/**
	 * Returns true if this editor has views that can be toggled, 
	 * false otherwise.
	 * 
	 * See {@link #toggleViewProperty()}, {@link #toggleView()}.
	 * 
	 * @return returns true if this editor can be toggled, false otherwise
	 */
	public default boolean canToggleView() {
		return toggleViewProperty().get();
	}

	/**
	 * Indicates that this editor has more than one view that can be toggled
	 * between.
	 * @return returns a boolean property
	 */
	public default ReadOnlyBooleanProperty toggleViewProperty() {
		return new SimpleBooleanProperty(false);
	}

	/**
	 * Toggles between the editor views. If there are no
	 * views to toggle, nothing happens.
	 */
	public default void toggleView() { }
	
	/**
	 * Splits/restores the editor views. If there is only one
	 * view, nothing happens.
	 */
	public default void toggleViewingMode() { }

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

	/**
	 * Gets the converter.
	 * @return the converter
	 */
	public default Optional<Converter> getConverter() {
		return Optional.empty();
	}
	
	public void activate();
	
	public Node getNode();

}
