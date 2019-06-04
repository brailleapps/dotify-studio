package application.common;

/**
 * Provides a place to put feature switches, so that it is easy to find a list of 
 * features that can be toggled.
 *  
 * @author Joel Håkansson
 */
public enum FeatureSwitch {
	/**
	 * Defines if embossing is enabled or not.
	 */
	EMBOSSING("on".equalsIgnoreCase(System.getProperty("application.feature.embossing", "on"))),
	/**
	 * Defines if opening of other file types than PEF are enabled.
	 */
	OPEN_OTHER_TYPES("on".equalsIgnoreCase(System.getProperty("application.feature.open-other-types", "on"))),
	/**
	 * Defines if it should be possible to select output format
	 */
	SELECT_OUTPUT_FORMAT("on".equalsIgnoreCase(System.getProperty("application.feature.select-output-format", "on"))),
	/**
	 * When on, the progress indicator in the Dotify panel uses progress values reported from the task system   
	 */
	REPORT_PROGRESS("on".equalsIgnoreCase(System.getProperty("application.feature.report-progress", "on"))),
	/**
	 * When on, autosave is available in settings.
	 */
	AUTOSAVE("on".equalsIgnoreCase(System.getProperty("application.feature.autosave", "on"))),
	/**
	 * When on, a character tool is available in the menu.
	 */
	CHARACTER_TOOL("on".equalsIgnoreCase(System.getProperty("application.feature.character-tool", "on"))),
	/**
	 * When on, file sets are processed
	 */
	PROCESS_FILE_SET("on".equalsIgnoreCase(System.getProperty("application.feature.file-set", "off"))),
	/**
	 * When on, import allows importing of the target format, if some enhancing tasks are available 
	 * for the target format.
	 */
	IMPORT_TARGET_FORMAT("on".equalsIgnoreCase(System.getProperty("application.feature.import-target-format", "off")))
	;

	private final boolean on;
	FeatureSwitch(boolean on) {
		this.on = on;
	}

	/**
	 * Returns true if the feature is on, false otherwise.
	 * @return true if the feature is on, false otherwise
	 */
	public boolean isOn() {
		return on;
	}
}
