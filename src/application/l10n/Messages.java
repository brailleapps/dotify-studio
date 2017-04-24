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
	BUTTON_OK("button-ok"),
	BUTTON_SELECT("button-select"),
	EMBOSS_WINDOW_TITLE("emboss-window-title"),
	/**
	 * The file is invalid and cannot be embossed.
	 */
	ERROR_CANNOT_EMBOSS_INVALID_FILE("error-cannot-emboss-invalid-file"),
	/**
	 * The page range could not be parsed.
	 * <ul>
	 * <li>{0} = the input</li>
	 * </ul>
	 */
	ERROR_FAILED_TO_PARSE_PAGE_RANGE("error-failed-to-parse-page-range"),
	/**
	 * The volume number could not be parsed.
	 * <ul>
	 * <li>{0} = the input</li>
	 * </ul>
	 */
	ERROR_FAILED_TO_PARSE_VOLUME_NUMBER("error-failed-to-parse-volume-number"),
	/**
	 * The page range field is empty.
	 */
	ERROR_EMPTY_PAGE_RANGE("error-empty-page-range"),
	/**
	 * The volume number field is empty.
	 */
	ERROR_EMPTY_VOLUME_NUMBER("error-empty-volume-number"),
	/**
	 * The volume number field is less than one.
	 */
	ERROR_VOLUME_NUMBER_LESS_THAN_ONE("error-volume-number-less-than-one"),
	/**
	 * The volume number is greater than the number of volumes. 
	 * <ul>
	 * <li>{0} = the input</li>
	 * <li>{1} = the number of volumes</li>
	 * </ul>
	 */
	ERROR_VOLUME_NUMBER_OUT_OF_RANGE("error-volume-number-out-of-range"),
	/**
	 * No device specified in preferences.
	 */
	ERROR_NO_DEVICE_SPECIFIED("error-no-device-specified"),
	/**
	 * The embosser settings are invalid.
	 */
	ERROR_INVALID_EMBOSSER_SETTINGS("error-invalid-embosser-settings"),
	/**
	 * No alignment specified in preferences.
	 */
	ERROR_NO_ALIGNMENT_SPECIFIED("error-no-alignment-specified"),
	TITLE_SET_SEARCH_FOLDER("title-set-search-folder"),
	TITLE_IMPORT_SOURCE_DOCUMENT_DIALOG("title-import-source-document-dialog"),
	TITLE_IMPORT_BRAILLE_OPTIONS_DIALOG("title-import-braille-options-dialog"),
	TITLE_IMPORT_BRAILLE_TEXT_DIALOG("title-import-braille-text-dialog"),
	TITLE_EXPORT_DIALOG("title-export-dialog"),
	TITLE_SAVE_AS_DIALOG("title-save-as-dialog"),
	TITLE_TEMPLATES_DIALOG("title-templates-dialog"),
	LABEL_BRAILLE_FONT("label-braille-font"),
	LABEL_TEXT_FONT("label-text-font"),
	LABEL_TRANSLATION("label-translation"),
	LABEL_DEVICE("label-device"),
	LABEL_EMBOSSER("label-embosser"),
	LABEL_PRINT_MODE("label-print-mode"),
	LABEL_TABLE("label-table"),
	LABEL_PAPER("label-paper"),
	LABEL_CUT_LENGTH("label-cut-length"),
	LABEL_ORIENTATION("label-orientation"),
	LABEL_Z_FOLDING("label-z-folding"),
	LABEL_ALIGNMENT("label-alignment"),
	LABEL_EDIT_ABOVE("label-edit-above"),
	LABEL_SETUP_VALID("label-setup-valid"),
	LABEL_SETUP_INVALID("label-setup-invalid"),
	LABEL_CREATE_TEST_DOCUMENT("label-create-test-document"),
	LABEL_PAPER_DIMENSIONS("label-paper-dimensions"),
	LABEL_NONE("label-none"),
	LABEL_HEIGHT("label-height"),
	LABEL_WIDTH("label-width"),
	LABEL_ROLL_SIZE("label-roll-size"),
	LABEL_SHEET_PAPER("label-sheet-paper"),
	LABEL_TRACTOR_PAPER("label-tractor-paper"),
	LABEL_ROLL_PAPER("label-roll-paper"),
	MESSAGE_SIX_DOT_ONLY("message-six-dot-only"),
	MESSAGE_SEARCH_RESULT("message-search-result"),
	MESSAGE_UNKNOWN_AUTHOR("message-unknown-author"),
	MESSAGE_UNKNOWN_TITLE("message-unknown-title"),
	MESSAGE_PAPER_DETAILS("message-paper-details"),
	MESSAGE_BOOK_DIMENSIONS("message-book-dimensions"),
	/**
	 * The file has been sent to the embosser
	 */
	MESSAGE_FILE_SENT_TO_EMBOSSER("message-file-sent-to-embosser"),
	PREFERENCES_MENU_ITEM("menu-item-preferences"),
	PREFERENCES_WINDOW_TITLE("preferences-window-title"),
	TAB_EMBOSS("tab-emboss"),
	TAB_PREVIEW("tab-preview"),
	TAB_SEARCH("tab-search"),
	VALUE_USE_DEFAULT("value-use-default"),
	EXTENSION_FILTER_SUPPORTED_FILES("extension-filter-supported-files"),
	EXTENSION_FILTER_ALL_FILES("extension-filter-all-files"),
	OPTION_VALUE_LANDSCAPE("option-value-landscape"),
	OPTION_VALUE_PORTRAIT("option-value-portrait"),
	OPTION_VALUE_SQUARE("option-value-square"),
	OPTION_VALUE_DEFAULT_ORIENTATION("option-value-default-orientation"),
	OPTION_VALUE_REVERSED_ORIENTATION("option-value-reversed-orientation"),
	OPTION_VALUE_PAGE_ALIGNMENT_LEFT("option-value-page-alignment-left"),
	OPTION_VALUE_PAGE_ALIGNMENT_CENTER("option-value-page-alignment-center"),
	OPTION_VALUE_PAGE_ALIGNMENT_RIGHT("option-value-page-alignment-right"),
	OPTION_VALUE_PAGE_ALIGNMENT_INNER("option-value-page-alignment-inner"),
	OPTION_VALUE_PAGE_ALIGNMENT_OUTER("option-value-page-alignment-outer"),
	OPTION_VALUE_REGULAR_PRINT_MODE("option-value-regular-print-mode"),
	OPTION_VALUE_MAGAZINE_PRINT_MODE("option-value-magazine-print-mode")
	;

	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
	private final String key;

	private Messages(String key) {
		this.key = key;
	}
	
	public static ResourceBundle getBundle() {
		return RESOURCE_BUNDLE;
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
