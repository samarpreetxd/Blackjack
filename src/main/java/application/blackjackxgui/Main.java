package application.blackjackxgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Blackjack.fxml"));
        Parent root = loader.load();

        // Get the controller and set the stage
        BlackjackController controller = loader.getController();
        controller.setStage(primaryStage);

        // Set up the stage and scene
        primaryStage.setTitle("Multiplayer Blackjack");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
