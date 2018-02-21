package com.googlecode.e2u;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;
import com.googlecode.e2u.preview.stax.BookReaderResult;

import shared.BuildInfo;

public class MainPage extends BasePage implements AListener {
	public final static String ENCODING = "utf-8";
	private BookViewController bookController;

	public MainPage(File f) {
		buildMenu();
		bookController = new BookViewController(f);

	}

	public void buildMenu() {
	}

	public Optional<URI> getBookURI() {
		if (bookController!=null) {
			return Optional.of(bookController.getBookURI());
		} else {
			return Optional.<URI>empty();
		}
	}

	public Optional<BookReaderResult> getBookReaderResult() {
		if (bookController!=null) {
			return Optional.ofNullable(bookController.getBookReaderResult());
		} else {
			return Optional.<BookReaderResult>empty();
		}
	}

	private Reader previewReader(String key, Context context) {
		String volume = context.getArgs().get("volume");
		int v = 1;
		try {
			v = Integer.parseInt(volume);
		} catch (NumberFormatException e) {

		}
		if (v<1) {v=1;}
		return bookController.getPreviewView().getReader(v);
	}

	@Override
	public Reader getContent(String key, Context context) throws IOException {
		if ("book".equals(key)) {
			return new InputStreamReader(bookController.getBookURI().toURL().openStream(), bookController.getBook().getInputEncoding());
		} else if ("preview-new".equals(key)) {
			//TODO: this is the same as the default
			return previewReader(key, context);
		} else if ("meta".equals(context.getArgs().get("method"))) {
			return new StringReader(buildHTML(renderView(context, bookController.getAboutBookView()), Messages.getString(L10nKeys.ABOUT_THE_BOOK), true));
		} else {
			return previewReader(key, context);
		}
	}

	private XHTMLTagger renderView(Context context, AContainer subview) {
		return subview.getHTML(context);
	}

	@Override
	public void close() {
		bookController.close();
	}

	@Override
	public synchronized void changeHappened(Object o) {
	}

	@Override
	public String toString() {
		return BuildInfo.NAME + " " + BuildInfo.VERSION + ", " + BuildInfo.BUILD;
	}

	@Override
	protected Map<String, String> getBodyAttributes() {
		HashMap<String, String> bodyAtts = new HashMap<>();
		bodyAtts.put("onload", "get('ping.xml?updates=true'+getUpdateString())");
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
		scripts.add("script/index.js");
		return scripts;
	}

}
