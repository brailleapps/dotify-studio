package application;

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
	 * Defines if editors are enabled or not. 
	 */
	EDITOR("on".equalsIgnoreCase(System.getProperty("application.feature.editor", "on"))),
	/**
	 * Defines if opening of other file types than PEF are enabled.
	 */
	OPEN_OTHER_TYPES("on".equalsIgnoreCase(System.getProperty("application.feature.open-other-types", "off"))),
	/**
	 * Defines if the output format should be HTML (instead of PEF)
	 */
	HTML_OUTPUT_FORMAT("on".equalsIgnoreCase(System.getProperty("application.feature.html-output-format", "off")));

	private final boolean on;
	FeatureSwitch(boolean on) {
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}
}
