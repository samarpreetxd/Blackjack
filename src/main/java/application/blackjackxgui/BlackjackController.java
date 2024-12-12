package application.blackjackxgui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class BlackjackController {

    @FXML
    private Label dealerHandLabel;

    @FXML
    private Label player1HandLabel;

    @FXML
    private Label player2HandLabel;

    @FXML
    private Label player3HandLabel;

    @FXML
    private Button hitButton;

    @FXML
    private Button standButton;

    private PrintWriter out;
    private BufferedReader in;

    @FXML
    public void initialize() {
        connectToServer();

        // Set button actions
        hitButton.setOnAction(e -> {
            System.out.println("HIT button clicked");
            sendAction("HIT");
        });

        standButton.setOnAction(e -> {
            System.out.println("STAND button clicked");
            sendAction("STAND");
        });

        disableActionButtons(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("127.0.0.1", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to server.");

            // Start a thread to handle server messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        handleServerMessage(message);
                    }
                } catch (IOException e) {
                    showAlert("Disconnected from server.");
                }
            }).start();

        } catch (IOException e) {
            showAlert("Failed to connect to server.");
        }
    }

    private void sendAction(String action) {
        if (out != null) {
            out.println(action); // Send action to the server
            System.out.println("Sent action to server: " + action); // Debug log
            disableActionButtons(true); // Disable buttons after sending the action
        }
    }

    private void handleServerMessage(String message) {
        System.out.println("Received from server: " + message);

        Platform.runLater(() -> {
            if (message.startsWith("STATE|")) {
                // Handle state updates (e.g., updating labels)
                handleStateUpdate(message.replace("STATE|", ""));
            } else if (message.startsWith("INFO|")) {
                // Handle informational messages (no pop-ups needed)
                System.out.println(message.replace("INFO|", ""));
            } else if (message.startsWith("RESULT|")) {
                // Show results as an alert
                showAlert(message.replace("RESULT|", ""));
            } else if (message.equalsIgnoreCase("Your turn")) {
                disableActionButtons(false); // Enable buttons for player's turn
                showAlert("It's your turn! Take an action.");
            } else if (message.equalsIgnoreCase("Wait for your turn")) {
                disableActionButtons(true); // Disable buttons when waiting
            }
        });
    }

    private void handleStateUpdate(String stateMessage) {
        if (stateMessage.startsWith("Dealer")) {
            dealerHandLabel.setText(stateMessage.replace("Dealer's Hand:", "").trim());
        } else if (stateMessage.startsWith("Player 1")) {
            player1HandLabel.setText(stateMessage.replace("Player 1's Hand:", "").trim());
        } else if (stateMessage.startsWith("Player 2")) {
            player2HandLabel.setText(stateMessage.replace("Player 2's Hand:", "").trim());
        } else if (stateMessage.startsWith("Player 3")) {
            player3HandLabel.setText(stateMessage.replace("Player 3's Hand:", "").trim());
        } else if (stateMessage.startsWith("Your Hand")) {
            // Update the player's own hand in the GUI
            player1HandLabel.setText(stateMessage.replace("Your Hand:", "").trim());
        }
    }

    private void disableActionButtons(boolean disable) {
        System.out.println("Setting buttons to " + (disable ? "disabled" : "enabled")); // Debug log
        hitButton.setDisable(disable);
        standButton.setDisable(disable);
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Blackjack Game");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    public void setStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            // Perform cleanup actions or notify the server before closing
            if (out != null) {
                out.println("DISCONNECT"); // Notify the server of disconnection
            }
            System.out.println("Application is closing...");
            Platform.exit();
        });
    }

}
