package application.common;

import java.util.Locale;

import javafx.scene.control.ComboBox;

/**
 * Provides a locale entry to be used in a {@link ComboBox}.
 * @author Joel Håkansson
 *
 */
public class LocaleEntry extends NiceName implements Comparable<LocaleEntry> {
	
	/**
	 * Creates an new instance with the specified locale.
	 * @param locale the locale
	 */
	public LocaleEntry(Locale locale) {
		super(locale.toLanguageTag(), locale.getDisplayName());
	}
	
	@Override
	public int compareTo(LocaleEntry o) {
		return getDisplayName().compareTo(o.getDisplayName());
	}
}