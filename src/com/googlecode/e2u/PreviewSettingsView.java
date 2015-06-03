package com.googlecode.e2u;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.daisy.braille.BrailleConstants;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableCatalog;


import org.daisy.factory.FactoryProperties;

import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.Settings.Keys;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class PreviewSettingsView extends AbstractSettingsView {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5885578735323031586L;
	private final static Map<String, String> brailleFonts;
	private final static Map<String, String> allFonts;
	static {
    	brailleFonts = new TreeMap<String, String>();
    	allFonts = new TreeMap<String, String>();
    	brailleFonts.put("", Messages.getString(L10nKeys.USE_DEFAULT));
    	allFonts.put("", Messages.getString(L10nKeys.USE_DEFAULT));
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (String f : ge.getAvailableFontFamilyNames()) {
			allFonts.put(f, f);
			Font font = Font.decode(f);
			int inx = font.canDisplayUpTo(BrailleConstants.BRAILLE_PATTERNS_256);
			if (inx==-1) {
				brailleFonts.put(f, f);
			} else if (inx>=64) {
				brailleFonts.put(f, f + " ("+Messages.getString(L10nKeys.SIX_DOT_ONLY)+")");
			}
		}
	}

	private SelectComponent textFontSelect;
	private SelectComponent brailleFontSelect;
	private SelectComponent charsetSelect;
	
	private final Settings settings;
	private final boolean hasBrailleFonts;
    private final static String previewSetupTarget = "setup-preview";

	
	public PreviewSettingsView(Settings settings, MenuSystem menu) {
		this.settings = settings;
    	textFontSelect = new SelectComponent(allFonts, "textFont", Messages.getString(L10nKeys.PREVIEW_TEXT_FONT), false, previewSetupTarget);
    	brailleFontSelect = new SelectComponent(brailleFonts, "brailleFont", Messages.getString(L10nKeys.PREIVEW_BRAILLE_FONT), false, previewSetupTarget);
    	{
	    	Collection<FactoryProperties> tc = TableCatalog.newInstance().list();
	    	charsetSelect = new SelectComponent(tc, "charset", null, Messages.getString(L10nKeys.PREVIEW_CHARSET), true, previewSetupTarget);
    	}
		setClass("group");
		if (menu!=null) {
			add(menu);
		}
		add(charsetSelect);
		if (brailleFonts.size()>0) {
			add(brailleFontSelect);
			hasBrailleFonts = true;
		} else {
			hasBrailleFonts = false;
			AParagraph p = new AParagraph();
			p.add(new ALabel(Messages.getString(L10nKeys.NO_BRAILLE_FONT)));
			add(p);
		}
		add(textFontSelect);
	}
	
	@Override
	public XHTMLTagger getHTML(Context context) {
		//update settings
    	String brailleFont = settings.getSetPref(Keys.brailleFont, context.getArgs().get("brailleFont"));
    	String textFont = settings.getSetPref(Keys.textFont, context.getArgs().get("textFont"));
    	String charset = settings.getSetPref(Keys.charset, context.getArgs().get("charset"));
    	
    	//update view
    	select(charsetSelect, charset);
    	select(textFontSelect, textFont);
    	if (hasBrailleFonts) {
    		select(brailleFontSelect, brailleFont);
    	}

		return super.getHTML(context);
		
	}
}
