package com.example.projectflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import dao.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            if (!DatabaseConnection.testConnection()) {
                showDatabaseErrorDialog();
                return;
            }
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/projectflow/login.fxml"));
            if (loader.getLocation() == null) {
                throw new RuntimeException("FXML not found: /com/example/projectflow/login.fxml");
            }
            System.out.println("Loading FXML from: " + loader.getLocation());
            Parent root = loader.load();
            Scene scene = new Scene(root, 400, 300);
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            primaryStage.setTitle("TaskBoard - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            double widthPercentage = 0.7;
            double heightPercentage = 0.8;
            primaryStage.setWidth(screenBounds.getWidth() * widthPercentage);
            primaryStage.setHeight(screenBounds.getHeight() * heightPercentage);
            primaryStage.centerOnScreen();
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(300);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Application Error",
                    "Failed to start:\n" + e.getMessage());
        }
    }

    private void showDatabaseErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText("Database connection failed");
        alert.setContentText("Check:\n" +
                "1. MySQL server is running\n" +
                "2. Connection parameters in DatabaseConnection.java\n" +
                "3. Database 'projectflow_db' exists");
        alert.getDialogPane().setMinSize(400, 250);
        alert.showAndWait();
        System.exit(1);
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinSize(300, 200);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
        System.out.println("Application stopped");
    }

    public static void main(String[] args) {
        launch(args);
    }
}