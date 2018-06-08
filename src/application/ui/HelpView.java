package application.ui;

import org.daisy.dotify.studio.api.SearchCapabilities;
import org.daisy.dotify.studio.api.SearchOptions;
import org.daisy.dotify.studio.api.Searchable;

import application.l10n.Messages;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

class HelpView extends BorderPane implements Searchable {
	private static final ReadOnlyObjectProperty<SearchCapabilities> SEARCH_CAPABILITIES = new SimpleObjectProperty<>(
			new SearchCapabilities.Builder()
			.direction(true)
			.matchCase(true)
			.wrap(true)
			.find(true)
			.replace(false)
			.build()
	);

	private WebView wv;
	HelpView() {
		wv = new WebView();
		wv.setOnDragOver(event->event.consume());
		setCenter(wv);
	}
	
	void loadURL(String helpURL) {
		if (helpURL!=null) {
			WebEngine engine = wv.getEngine();
			engine.load(helpURL);
		} else {
			wv.getEngine().loadContent("<html><body><p>"+Messages.ERROR_FAILED_TO_LOAD_HELP.localize()+"</p></body></html>");
		}
	}

	@Override
	public ObservableObjectValue<SearchCapabilities> searchCapabilities() {
		return SEARCH_CAPABILITIES;
	}

	@Override
	public boolean findNext(String text, SearchOptions opts) {
		return (Boolean)wv.getEngine().executeScript(
				String.format("self.find('%s', %b, %b, %b)", text, 
				opts.shouldMatchCase(), opts.shouldReverseSearch(), opts.shouldWrapAround())
		);
	}

	@Override
	public void replace(String replace) {
		// Not supported
	}

	@Override
	public String getSelectedText() {
		return wv.getEngine().executeScript("window.getSelection().toString()").toString();
	}
}