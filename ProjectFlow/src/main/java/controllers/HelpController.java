package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class HelpController {

    @FXML private TabPane helpTabs;
    @FXML private Button startButton;

    @FXML
    public void initialize() {
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        if (startButton != null) {
            startButton.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Getting Started");
                alert.setHeaderText("Ready!");
                alert.setContentText("You can now start using TaskBoard.\nCreate your first board and add tasks.");
                alert.showAndWait();
            });
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) helpTabs.getScene().getWindow();
        stage.close();
    }
}