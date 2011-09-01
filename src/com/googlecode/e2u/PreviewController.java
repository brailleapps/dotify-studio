package com.googlecode.e2u;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerFactory;

import org.daisy.braille.BrailleConstants;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.DefaultTableProvider;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableCatalog;


import com.googlecode.ajui.Context;
import com.googlecode.e2u.BookReader.BookReaderResult;
import com.googlecode.e2u.Settings.Keys;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class PreviewController {
	private final BookReader r;
	private HashMap<Integer, PreviewRenderer> done;
	private final Settings settings;
	private boolean saxonNotAvailable;
	private String brailleFont, textFont, charset;

	/**
	 * 
	 * @param r
	 * 
	 */
	public PreviewController(final BookReader r, Settings settings) {
		this.settings = settings;
		this.r = r;
		init();
		brailleFont = settings.getString(Keys.brailleFont);
		textFont = settings.getString(Keys.textFont);
		charset = settings.getString(Keys.charset);
	}
	
	private void init() {
		final TransformerFactory factory = TransformerFactory.newInstance();
		saxonNotAvailable = false;
		done = new HashMap<Integer, PreviewRenderer>();
		try {
			factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
			BookReaderResult brr = r.getResult();
			Map<String, String> params = buildParams(settings, "view2.xml", settings.getString(Keys.charset), "book.xml", null);
			for (int i=1; i<=r.getResult().getBook().getVolumes(); i++) {
		        done.put(i, new PreviewRenderer(brr.getURI(), i, factory, this, params));
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
		boolean changed = brailleFont==null || textFont==null || charset==null || 
			!(this.brailleFont.equals(brailleFont) && this.textFont.equals(textFont) && this.charset.equals(charset));
		this.brailleFont = brailleFont;
		this.textFont = textFont;
		this.charset = charset;
		return changed;
	}

	public Reader getReader(int vol) {
		if (saxonNotAvailable) {
			return new StringReader("Failed to read");
		}
		try {
			if (settingsChanged()) {
				init();
			}
			return new InputStreamReader(new FileInputStream(done.get(vol).getFile()));
		} catch (FileNotFoundException e) {
			return new StringReader("Failed to read");
		}
	}
	
	public static Map<String, String> buildParamsFromContext(Context context, Settings settings) {
		HashMap<String, String> args = context.getArgs();
		String file = args.get("file");

		String charset = args.get("charset");
		String volume = args.get("volume");
		if (file==null || "".equals(file)) {
			file = "book.xml";
		}

		if (charset==null || "".equals(charset)) {
			charset = settings.getString(Keys.charset,
					DefaultTableProvider.class.getCanonicalName()+".TableType.EN_US");
		}
		
		Map<String, String> params = buildParams(settings, context.getTarget(), charset, file, volume);
		return params;
	}
	
	public static Map<String, String> buildParams(Settings settings, String target, String charset, String file, String volume) {
		HashMap<String, String> params = new HashMap<String, String>();
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