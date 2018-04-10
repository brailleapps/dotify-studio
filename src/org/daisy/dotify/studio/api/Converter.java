package org.daisy.dotify.studio.api;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * Provides an interface for a converter.
 * @author Joel Håkansson
 */
public interface Converter {

	/**
	 * Lets the user select a settings template and the runs the converter.
	 * Must be called from the JavaFX Application Thread.
	 */
	public void selectTemplateAndApply();

	/**
	 * Runs the converter using the current state. The call is non-blocking
	 * and may be called from the JavaFX Application Thread.
	 */
	public void apply();

	/**
	 * Saves the current settings as a template.
	 * Must be called from the JavaFX Application Thread.
	 */
	public void saveTemplate();

// Watch source property
	/**
	 * When true, the converter source file is watched for changes
	 * and automatically updated when changes occurs.
	 * @return a boolean property
	 */
	public BooleanProperty watchSourceProperty();

	/**
	 * Gets the value of the watch source property, see {@link #watchSourceProperty()}.
	 * @return true if the source is watched, false otherwise
	 */
	public boolean getWatchSource();

	/**
	 * Sets the watch source property, see {@link #watchSourceProperty()}.
	 * @param value the watch source property value
	 */
	public void setWatchSource(boolean value);
	
// Show options property
	/**
	 * When true, the converter options are displayed.
	 * @return a boolean property
	 */
	public BooleanProperty showOptionsProperty();

	/**
	 * Gets the value of the show options property, see {@link #showOptionsProperty()}.
	 * @return true if the options are shown, false otherwise
	 */
	public boolean getShowOptions();

	/**
	 * Sets the show options property, see {@link #showOptionsProperty()}.
	 * @param value the show options property value
	 */
	public void setShowOptions(boolean value);

// Is idle property
	/**
	 * When true, the converter isn't currently running. To run, use {@link #apply()} or {@link #selectTemplateAndApply()}.
	 * @return a boolean property
	 */
	public ReadOnlyBooleanProperty isIdleProperty();

	/**
	 * Gets the value of the is idle property, see {@link #isIdleProperty()}. 
	 * @return true if the converter is idle, false otherwise
	 */
	public boolean getIsIdle();

}
