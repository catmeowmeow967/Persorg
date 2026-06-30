package controllers;

import dao.TaskDAO;
import dao.AttachmentDAO;
import javafx.scene.layout.HBox;
import models.Task;
import models.Attachment;
import utils.FileUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class TaskDetailController {

    @FXML private Label taskTitleLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label dueDateLabel;
    @FXML private Label priorityLabel;
    @FXML private Label statusLabel;
    @FXML private Label createdLabel;
    @FXML private Button saveButton;
    @FXML private Button closeButton;
    @FXML private Button setDueDateButton;
    @FXML private Button toggleStatusButton;
    @FXML private Button archiveButton;
    @FXML private Button toggleStatusButtonTop;
    @FXML private Button setDueDateButtonSmall;
    @FXML private TabPane detailTabs;
    @FXML private VBox attachmentsContainer;

    private Task task;
    private TaskDAO taskDAO = new TaskDAO();
    private AttachmentDAO attachmentDAO = new AttachmentDAO();
    private boolean isModified = false;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        setupButtonActions();
        setupAttachmentsTab();
    }

    private void setupAttachmentsTab() {
        Button uploadButton = new Button("Add File");
        uploadButton.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0;");
        uploadButton.setOnAction(e -> handleUploadFile());
        attachmentsContainer.getChildren().add(uploadButton);
    }

    private void setupButtonActions() {
        if (saveButton != null) {
            saveButton.setOnAction(e -> handleSave());
        }
        if (closeButton != null) {
            closeButton.setOnAction(e -> handleClose());
        }
        if (setDueDateButton != null) {
            setDueDateButton.setOnAction(e -> handleSetDueDate());
        }
        if (toggleStatusButton != null) {
            toggleStatusButton.setOnAction(e -> handleToggleStatus());
        }
        if (archiveButton != null) {
            archiveButton.setOnAction(e -> handleArchiveTaskFromDetails());
        }
        if (setDueDateButtonSmall != null) {
            setDueDateButtonSmall.setOnAction(e -> handleSetDueDate());
        }
        if (toggleStatusButtonTop != null) {
            toggleStatusButtonTop.setOnAction(e -> handleToggleStatus());
        }
    }

    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        File file = fileChooser.showOpenDialog(taskTitleLabel.getScene().getWindow());
        if (file != null) {
            try {
                String savedPath = FileUtils.saveFile(file, task.getId());
                Attachment attachment = new Attachment(task.getId(), file.getName(), savedPath, getFileType(file));
                if (attachmentDAO.create(attachment)) {
                    loadAttachments();
                    showSuccess("File uploaded: " + file.getName());
                }
            } catch (IOException e) {
                showError("Upload error: " + e.getMessage());
            }
        }
    }

    private String getFileType(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1).toUpperCase() : "FILE";
    }

    private void loadAttachments() {
        attachmentsContainer.getChildren().removeIf(node -> node instanceof HBox);
        List<Attachment> attachments = attachmentDAO.findByTaskId(task.getId());
        if (attachments.isEmpty()) {
            Label emptyLabel = new Label("No files");
            emptyLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 13; -fx-padding: 10;");
            attachmentsContainer.getChildren().add(emptyLabel);
            return;
        }
        for (Attachment attachment : attachments) {
            HBox fileRow = new HBox(10);
            fileRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            fileRow.setStyle("-fx-padding: 8; -fx-border-color: #999; -fx-border-width: 0 0 1 0;");
            Label fileLabel = new Label(attachment.getFilename());
            fileLabel.setStyle("-fx-font-size: 13; -fx-text-fill: black;");
            Button openButton = new Button("Open");
            openButton.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 0;");
            openButton.setOnAction(e -> openFile(attachment));
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 0;");
            deleteButton.setOnAction(e -> deleteAttachment(attachment));
            fileRow.getChildren().addAll(fileLabel, openButton, deleteButton);
            attachmentsContainer.getChildren().add(fileRow);
        }
    }

    private void openFile(Attachment attachment) {
        try {
            File file = FileUtils.getFile(attachment.getFilePath());
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showError("File not found: " + attachment.getFilename());
            }
        } catch (IOException e) {
            showError("Cannot open file: " + e.getMessage());
        }
    }

    private void deleteAttachment(Attachment attachment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete File");
        alert.setHeaderText("Delete " + attachment.getFilename() + "?");
        alert.setContentText("File will be permanently deleted.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FileUtils.deleteFile(attachment.getFilePath());
                    if (attachmentDAO.delete(attachment.getId())) {
                        loadAttachments();
                        showSuccess("File deleted");
                    }
                } catch (IOException e) {
                    showError("Delete error: " + e.getMessage());
                }
            }
        });
    }

    public void setTask(Task task) {
        this.task = task;
        updateUI();
        loadAttachments();
        if (archiveButton != null) {
            if (task.isArchived()) {
                archiveButton.setText("Restore");
                archiveButton.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0;");
            } else {
                archiveButton.setText("Archive");
                archiveButton.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0;");
            }
        }
    }

    private void updateUI() {
        if (task == null) return;
        taskTitleLabel.setText(task.getTitle());
        descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");
        if (task.getDueDate() != null) {
            dueDateLabel.setText(task.getDueDate().toString());
        } else {
            dueDateLabel.setText("No due date");
        }
        if (task.getPriority() != null) {
            priorityLabel.setText("Priority: " + task.getPriority().toString());
        } else {
            priorityLabel.setText("Priority: None");
        }
        if (task.isCompleted()) {
            statusLabel.setText("Done");
        } else {
            statusLabel.setText("In Progress");
        }
        if (task.getCreatedAt() != null) {
            createdLabel.setText("Created: " + task.getCreatedAt().toString());
        }
    }

    @FXML
    public void handleSave() {
        if (task != null && descriptionArea != null) {
            task.setDescription(descriptionArea.getText());
            if (taskDAO.update(task)) {
                showSuccess("Task saved");
                isModified = false;
                if (mainController != null) {
                    mainController.refreshBoard();
                }
            }
        }
    }

    @FXML
    public void handleSetDueDate() {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Set Due Date");
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(task.getDueDate() != null ? task.getDueDate() : LocalDate.now().plusDays(7));
        VBox content = new VBox(10, new Label("Select date:"), datePicker);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return datePicker.getValue();
            }
            return null;
        });
        dialog.showAndWait().ifPresent(date -> {
            task.setDueDate(date);
            if (taskDAO.update(task)) {
                updateUI();
                showSuccess("Due date set");
                if (mainController != null) {
                    mainController.refreshBoard();
                }
            }
        });
    }

    @FXML
    public void handleToggleStatus() {
        if (task != null) {
            task.setCompleted(!task.isCompleted());
            if (taskDAO.update(task)) {
                updateUI();
                showSuccess(task.isCompleted() ? "Task completed" : "Task reopened");
                if (mainController != null) {
                    mainController.refreshBoard();
                }
            }
        }
    }

    @FXML
    public void handleArchiveTaskFromDetails() {
        if (task == null) return;
        if (task.isArchived()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Restore Task");
            confirmAlert.setHeaderText("Restore from archive?");
            confirmAlert.setContentText("Task '" + task.getTitle() + "' will be restored.");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (taskDAO.restoreTask(task.getId())) {
                        showSuccess("Task restored: " + task.getTitle());
                        if (mainController != null) {
                            mainController.refreshBoard();
                        }
                        handleClose();
                    }
                }
            });
        } else {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Archive Task");
            confirmAlert.setHeaderText("Archive this task?");
            confirmAlert.setContentText("Task '" + task.getTitle() + "' will be moved to archive.");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (taskDAO.archiveTask(task.getId())) {
                        showSuccess("Task archived: " + task.getTitle());
                        if (mainController != null) {
                            mainController.refreshBoard();
                        }
                        handleClose();
                    }
                }
            });
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) taskTitleLabel.getScene().getWindow();
        stage.close();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}