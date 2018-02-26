package application.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.utils.pef.FileTools;

import application.l10n.Messages;
import application.ui.preview.server.StartupDetails;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Provides the main entry point for the application.
 * @author Joel Håkansson
 *
 */
public class MainFx extends Application {
	private static final Logger logger = Logger.getLogger(MainFx.class.getCanonicalName());

	/**
	 * Starts the application.
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		try {
			//TODO: check error conditions, such as null
			File parent = new File((MainFx.class.getProtectionDomain().getCodeSource().getLocation()).toURI()).getParentFile();
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Plugins folder: " + parent);
			}
			// list jars and convert to URL's
			File plugins = new File(parent, "plugins");
			if (plugins.isDirectory()) {
				URL[] jars = FileTools.toURL(listFiles(plugins).toArray(new File[]{}));
				for (URL u : jars) {
					logger.info("Found jars " + u);
				}
				// set context class loader
				if (jars.length>0) {
					Thread.currentThread().setContextClassLoader(new URLClassLoader(jars));
				}
			}
		} catch (URISyntaxException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Failed to set plugins class loader.", e);
			}
		}

		primaryStage.setTitle("Dotify Studio");
		primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("resource-files/icon.png")));
		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();

		primaryStage.setX(bounds.getMinX());
		primaryStage.setY(bounds.getMinY());
		primaryStage.setWidth(bounds.getWidth());
		primaryStage.setHeight(bounds.getHeight());

		FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Main.fxml"), Messages.getBundle());
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);

		primaryStage.setScene(scene);
		primaryStage.show();
		
		MainController controller = fxmlLoader.<MainController>getController();
		Optional<StartupDetails> args = StartupDetails.parse(getParameters().getRaw().toArray(new String[]{}));
		args.ifPresent(a->controller.openArgs(a));

		if (!args.isPresent() && getParameters().getRaw().size()>0) {
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.ERROR, Messages.ERROR_INVALID_ARGUMENTS.localize(getParameters().getRaw()), ButtonType.OK);
				alert.showAndWait();
			});
		}

		scene.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent ev) {
				if (!controller.confirmShutdown()) {
					ev.consume();
				}
			}
		});
	}
	
	private static List<File> listFiles(File dir) {
		List<File> files = new ArrayList<>();
		for (File f : dir.listFiles(f->f.isDirectory())) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Scanning dir " + f);
			}
			files.addAll(listFiles(f));
		}
		for (File f : dir.listFiles(f->f.getName().endsWith(".jar"))) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Adding file: " + f);
			}
			files.add(f);
		}
		return files;
	}

}