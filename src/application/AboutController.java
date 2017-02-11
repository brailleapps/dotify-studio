package application;


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
	@FXML private Label title;
	@FXML private Label description;
	@FXML private Label version;
	@FXML private Text moreAboutPef;
	@FXML private Hyperlink pefLink;
	@FXML private Button ok;

	@FXML
	public void initialize() {
		title.setText(Messages.APPLICATION_TITLE.localize());
		description.setText(Messages.APPLICATION_DESCRIPTION.localize());
		version.setText(Messages.APPLICATION_VERSION.localize(BuildInfo.VERSION, BuildInfo.BUILD));
		moreAboutPef.setText(Messages.APPLICATION_ABOUT_PEF.localize());
		pefLink.setText(PEF_URL);
		ok.setText(Messages.BUTTON_OK.localize());
	}
	
	@FXML
	public void closeWindow() {
		((Stage)ok.getScene().getWindow()).close();
	}
	
	@FXML
	public void visitPefFormat() {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(PEF_URL));
			} catch (IOException | URISyntaxException e) {

			}
		}
	}
}
