package com.googlecode.e2u.l10n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum Messages {
	ABOUT_THE_BOOK("Worker.heading-about-book"),
	UNKNOWN_TITLE("Worker.unknown-title"),
	UNKNOWN_AUTHOR("Worker.unknown-author"),
	SIZE("Worker.size"),
	SIZE_PAGES("Worker.size-format"),
	SIZE_SHEETS("Worker.size-sheets"),
	SIZE_VOLUMES("Worker.size-volumes"),
	DIMENSIONS("Worker.dimensions"),
	EIGHT_DOT("Worker.eight-dot"),
	DUPLEX("Worker.duplex"),
	DUPLEX_NO("Worker.duplex-no"),
	DUPLEX_YES("Worker.duplex-yes"),
	DUPLEX_MIXED("Worker.duplex-mixed"),
	YES("Worker.yes"),
	NO("Worker.no"),
	FILE_DIMENSIONS("Worker.file-dimensions"),
	MENU_ABOUT_BOOK("Worker.menu-about-book"),
	PREVIEW_VIEW("Worker.preview-view"),
	XSLT_TOGGLE_VIEW("xslt.toggle-view-label"),
	XSLT_SHOWING_PAGES("xslt.showing-pages"),
	XSLT_ABOUT_LABEL("xslt.about-label"),
	XSLT_VIEW_SOURCE("xslt.view-source"),
	XSLT_VOLUME_LABEL("xslt.volume-label"),
	XSLT_SECTION_LABEL("xslt.section-label"),
	XSLT_PAGE_LABEL("xslt.page-label"),
	XSLT_SHEETS_LABEL("xslt.sheets-label"),
	XSLT_GO_TO_PAGE_LABEL("xslt.go-to-page-label"),
	VALIDATION_ISSUES("validation-issues");

	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
	private final String key;

	private Messages(String key) {
		this.key = key;
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Localizes the message without any variables.
	 * @return returns the localized message
	 */
	public String localize() {
		return getString(key);
	}
	
	/**
	 * Localizes the message with the specified variables.
	 * @param variables the variables to insert into the localized message
	 * @return returns the localized message
	 */
	public String localize(Object ... variables) {
		return MessageFormat.format(localize(), variables);
	}
}
