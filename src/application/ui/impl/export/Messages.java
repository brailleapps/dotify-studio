package application.ui.impl.export;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides messages available for localization.
 * @author Joel Håkansson
 */
enum Messages {
	DIALOG_TITLE_EXPORT_TO_TEXT("dialog-title-export-to-text"),
	DIALOG_TITLE_SPLIT_PEF("dialog-title-split-pef"),
	DIALOG_TITLE_EXPORT_OPTIONS("dialog-title-export-options"),
	LABEL_BRAILLE_TABLE("label-braille-table"),
	LABEL_FOLDER_NOT_EMPTY("label-folder-not-empty"),
	MENU_ITEM_EXPORT_TO_TEXT("menu-item-export-to-text"),
	MENU_ITEM_SPLIT_PEF("menu-item-split-pef"),
	MESSAGE_CONFIRM_OVERWRITE("message-confirm-overwrite"),
	MESSAGE_SELECT_BRAILLE_TABLE("message-select-braille-table")
	;
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(Messages.class.getCanonicalName(), Locale.getDefault());
	private final String key;

	private Messages(String key) {
		this.key = key;
	}
	
	String getKey() {
		return key;
	}

	/**
	 * Localizes the message without any variables.
	 * @return returns the localized message
	 */
	String localize() {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Localizes the message with the specified variables.
	 * @param variables the variables to insert into the localized message
	 * @return returns the localized message
	 */
	String localize(Object ... variables) {
		return MessageFormat.format(localize(), variables);
	}

}
