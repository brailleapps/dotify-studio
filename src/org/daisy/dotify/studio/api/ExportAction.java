package org.daisy.dotify.studio.api;

import java.io.File;

import javafx.stage.Window;

/**
 * Provides an export action.
 * @author Joel Håkansson
 */
public interface ExportAction {

	/**
	 * Exports the file.
	 * @param ownerWindow the owner window for dialogs
	 * @param source the file to perform the export action on
	 */
	public void export(Window ownerWindow, File source);
}
