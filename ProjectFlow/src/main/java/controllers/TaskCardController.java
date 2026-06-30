package controllers;

import dao.TaskDAO;
import dao.AttachmentDAO;
import models.Task;
import models.Attachment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TaskCardController {

    @FXML private Rectangle priorityIndicator;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priorityLabel;
    @FXML private Label dueDateLabel;
    @FXML private Label dueDateIcon;
    @FXML private FlowPane labelsContainer;
    @FXML private FlowPane membersContainer;
    @FXML private VBox taskCardContainer;
    @FXML private Button menuButton;

    private Task task;
    private ColumnController columnController;
    private TaskDAO taskDAO = new TaskDAO();
    private AttachmentDAO attachmentDAO = new AttachmentDAO();

    @FXML
    public void initialize() {
        taskCardContainer.setOnMouseClicked(event -> handleCardClick());
        taskCardContainer.setOnMouseEntered(event -> handleMouseEnter());
        taskCardContainer.setOnMouseExited(event -> handleMouseExit());
        menuButton.setOnMouseEntered(event -> handleMenuButtonHover());
        menuButton.setOnMouseExited(event -> handleMenuButtonExit());
        menuButton.setOnAction(event -> handleCardMenu());
    }

    public void setTask(Task task, ColumnController columnController) {
        this.task = task;
        this.columnController = columnController;
        updateUI();
    }

    private void updateUI() {
        if (task == null) return;
        titleLabel.setText(task.getTitle());
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            String shortDesc = task.getDescription();
            if (shortDesc.length() > 100) {
                shortDesc = shortDesc.substring(0, 97) + "...";
            }
            descriptionLabel.setText(shortDesc);
            descriptionLabel.setVisible(true);
            descriptionLabel.setManaged(true);
        } else {
            descriptionLabel.setVisible(false);
            descriptionLabel.setManaged(false);
        }
        if (task.getPriority() != null) {
            String priorityText = getPriorityText(task.getPriority());
            priorityLabel.setText(priorityText);
            String priorityColor = getPriorityColor(task.getPriority());
            String textColor = getPriorityTextColor(task.getPriority());
            priorityLabel.setStyle(String.format(
                    "-fx-background-color: %s; -fx-text-fill: %s; " +
                            "-fx-font-size: 11; -fx-font-weight: bold; " +
                            "-fx-padding: 2 6;",
                    priorityColor, textColor
            ));
            priorityIndicator.setFill(Color.web(priorityColor));
        } else {
            priorityLabel.setText("NONE");
            priorityLabel.setStyle(
                    "-fx-background-color: #333; -fx-text-fill: white; " +
                            "-fx-font-size: 11; -fx-font-weight: bold; " +
                            "-fx-padding: 2 6;"
            );
            priorityIndicator.setFill(Color.web("#333"));
        }
        if (task.getDueDate() != null) {
            String dueDateText = formatDueDate(task.getDueDate());
            String dateColor = getDueDateColor(task.getDueDate());
            String icon = getDueDateIcon(task.getDueDate());
            dueDateLabel.setText(dueDateText);
            dueDateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + dateColor + ";");
            dueDateIcon.setText(icon);
            dueDateIcon.setStyle("-fx-font-size: 11; -fx-text-fill: " + dateColor + ";");
            dueDateLabel.setVisible(true);
            dueDateLabel.setManaged(true);
            dueDateIcon.setVisible(true);
            dueDateIcon.setManaged(true);
        } else {
            dueDateLabel.setVisible(false);
            dueDateLabel.setManaged(false);
            dueDateIcon.setVisible(false);
            dueDateIcon.setManaged(false);
        }
        updateLabels();
        updateMembers();
        updateCardStyle();
        updateAttachmentIndicator();
    }

    private void updateAttachmentIndicator() {
        List<Attachment> attachments = attachmentDAO.findByTaskId(task.getId());
        if (!attachments.isEmpty()) {
            Label attachLabel = new Label("[" + attachments.size() + "]");
            attachLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #333;");
            membersContainer.getChildren().add(0, attachLabel);
        }
    }

    public String getPriorityText(Task.Priority priority) {
        switch (priority) {
            case LOW: return "LOW";
            case MEDIUM: return "MED";
            case HIGH: return "HIGH";
            case CRITICAL: return "CRIT";
            default: return "NONE";
        }
    }

    public String getPriorityColor(Task.Priority priority) {
        switch (priority) {
            case LOW: return "#61bd4f";
            case MEDIUM: return "#f2d600";
            case HIGH: return "#ff9f1a";
            case CRITICAL: return "#c62828";
            default: return "#333";
        }
    }

    public String getPriorityTextColor(Task.Priority priority) {
        return priority == Task.Priority.MEDIUM ? "black" : "white";
    }

    public String formatDueDate(LocalDate date) {
        if (date == null) return "";
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, date);
        if (daysUntil == 0) return "TODAY";
        if (daysUntil == 1) return "TOMORROW";
        if (daysUntil == -1) return "YESTERDAY";
        if (daysUntil < 0) return Math.abs(daysUntil) + "D AGO";
        if (daysUntil <= 7) return daysUntil + "D";
        return date.toString();
    }

    public String getDueDateColor(LocalDate date) {
        if (date == null) return "#333";
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, date);
        if (daysUntil < 0) return "#c62828";
        if (daysUntil == 0) return "#f2d600";
        if (daysUntil <= 2) return "#ff9f1a";
        return "#61bd4f";
    }

    public String getDueDateIcon(LocalDate date) {
        if (date == null) return "";
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, date);
        if (daysUntil < 0) return "!";
        if (daysUntil == 0) return "*";
        if (daysUntil == 1) return ">";
        return "";
    }

    private void updateLabels() {
        labelsContainer.getChildren().clear();
        if (task.getPriority() == Task.Priority.CRITICAL) {
            createLabel("URGENT", "#c62828");
        }
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            createLabel("OVERDUE", "#c62828");
        }
        String title = task.getTitle().toLowerCase();
        if (title.contains("bug") || title.contains("error")) {
            createLabel("BUG", "#c62828");
        }
        if (title.contains("feature")) {
            createLabel("FEATURE", "#61bd4f");
        }
        if (title.contains("design") || title.contains("ui")) {
            createLabel("DESIGN", "#00c2e0");
        }
    }

    private void createLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                        "-fx-font-size: 10; -fx-font-weight: bold; " +
                        "-fx-padding: 1 4;",
                color
        ));
        labelsContainer.getChildren().add(label);
    }

    private void updateMembers() {
        membersContainer.getChildren().clear();
        String[] memberColors = {"#00c2e0", "#ff9f1a", "#61bd4f"};
        for (int i = 0; i < Math.min(2, memberColors.length); i++) {
            javafx.scene.shape.Circle member = new javafx.scene.shape.Circle(8);
            member.setFill(Color.web(memberColors[i]));
            membersContainer.getChildren().add(member);
        }
    }

    private void updateCardStyle() {
        if (task.isCompleted()) {
            taskCardContainer.setStyle(
                    "-fx-background-color: #f5f5f5; -fx-border-color: #999; -fx-border-width: 1; " +
                            "-fx-padding: 8; -fx-opacity: 0.6;"
            );
            titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666; -fx-strikethrough: true;");
        } else {
            taskCardContainer.setStyle(
                    "-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1; " +
                            "-fx-padding: 8;"
            );
            titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: black; -fx-font-weight: normal;");
        }
    }

    public void handleCardClick() {
        if (task != null && columnController != null && columnController.getMainController() != null) {
            columnController.getMainController().showTaskDetailDialog(task);
        }
    }

    public void handleMouseEnter() {
        if (task != null && !task.isCompleted()) {
            taskCardContainer.setStyle(
                    "-fx-background-color: #f5f5f5; -fx-border-color: #333; -fx-border-width: 1; " +
                            "-fx-padding: 8;"
            );
        }
    }

    public void handleMouseExit() {
        updateCardStyle();
    }

    public void handleMenuButtonHover() {
        menuButton.setStyle(
                "-fx-background-color: rgba(0,0,0,0.1); " +
                        "-fx-text-fill: #333; " +
                        "-fx-font-size: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0;"
        );
    }

    public void handleMenuButtonExit() {
        menuButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: transparent; " +
                        "-fx-font-size: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0;"
        );
    }

    public void handleCardMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> handleCardClick());
        MenuItem dueDateItem = new MenuItem("Change Due Date");
        dueDateItem.setOnAction(e -> {
            if (columnController != null && columnController.getMainController() != null) {
                columnController.getMainController().showEditTaskDialog(task);
            }
        });
        MenuItem archiveItem = new MenuItem("Archive");
        archiveItem.setOnAction(e -> handleArchiveTask());
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Task");
            alert.setContentText("Delete '" + task.getTitle() + "' permanently?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (taskDAO.delete(task.getId())) {
                        if (columnController != null) {
                            columnController.refresh();
                        }
                    }
                }
            });
        });
        menu.getItems().addAll(editItem, dueDateItem, new SeparatorMenuItem(), archiveItem, deleteItem);
        menu.show(menuButton, menuButton.localToScreen(0, 0).getX() + menuButton.getWidth(), menuButton.localToScreen(0, 0).getY());
    }

    private void handleArchiveTask() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Archive Task");
        confirmAlert.setHeaderText("Archive this task?");
        confirmAlert.setContentText("Task '" + task.getTitle() + "' will be moved to archive.");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (taskDAO.archiveTask(task.getId())) {
                    System.out.println("Task archived: " + task.getTitle() + " (ID: " + task.getId() + ")");
                    if (columnController != null && columnController.getMainController() != null) {
                        columnController.getMainController().showSuccess("Task archived: " + task.getTitle());
                    }
                    if (columnController != null) {
                        columnController.refresh();
                    }
                } else {
                    System.out.println("Archive error: " + task.getId());
                    if (columnController != null && columnController.getMainController() != null) {
                        columnController.getMainController().showError("Archive error");
                    }
                }
            }
        });
    }

    public Task getTask() {
        return task;
    }
}