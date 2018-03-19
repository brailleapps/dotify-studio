package org.daisy.dotify.studio.api;

import java.util.List;
import java.util.Optional;

import org.daisy.streamline.api.media.FileDetails;

/**
 * Provides an export action for a GUI. An implementation may show dialogs as a part of the export.
 * The provider supports one or more actions for one or more formats. Every action must be
 * supported for every supported format.
 * 
 * @author Joel Håkansson
 */
public interface ExportActionProvider {
	
	/**
	 * Lists all provided export actions.
	 * @return returns a list of export actions
	 */
	public List<ExportActionDescription> listActions();
	
	/**
	 * Returns true if the specified format is supported, false otherwise
	 * @param format the format to test
	 * @return true if the specified format is supported, false otherwise
	 */
	public boolean supportsFormat(FileDetails format);
	
	/**
	 * Returns true if the action with the specified id is supported, false otherwise
	 * @param id the id to test
	 * @return true if the id is supported, false otherwise
	 */
	public boolean supportsAction(String id);
	
	/**
	 * Creates a new export action.
	 * @param id the id of the export action to create
	 * @return a new export action
	 */
	public Optional<ExportAction> newExportAction(String id);

}
