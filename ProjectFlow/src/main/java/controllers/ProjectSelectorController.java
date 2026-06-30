package controllers;

import dao.ColumnDAO;
import dao.ProjectDAO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import models.Project;
import models.User;
import java.util.List;

public class ProjectSelectorController {

    @FXML private VBox projectsContainer;

    private MainController mainController;
    private User currentUser;
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ColumnDAO columnDAO = new ColumnDAO();
    private Stage dialogStage;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.currentUser = mainController.getCurrentUser();
        loadProjects();
    }

    private void loadProjects() {
        projectsContainer.getChildren().clear();
        if (currentUser == null) return;
        List<Project> projects = projectDAO.findByUserId(currentUser.getId());
        for (Project project : projects) {
            HBox projectBox = createProjectCard(project);
            projectsContainer.getChildren().add(projectBox);
        }
    }

    private HBox createProjectCard(Project project) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1; -fx-padding: 15; -fx-cursor: hand;");
        Circle colorIndicator = new Circle(12);
        try {
            colorIndicator.setFill(javafx.scene.paint.Color.web(project.getColor()));
        } catch (Exception e) {
            colorIndicator.setFill(javafx.scene.paint.Color.web("#1565c0"));
        }
        VBox info = new VBox(5);
        Label title = new Label(project.getTitle());
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: black;");
        Label desc = new Label(project.getDescription() != null ? project.getDescription() : "No description");
        desc.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
        info.getChildren().addAll(title, desc);
        Button deleteBtn = new Button("X");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14; -fx-cursor: hand; -fx-text-fill: #c62828;");
        deleteBtn.setOnAction(e -> deleteProject(project));
        card.getChildren().addAll(colorIndicator, info, deleteBtn);
        card.setOnMouseClicked(e -> {
            if (mainController != null) {
                mainController.setCurrentProject(project);
                mainController.loadProject();
                closeDialog();
            }
        });
        return card;
    }

    private void deleteProject(Project project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Board");
        alert.setHeaderText("Delete '" + project.getTitle() + "'?");
        alert.setContentText("All columns and tasks will be permanently deleted!");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                columnDAO.deleteByProjectId(project.getId());
                if (projectDAO.delete(project.getId())) {
                    loadProjects();
                    mainController.showSuccess("Board deleted");
                }
            }
        });
    }

    @FXML
    public void handleCreateNewProject() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Board");
        dialog.setHeaderText("Create new board");
        TextField titleField = new TextField();
        titleField.setPromptText("Board name...");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description (optional)...");
        descArea.setPrefRowCount(3);
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Board name*:"), titleField,
                new Label("Description:"), descArea
        );
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal.trim().isEmpty());
        });
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return titleField.getText().trim();
            }
            return null;
        });
        dialog.showAndWait().ifPresent(title -> {
            Project project = new Project(currentUser.getId(), title, descArea.getText().trim());
            project.setColor("#1565c0");
            if (projectDAO.create(project)) {
                mainController.setCurrentProject(project);
                mainController.loadProject();
                mainController.showSuccess("Board created: " + title);
                closeDialog();
            }
        });
    }

    @FXML
    public void handleClose() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
}