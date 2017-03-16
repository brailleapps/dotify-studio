package application;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.e2u.Start;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EmbosserBrowser extends BorderPane {
	private WebView browser;
	private final String url;

	public EmbosserBrowser(String[] args) {

        String url = null;
        try {
			url = Start.run(args, false);
		} catch (Exception e1) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, "Failed to load server.", e1);;
		}
        this.url = url;
       
        if (url==null) {
        	url = getClass().getResource("resource-files/fail.html").toString();
        }
        browser = new WebView();

        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        webEngine.setCreatePopupHandler(p-> {
                Stage stage = new Stage(StageStyle.UTILITY);
                WebView wv2 = new WebView();
                stage.setScene(new Scene(wv2));
                stage.show();
                return wv2.getEngine();
            }
        );
        setCenter(browser);
	}
	
	public void reload() {
		browser.getEngine().reload();
	}
	
	public String getURL() {
		return url;
	}

}
