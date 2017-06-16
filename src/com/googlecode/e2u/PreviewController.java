package com.googlecode.e2u;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.daisy.braille.api.table.BrailleConstants;
import org.daisy.braille.api.table.BrailleConverter;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.consumer.table.TableCatalog;
import org.daisy.braille.impl.table.DefaultTableProvider;

import com.googlecode.ajui.Context;
import com.googlecode.e2u.BookReader.BookReaderResult;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

import shared.Settings;
import shared.Settings.Keys;

public class PreviewController {
	private static final Logger logger = Logger.getLogger(PreviewController.class.getCanonicalName());
	private final BookReader r;
	private Map<Integer, PreviewRenderer> done;
	private final Settings settings;
	private boolean saxonNotAvailable;
	private String brailleFont, textFont, charset;
	private long lastUpdated;

	/**
	 * 
	 * @param r
	 * 
	 */
	public PreviewController(final BookReader r, Settings settings) {
		this.settings = settings;
		this.r = r;
		done = Collections.synchronizedMap(new HashMap<Integer, PreviewRenderer>());
		update(false);
		brailleFont = settings.getString(Keys.brailleFont);
		textFont = settings.getString(Keys.textFont);
		charset = settings.getString(Keys.charset);
	}
	
	private void update(boolean force) {
		saxonNotAvailable = false;
		if (!force && lastUpdated+10000>System.currentTimeMillis()) {
			return;
		}
		lastUpdated = System.currentTimeMillis();
		try {
			BookReaderResult brr = r.getResult();
			Map<String, String> params = buildParams(settings, "view.html", settings.getString(Keys.charset), "book.xml", null);
			for (int i=1; i<=r.getResult().getBook().getVolumes(); i++) {
				PreviewRenderer pr = done.remove(i);
				if (pr!=null) {
					pr.abort();
					if (pr.getFile()!=null) {
						logger.fine("Removing old renderer");
						pr.getFile().delete();
					}
				}
		        done.put(i, new PreviewRenderer(brr.getURI(), i, this, params));
			}
		} catch (IllegalArgumentException iae) { 
			saxonNotAvailable = true;
		}
	}
	
	public synchronized boolean myTurn(int vol) {
		for (Integer i : done.keySet()) {
			if (!done.get(i).isDone()) {
				return vol == i;
			}
		}
		return true;
	}
	
	private boolean settingsChanged() {
		String brailleFont = settings.getString(Keys.brailleFont);
		String textFont = settings.getString(Keys.textFont);
		String charset = settings.getString(Keys.charset);
		boolean changed = 
			(this.brailleFont!=null && !this.brailleFont.equals(brailleFont)) ||
			(this.textFont!=null && !this.textFont.equals(textFont)) ||
			(this.charset!=null && !this.charset.equals(charset));
		this.brailleFont = brailleFont;
		this.textFont = textFont;
		this.charset = charset;
		return changed;
	}
	
	private boolean fileChanged() {
		if (r.getResult().getBookFile()==null) {
			return false;
		} else {
			return lastUpdated<r.getResult().getBookFile().lastModified();
		}
	}

	public Reader getReader(int vol) {
		if (saxonNotAvailable) {
			return new StringReader("Failed to read");
		}
		try {
			boolean fileChanged = fileChanged();
			if (settingsChanged() || fileChanged) {
				update(fileChanged);
			}
			return new InputStreamReader(new FileInputStream(done.get(vol).getFile()), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			return new StringReader("Failed to read");
		}
	}

	private static Map<String, String> buildParams(Settings settings, String target, String charset, String file, String volume) {
		HashMap<String, String> params = new HashMap<>();
		if (file!=null) {
			Table table = null;
			if (charset!=null) { 
				table = TableCatalog.newInstance().get(charset);
			}
			if (table==null) {
				table = TableCatalog.newInstance().get(DefaultTableProvider.class.getCanonicalName()+".TableType.EN_US");
				settings.getSetPref(Keys.charset, table.getIdentifier());
			}
			params.put("uri", file);
			if (volume!=null && !"".equals(volume)) {
				params.put("volume", volume);
			} else {
				params.put("volume", "1");
			}
			BrailleConverter as = table.newBrailleConverter();
			if (as.supportsEightDot()) {
				params.put("code", as.toText(BrailleConstants.BRAILLE_PATTERNS_256));
			} else {
				params.put("code", as.toText(BrailleConstants.BRAILLE_PATTERNS_64));
			}
			params.put("toggle-view-label", Messages.getString(L10nKeys.XSLT_TOGGLE_VIEW));
			params.put("return-label", Messages.getString(L10nKeys.XSLT_RETURN_LABEL));
			params.put("emboss-view-label", Messages.getString(L10nKeys.EMBOSS_VIEW));
			params.put("preview-view-label", Messages.getString(L10nKeys.PREVIEW_VIEW));
			params.put("find-view-label", Messages.getString(L10nKeys.MENU_OPEN));
			params.put("setup-view-label", Messages.getString(L10nKeys.MENU_SETUP));
			params.put("about-software-label", Messages.getString(L10nKeys.MENU_ABOUT_SOFTWARE));
			params.put("unknown-author-label", Messages.getString(L10nKeys.UNKNOWN_AUTHOR));
			params.put("unknown-title-label", Messages.getString(L10nKeys.UNKNOWN_TITLE));
			params.put("showing-pages-label", Messages.getString(L10nKeys.XSLT_SHOWING_PAGES));
			params.put("about-label", Messages.getString(L10nKeys.XSLT_ABOUT_LABEL));
			params.put("show-source", Messages.getString(L10nKeys.XSLT_VIEW_SOURCE));
			params.put("volume-label", Messages.getString(L10nKeys.XSLT_VOLUME_LABEL));
			params.put("section-label", Messages.getString(L10nKeys.XSLT_SECTION_LABEL));
			params.put("page-label", Messages.getString(L10nKeys.XSLT_PAGE_LABEL));
			params.put("sheets-label", Messages.getString(L10nKeys.XSLT_SHEETS_LABEL));
			params.put("viewing-label", Messages.getString(L10nKeys.XSLT_VIEWING_LABEL));
			params.put("go-to-page-label", Messages.getString(L10nKeys.XSLT_GO_TO_PAGE_LABEL));
			params.put("uri-string", target + "?file=" + file + "&charset=" + charset);
			try {
				params.put("brailleFont", URLDecoder.decode(settings.getString(Keys.brailleFont, ""), MainPage.ENCODING));
				params.put("textFont", URLDecoder.decode(settings.getString(Keys.textFont, ""), MainPage.ENCODING));
			} catch (UnsupportedEncodingException e) {
				// should never happen if encoding is UTF-8
			}
		}
		return params;
	}
}