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
	SELECT_OUTPUT_FORMAT("on".equalsIgnoreCase(System.getProperty("application.feature.select-output-format", "off"))),
	/**
	 * When on, the progress indicator in the Dotify panel uses progress values reported from the task system   
	 */
	REPORT_PROGRESS("on".equalsIgnoreCase(System.getProperty("application.feature.report-progress", "off")));

	private final boolean on;
	FeatureSwitch(boolean on) {
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}
}
