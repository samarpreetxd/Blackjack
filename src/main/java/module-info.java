module application.blackjackxgui {
    requires javafx.controls;
    requires javafx.fxml;


    opens application.blackjackxgui to javafx.fxml;
    exports application.blackjackxgui;
}