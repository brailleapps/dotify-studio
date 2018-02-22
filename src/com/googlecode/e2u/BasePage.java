package com.googlecode.e2u;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.ajui.Content;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public abstract class BasePage implements Content {

	public BasePage() { }

	protected String buildHTML(XHTMLTagger content, String title, boolean footer) {
		return buildHTML(content, title, footer, true);
	}
	
	protected abstract Map<String, String> getBodyAttributes();
	protected abstract List<String> getStylePaths();
	protected abstract List<String> getScriptPaths();

	protected String buildHTML(XHTMLTagger content, String title, boolean footer, boolean header) {
		XHTMLTagger sb = new XHTMLTagger();
		sb.start("html").attr("xmlns", "http://www.w3.org/1999/xhtml")
		.start("head")
			.start("meta").attr("http-equiv", "content-type").attr("content", "text/html; charset=UTF-8").end()
			.start("title").text(title).end();
		for (String style : getStylePaths()) {
			sb.start("link").attr("rel", "stylesheet").attr("type", "text/css").attr("href", style).end();
		}
		for (String script : getScriptPaths()) {
			sb.start("script").attr("src", script).end();
		}
		sb.end();
		sb.start("body");
		for (Entry<String, String> entry : getBodyAttributes().entrySet()) {
			sb.attr(entry.getKey(), entry.getValue());
		}
    	//Header
		if (header) {
			sb.start("div").attr("id", "view");
			sb.start("form").attr("action", "#").attr("method", "get").start("p")
				.start("span").attr("id", "item-preview")
					.start("a").attr("href", "view.html").text(Messages.getString(L10nKeys.PREVIEW_VIEW)).end()
				.end()
				.start("span")
					.start("a").attr("href", "index.html?method=meta").text(Messages.getString(L10nKeys.MENU_ABOUT_BOOK)).end()
				.end()
				.start("input").attr("id", "connected").attr("type", "submit").attr("value", "").attr("title", "Avsluta").attr("disabled", "disabled").end()
				.start("input").attr("id", "notConnected").attr("type", "submit").attr("value", "").attr("title", "Avsluta").attr("disabled", "disabled").end()
			.end().end().end();
			sb.start("div").attr("id", "top-nav").start("p").end().end();
		}
		sb.start("div").attr("id", "main").start("div").attr("id", "content");
    	if (title!=null && !"".equals(title)) {
    		sb.tag("h1", title)
    				//tag("h1", title)
    			; //$NON-NLS-1$
    	}
		if (content!=null) {
			sb.insert(content);
		}
    	sb.end().end().start("div").attr("id", "bottom-bar").end();
    	
    	//Footer
    	sb.insert(
    			new XHTMLTagger()
    	);
    	sb.end();
    	sb.end();
    	return sb.getResult();
    }
}
