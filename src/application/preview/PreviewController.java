package application.preview;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.e2u.Start;

import application.l10n.Messages;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PreviewController extends BorderPane {
	private static final Logger logger = Logger.getLogger(PreviewController.class.getCanonicalName());
	@FXML
	public WebView browser;
	private String url;
	private Start start;

	public PreviewController() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Preview.fxml"), Messages.getBundle());
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load view", e);
		}

        WebEngine webEngine = browser.getEngine();
        webEngine.setCreatePopupHandler(p-> {
                Stage stage = new Stage(StageStyle.UTILITY);
                WebView wv2 = new WebView();
                stage.setScene(new Scene(wv2));
                stage.show();
                return wv2.getEngine();
            }
        );
	}
	
	public void open(String[] args) {
		Task<String> startServer = new Task<String>() {

			@Override
			protected String call() throws Exception {
		        String url = null;
		        try {
		        	start = new Start();
		        	return start.start(args, false, false);
				} catch (Exception e1) {
					Logger.getLogger(this.getClass().getCanonicalName()).log(Level.SEVERE, "Failed to load server.", e1);;
				}  
		        return null;
			}
		};
		startServer.setOnSucceeded(ev -> {
				this.url = startServer.getValue();
				if (url!=null) {
					browser.getEngine().load(url);
				} else {
					browser.getEngine().load(getClass().getResource("resource-files/fail.html").toString());
				}
			}
		);
		Thread th = new Thread(startServer);
		th.setDaemon(true);
		th.start();
	}
	
	public void reload() {
		browser.getEngine().reload();
	}
	
	public String getURL() {
		return url;
	}
	
	public Optional<URI> getBookURI() {
		if (start!=null) {
			return start.getMainPage().getBookURI();
		} else {
			return Optional.<URI>empty();
		}
	}

}
