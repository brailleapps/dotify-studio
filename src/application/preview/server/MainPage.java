package application.preview.server;

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
import java.util.Map.Entry;
import java.util.Optional;

import com.googlecode.ajui.Content;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;

import application.l10n.Messages;
import application.preview.server.preview.stax.BookReaderResult;
import shared.BuildInfo;

public class MainPage implements Content {
	public final static String ENCODING = "utf-8";
	private final BookViewController bookController;

	public MainPage(File f) {
		bookController = new BookViewController(f);
	}

	public Optional<URI> getBookURI() {
		return Optional.of(bookController.getBookURI());
	}

	public Optional<BookReaderResult> getBookReaderResult() {
		return Optional.ofNullable(bookController.getBookReaderResult());
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
			return new StringReader(buildHTML(bookController.getAboutBookView().getHTML(context), Messages.ABOUT_THE_BOOK.localize(), true));
		} else {
			return previewReader(key, context);
		}
	}

	@Override
	public void close() {
		bookController.close();
	}

	@Override
	public String toString() {
		return BuildInfo.NAME + " " + BuildInfo.VERSION + ", " + BuildInfo.BUILD;
	}

	private Map<String, String> getBodyAttributes() {
		HashMap<String, String> bodyAtts = new HashMap<>();
		bodyAtts.put("onload", "get('ping.xml?updates=true'+getUpdateString())");
		bodyAtts.put("class", "ui");
		return bodyAtts;
	}

	private List<String> getStylePaths() {
		List<String> styles = new ArrayList<>();
		styles.add("styles/default/base.css");
		styles.add("styles/default/theme.css");
		styles.add("styles/default/layout.css");
		styles.add("styles/default/state.css");
		return styles;
	}

	private List<String> getScriptPaths() {
		List<String> scripts = new ArrayList<>();
		scripts.add("script/index.js");
		return scripts;
	}
	
	private String buildHTML(XHTMLTagger content, String title, boolean footer) {
		return buildHTML(content, title, footer, true);
	}

	private String buildHTML(XHTMLTagger content, String title, boolean footer, boolean header) {
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
			.start("a").attr("href", "view.html").text(Messages.PREVIEW_VIEW.localize()).end()
			.end()
			.start("span")
			.start("a").attr("href", "index.html?method=meta").text(Messages.MENU_ABOUT_BOOK.localize()).end()
			.end()
			.start("input").attr("id", "connected").attr("type", "submit").attr("value", "").attr("title", "Avsluta").attr("disabled", "disabled").end()
			.start("input").attr("id", "notConnected").attr("type", "submit").attr("value", "").attr("title", "Avsluta").attr("disabled", "disabled").end()
			.end().end().end();
			sb.start("div").attr("id", "top-nav").start("p").end().end();
		}
		sb.start("div").attr("id", "main").start("div").attr("id", "content");
		if (title!=null && !"".equals(title)) {
			sb.tag("h1", title);
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
