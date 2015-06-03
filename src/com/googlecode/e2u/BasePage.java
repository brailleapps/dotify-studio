package com.googlecode.e2u;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.googlecode.ajui.Content;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;

public abstract class BasePage implements Content {
    //final static String APP_PAGE = "index.html";
	final static String KEY_TITLE = "title";

	public BasePage() { }

	protected String buildHTML(String content, String title, boolean footer) {
		return buildHTML(content, title, footer, true);
	}
	
	protected String buildHTML(String content, String title, boolean footer, boolean header) {
		if (title==null) {
			title = "";
		}
		if (content==null) {
			content = "";
		}
    	StringBuffer sb = new StringBuffer();
    	if (!"".equals(title)) {
    		sb.append(
    				new XHTMLTagger().tag("h1", title).getResult()
    				//tag("h1", title)
    			); //$NON-NLS-1$
    	}
    	sb.append(content);
    	return sb.toString();
    }
	
	public abstract String getContentString(String key, Context context) throws IOException;

	public Reader getContent(String key, Context context) throws IOException {
		return new StringReader(getContentString(key, context));
	}
}
