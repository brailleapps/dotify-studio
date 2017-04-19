package application.about;


import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.googlecode.e2u.BuildInfo;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AboutController {
	private static final String PEF_URL = "http://pef-format.org";
	private static final String BRAILLE_APPS_URL = "https://github.com/brailleapps";
	@FXML private Label title;
	@FXML private Label description;
	@FXML private Label version;
	@FXML private Hyperlink pefLink;
	@FXML private Hyperlink contributeLink;
	@FXML private Button ok;

	@FXML
	public void initialize() {
		version.setText(Messages.APPLICATION_VERSION.localize(BuildInfo.VERSION, BuildInfo.BUILD));
		pefLink.setText(PEF_URL);
		contributeLink.setText(BRAILLE_APPS_URL);
	}
	
	@FXML
	public void closeWindow() {
		((Stage)ok.getScene().getWindow()).close();
	}
	
	@FXML
	public void visitPefFormat() {
		visit(PEF_URL);
	}
	
	@FXML
	public void visitGithub() {
		visit(BRAILLE_APPS_URL);
	}
	
	
	private void visit(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException | URISyntaxException e) {

			}
		}

	}
}
