package com.googlecode.e2u;




import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class ClosePage extends BasePage {

	private String getContentString(String key, Context context) {
		String ret = buildHTML(closeHTML(), Messages.getString(L10nKeys.CLOSED), true, false);
		context.close();
		return ret; 
	}

    private XHTMLTagger closeHTML() {
    	return new XHTMLTagger().tag("p", Messages.getString(L10nKeys.TOOLTIP_CLOSED)); //$NON-NLS-1$
    }

	@Override
	public void close() {
		// Nothing to do
	}

	@Override
	protected Map<String, String> getBodyAttributes() {
		HashMap<String, String> bodyAtts = new HashMap<>();
		bodyAtts.put("onload", "get()");
		bodyAtts.put("class", "ui");
		return bodyAtts;
	}

	@Override
	protected List<String> getStylePaths() {
		List<String> styles = new ArrayList<>();
		styles.add("styles/default/base.css");
		styles.add("styles/default/theme.css");
		styles.add("styles/default/layout.css");
		styles.add("styles/default/state.css");
		return styles;
	}

	@Override
	protected List<String> getScriptPaths() {
		List<String> scripts = new ArrayList<>();
		scripts.add("script/close.js");
		return scripts;
	}

	@Override
	public Reader getContent(String key, Context context) throws IOException {
		return new StringReader(getContentString(key, context));
	}

}
