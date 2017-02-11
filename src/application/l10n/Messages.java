package application.l10n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum Messages {
	ABOUT_WINDOW_TITLE("about-window-title"),
	APPLICATION_TITLE("application-title"),
	APPLICATION_DESCRIPTION("application-description"),
	APPLICATION_VERSION("application-version"),
	APPLICATION_ABOUT_PEF("application-about-pef"),
	BUTTON_OK("button-ok")
	;
	
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
	private final String key;

	private Messages(String key) {
		this.key = key;
	}

	public String localize() {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}		
	}
	
	public String localize(Object ... arguments) {
		return MessageFormat.format(localize(), arguments);
	}

}
