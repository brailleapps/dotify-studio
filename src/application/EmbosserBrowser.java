package application;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.e2u.Start;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EmbosserBrowser extends VBox {
	private WebView browser;

	public EmbosserBrowser(String[] args) {

        String url = getClass().getResource("resource-files/fail.html").toString();
        try {
			url = Start.run(args, false);
		} catch (Exception e1) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, "Failed to load server.", e1);;
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
        getChildren().addAll(browser);
	}
	
	public void reload() {
		browser.getEngine().reload();
	}

}
