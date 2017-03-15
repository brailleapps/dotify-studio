package application;

import java.io.IOException;

import application.l10n.Messages;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
 
public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
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