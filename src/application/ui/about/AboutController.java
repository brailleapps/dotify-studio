package application.ui.about;


import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

import application.common.BuildInfo;
import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Provides a controller for the dialog that displays information about the software.
 * @author Joel Håkansson
 *
 */
public class AboutController {
	private static final String PEF_URL = "http://pef-format.org";
	private static final String BRAILLE_APPS_URL = "https://github.com/brailleapps";
	@FXML private Label title;
	@FXML private Text description;
	@FXML private Text version;
	@FXML private Label environment;
	@FXML private Hyperlink pefLink;
	@FXML private Hyperlink contributeLink;
	@FXML private Button ok;
	@FXML private TextArea configTextArea;

	/**
	 * Initializes the controller.
	 */
	@FXML void initialize() {
		version.setText(Messages.APPLICATION_VERSION.localize(BuildInfo.VERSION, BuildInfo.BUILD));
		environment.setText(Messages.APPLICATION_ENVIRONMENT.localize(
				System.getProperty("java.version")));
		pefLink.setText(PEF_URL);
		contributeLink.setText(BRAILLE_APPS_URL);
		configTextArea.setText(System.getProperties().entrySet().stream()
				.sorted((v1, v2)->String.valueOf(v1.getKey()).compareTo(String.valueOf(v2.getKey())))
				.map(v->v.getKey()+"="+v.getValue())
				.collect(Collectors.joining("\n", "", "")));
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
			new Thread(()->{
				// Wrapping this in a new thread in order to fix
				// https://github.com/brailleapps/dotify-studio/issues/44
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException e) {
	
				}
			}).start();
		}
	}
}
