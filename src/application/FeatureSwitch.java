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
	EDITOR("on".equalsIgnoreCase(System.getProperty("application.feature.editor", "on")));

	private final boolean on;
	FeatureSwitch(boolean on) {
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}
}
