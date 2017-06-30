package application.about;


import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import shared.BuildInfo;

/**
 * Provides a controller for the dialog that displays information about the software.
 * @author Joel Håkansson
 *
 */
public class AboutController {
	private static final String PEF_URL = "http://pef-format.org";
	private static final String BRAILLE_APPS_URL = "https://github.com/brailleapps";
	@FXML private Label title;
	@FXML private Label description;
	@FXML private Label version;
	@FXML private Hyperlink pefLink;
	@FXML private Hyperlink contributeLink;
	@FXML private Button ok;

	/**
	 * Initializes the controller.
	 */
	@FXML void initialize() {
		version.setText(Messages.APPLICATION_VERSION.localize(BuildInfo.VERSION, BuildInfo.BUILD));
		pefLink.setText(PEF_URL);
		contributeLink.setText(BRAILLE_APPS_URL);
	}

	/**
	 * Closes the window.
	 */
	@FXML void closeWindow() {
		((Stage)ok.getScene().getWindow()).close();
	}

	/**
	 * Opens the pef-format website in a browser.
	 */
	@FXML void visitPefFormat() {
		visit(PEF_URL);
	}

	/**
	 * Opens the github organization in a browser.
	 */
	@FXML void visitGithub() {
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
