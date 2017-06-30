package application;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.pef.FileTools;

import application.l10n.Messages;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
 
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
			URL[] jars = FileTools.toURL(FileTools.listFiles(new File(parent, "plugins"), ".jar"));
			for (URL u : jars) {
				logger.info("Found jars " + u);
			}
			// set context class loader
			if (jars.length>0) {
				Thread.currentThread().setContextClassLoader(new URLClassLoader(jars));
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
        
        MainController controller = fxmlLoader.<MainController>getController();
        controller.openArgs(getParameters().getRaw().toArray(new String[]{}));

        primaryStage.setScene(scene);
        primaryStage.show();
    }

}