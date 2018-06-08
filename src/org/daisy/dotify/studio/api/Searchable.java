package org.daisy.dotify.studio.api;

import javafx.beans.value.ObservableObjectValue;

/**
 * Provides support for find/replace functionality.
 * @author Joel Håkansson
 */
public interface Searchable {
	
	/**
	 * Returns the search capabilities for the implementation.
	 * @return the search capabilities
	 */
	public ObservableObjectValue<SearchCapabilities> searchCapabilities();

	/**
	 * Finds the next match starting from the last match or, if non is available, the
	 * caret position.
	 * @param text the text to find
	 * @param opts search options, options that are not supported will be ignored
	 * @return true if a match was found, false otherwise
	 */
	public boolean findNext(String text, SearchOptions opts);
	
	/**
	 * Replaces the selected text with the replacement text.
	 * @param replace the replacement text
	 */
	public void replace(String replace);
	
	/**
	 * Gets the selected text, or an empty string if no text is selected.
	 * @return the selected text
	 */
	public String getSelectedText();

}
