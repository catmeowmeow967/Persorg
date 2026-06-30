package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Column;
import models.Project;
import models.Task;
import models.User;
import dao.ColumnDAO;
import dao.ProjectDAO;
import dao.TaskDAO;
import dao.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private javafx.scene.control.Label projectTitleLabel;
    @FXML private HBox kanbanBoard;
    @FXML private Button userMenuBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button boardsBtn;
    @FXML private Button archiveTabButton;
    @FXML private Button helpButton;
    @FXML private Button filtersBtn;
    @FXML private Button labelsBtn;
    @FXML private Button membersBtn;
    @FXML private Button automationBtn;
    @FXML private Button menuBtn;

    private User currentUser;
    private Project currentProject;
    private boolean showArchive = false;
    private ProjectDAO projectDAO = new ProjectDAO();
    private ColumnDAO columnDAO = new ColumnDAO();
    private TaskDAO taskDAO = new TaskDAO();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("MainController initialized");
        setupToolbarButtons();
        setupProjectTitleClick();
        setupNotificationButton();
        setupHelpButton();
        if (archiveTabButton != null) {
            archiveTabButton.setOnAction(e -> toggleArchiveView());
            archiveTabButton.setText("Archive");
        }
    }

    @FXML
    private void toggleArchiveView() {
        showArchive = !showArchive;
        System.out.println("Archive mode toggle. Show archive: " + showArchive);
        System.out.println("Current project: " + (currentProject != null ? currentProject.getTitle() : "null"));
        archiveTabButton.setText(showArchive ? "Board" : "Archive");
        loadColumns();
    }

    private void setupToolbarButtons() {
        if (menuBtn != null) {
            menuBtn.setOnAction(e -> showHelpMenu());
            menuBtn.setTooltip(new Tooltip("Menu and help"));
        }
        if (filtersBtn != null) {
            filtersBtn.setOnAction(e -> showFiltersDialog());
            filtersBtn.setTooltip(new Tooltip("Task filters"));
        }
        if (labelsBtn != null) {
            labelsBtn.setOnAction(e -> showLabelsDialog());
            labelsBtn.setTooltip(new Tooltip("Manage labels"));
        }
        if (membersBtn != null) {
            membersBtn.setOnAction(e -> showMembersDialog());
            membersBtn.setTooltip(new Tooltip("Manage members"));
        }
        if (automationBtn != null) {
            automationBtn.setOnAction(e -> showAutomationDialog());
            automationBtn.setTooltip(new Tooltip("Task automation"));
        }
        if (boardsBtn != null) {
            boardsBtn.setOnAction(e -> showProjectSelector());
            boardsBtn.setTooltip(new Tooltip("My boards"));
        }
    }

    @FXML
    public void showHelpMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem helpItem = new MenuItem("Help");
        helpItem.setOnAction(e -> showHelpDialog());
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        menu.getItems().addAll(helpItem, new SeparatorMenuItem(), aboutItem);
        menu.show(menuBtn, menuBtn.localToScreen(0, 0).getX(), menuBtn.localToScreen(0, 0).getY() + menuBtn.getHeight());
    }

    private void setupNotificationButton() {
        if (notificationsBtn != null) {
            notificationsBtn.setOnAction(e -> handleNotifications());
            notificationsBtn.setTooltip(new Tooltip("Tasks for today"));
        }
    }

    private void setupHelpButton() {
        if (helpButton != null) {
            helpButton.setOnAction(e -> showHelpDialog());
            helpButton.setTooltip(new Tooltip("Help"));
        }
    }

    @FXML
    public void handleNotifications() {
        if (currentProject == null) {
            showInformation("Notifications", "No active project");
            return;
        }
        List<Task> todayTasks = getTodayTasks();
        if (todayTasks.isEmpty()) {
            showInformation("Notifications", "No tasks for today!");
            return;
        }
        ContextMenu menu = new ContextMenu();
        Label headerLabel = new Label("Tasks for today (" + todayTasks.size() + ")");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 10;");
        CustomMenuItem headerItem = new CustomMenuItem(headerLabel);
        headerItem.setHideOnClick(false);
        menu.getItems().add(headerItem);
        menu.getItems().add(new SeparatorMenuItem());
        for (Task task : todayTasks) {
            Column column = columnDAO.findById(task.getColumnId());
            String columnName = column != null ? column.getTitle() : "Unknown";
            String displayText = task.getTitle() + " -> " + columnName;
            MenuItem taskItem = new MenuItem(displayText);
            taskItem.setStyle("-fx-font-size: 13;");
            taskItem.setOnAction(e -> showTaskDetailDialog(task));
            menu.getItems().add(taskItem);
        }
        menu.show(notificationsBtn, notificationsBtn.localToScreen(0, 0).getX(), notificationsBtn.localToScreen(0, 0).getY() + notificationsBtn.getHeight());
    }

    private List<Task> getTodayTasks() {
        if (currentProject == null) return new ArrayList<>();
        LocalDate today = LocalDate.now();
        List<Task> todayTasks = new ArrayList<>();
        List<Task> allTasks = taskDAO.findByProjectId(currentProject.getId());
        for (Task task : allTasks) {
            if (task.getDueDate() != null && !task.isCompleted()) {
                long daysUntil = ChronoUnit.DAYS.between(today, task.getDueDate());
                if (daysUntil == 0) {
                    todayTasks.add(task);
                }
            }
        }
        return todayTasks;
    }

    private void setupProjectTitleClick() {
        projectTitleLabel.setStyle(projectTitleLabel.getStyle() + "; -fx-cursor: hand;");
        projectTitleLabel.setOnMouseClicked(e -> showProjectSelector());
        Tooltip tooltip = new Tooltip("Click to select another board");
        Tooltip.install(projectTitleLabel, tooltip);
    }

    public void loadProject() {
        if (currentProject == null) return;
        projectTitleLabel.setText(currentProject.getTitle());
        loadColumns();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && userMenuBtn != null) {
            userMenuBtn.setText(user.getUsername().substring(0, 1).toUpperCase());
        }
        loadProjects();
    }

    private void loadProjects() {
        if (currentUser == null) return;
        List<Project> projects = projectDAO.findByUserId(currentUser.getId());
        if (!projects.isEmpty()) {
            currentProject = projects.get(0);
            loadProject();
        } else {
            createDefaultProject();
        }
    }

    private void createDefaultProject() {
        Project project = new Project(currentUser.getId(), "My first board", "Welcome to TaskBoard!");
        project.setColor("#026aa7");
        if (projectDAO.create(project)) {
            currentProject = project;
            loadProject();
            createDefaultColumns();
        }
    }

    private void createDefaultColumns() {
        String[] titles = {"To Do", "In Progress", "Review", "Done"};
        String[] colors = {"#eb5a46", "#ff9f1a", "#f2d600", "#61bd4f"};
        for (int i = 0; i < titles.length; i++) {
            Column column = new Column(currentProject.getId(), titles[i], i);
            column.setColor(colors[i]);
            columnDAO.create(column);
        }
        loadColumns();
    }

    public void loadColumns() {
        if (kanbanBoard == null) return;
        kanbanBoard.getChildren().clear();
        if (currentProject == null) return;
        System.out.println("Loading columns. Archive mode: " + showArchive);
        if (showArchive) {
            loadArchivedTasks();
            return;
        }
        List<Column> columns = columnDAO.findByProjectId(currentProject.getId());
        System.out.println("Found active columns: " + columns.size());
        for (Column column : columns) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/column.fxml"));
                VBox columnBox = loader.load();
                ColumnController controller = loader.getController();
                controller.setColumn(column, this);
                kanbanBoard.getChildren().add(columnBox);
            } catch (IOException e) {
                e.printStackTrace();
                createSimpleColumn(column);
            }
        }
        addCreateColumnButton();
    }

    private void loadArchivedTasks() {
        System.out.println("Loading archived tasks for project ID: " + currentProject.getId());
        List<Task> archivedTasks = taskDAO.findArchivedTasksByProjectId(currentProject.getId());
        System.out.println("Found archived tasks: " + archivedTasks.size());
        VBox archiveHeader = new VBox(10);
        archiveHeader.setPadding(new Insets(20, 20, 15, 20));
        archiveHeader.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");
        archiveHeader.setPrefWidth(800);
        Label archiveTitle = new Label("ARCHIVE");
        archiveTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: black;");
        Label archiveInfo = new Label("All archived tasks are shown here");
        archiveInfo.setStyle("-fx-text-fill: #333; -fx-font-size: 14;");
        Label taskCountLabel = new Label("Total archived: " + archivedTasks.size());
        taskCountLabel.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold; -fx-font-size: 16;");
        archiveHeader.getChildren().addAll(archiveTitle, archiveInfo, taskCountLabel);
        kanbanBoard.getChildren().add(archiveHeader);
        if (archivedTasks.isEmpty()) {
            VBox emptyArchiveBox = new VBox(20);
            emptyArchiveBox.setPadding(new Insets(40, 20, 20, 20));
            emptyArchiveBox.setAlignment(Pos.CENTER);
            Label emptyLabel = new Label("Archive is empty");
            emptyLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #333;");
            Label hintLabel = new Label("To archive a task, open task menu and select 'Archive'");
            hintLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14; -fx-alignment: center;");
            hintLabel.setWrapText(true);
            emptyArchiveBox.getChildren().addAll(emptyLabel, hintLabel);
            kanbanBoard.getChildren().add(emptyArchiveBox);
            return;
        }
        VBox archivedTasksContainer = new VBox(10);
        archivedTasksContainer.setPadding(new Insets(20, 20, 20, 40));
        archivedTasksContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: black; -fx-border-width: 1;");
        Label tasksHeader = new Label("Archived tasks:");
        tasksHeader.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: black; -fx-padding: 0 0 10 0;");
        archivedTasksContainer.getChildren().add(tasksHeader);
        for (Task task : archivedTasks) {
            VBox taskCard = createArchivedTaskCard(task);
            archivedTasksContainer.getChildren().add(taskCard);
        }

        kanbanBoard.getChildren().add(archivedTasksContainer);
    }

    private VBox createArchivedTaskCard(Task task) {
        VBox taskCard = new VBox(10);
        taskCard.setPadding(new Insets(15));
        taskCard.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1; -fx-padding: 15;");
        taskCard.setPrefWidth(750);
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setSpacing(10);
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: black;");
        Label priorityLabel = new Label(getPriorityText(task.getPriority()));
        priorityLabel.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; " +
                        "-fx-font-size: 11; -fx-font-weight: bold; " +
                        "-fx-padding: 2 6;",
                getPriorityColor(task.getPriority()),
                getPriorityTextColor(task.getPriority())
        ));
        String updatedText = "";
        if (task.getUpdatedAt() != null) {
            updatedText = "Archived: " + task.getUpdatedAt().format(DATE_FORMATTER);
        }
        Label dateLabel = new Label(updatedText);
        dateLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 12;");
        topRow.getChildren().addAll(titleLabel, priorityLabel);
        HBox.setHgrow(dateLabel, javafx.scene.layout.Priority.ALWAYS);
        topRow.getChildren().add(dateLabel);
        Label descriptionLabel = new Label(task.getDescription() != null ? task.getDescription() : "No description");
        descriptionLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(700);
        Column column = columnDAO.findById(task.getColumnId());
        String columnInfo = column != null ? "Original column: " + column.getTitle() : "Column not found";
        Label columnLabel = new Label(columnInfo);
        columnLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 12; -fx-font-style: italic;");
        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        Button restoreButton = new Button("RESTORE");
        restoreButton.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-cursor: hand; -fx-background-radius: 0;");
        restoreButton.setOnAction(e -> handleRestoreTask(task));
        Button deleteButton = new Button("DELETE");
        deleteButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-cursor: hand; -fx-background-radius: 0;");
        deleteButton.setOnAction(e -> handleDeleteArchivedTask(task));
        actionsRow.getChildren().addAll(restoreButton, deleteButton);
        taskCard.getChildren().addAll(topRow, descriptionLabel, columnLabel, actionsRow);

        return taskCard;
    }

    private void handleRestoreTask(Task task) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Restore task");
        confirmAlert.setHeaderText("Restore task?");
        confirmAlert.setContentText("Task '" + task.getTitle() + "' will be returned to the board.");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (taskDAO.restoreTask(task.getId())) {
                    showSuccess("Task restored: " + task.getTitle());
                    loadColumns();
                } else {
                    showError("Failed to restore task");
                }
            }
        });
    }

    private void handleDeleteArchivedTask(Task task) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete task");
        confirmAlert.setHeaderText("Delete task permanently?");
        confirmAlert.setContentText("Task '" + task.getTitle() + "' will be permanently deleted.\n\nThis action cannot be undone!");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (taskDAO.delete(task.getId())) {
                    showSuccess("Task deleted: " + task.getTitle());
                    loadColumns();
                } else {
                    showError("Failed to delete task");
                }
            }
        });
    }

    private String getPriorityText(Task.Priority priority) {
        switch (priority) {
            case LOW: return "LOW";
            case MEDIUM: return "MEDIUM";
            case HIGH: return "HIGH";
            case CRITICAL: return "CRITICAL";
            default: return "NONE";
        }
    }

    private String getPriorityColor(Task.Priority priority) {
        switch (priority) {
            case LOW: return "#61bd4f";
            case MEDIUM: return "#f2d600";
            case HIGH: return "#ff9f1a";
            case CRITICAL: return "#eb5a46";
            default: return "#333";
        }
    }

    private String getPriorityTextColor(Task.Priority priority) {
        return priority == Task.Priority.MEDIUM ? "black" : "white";
    }

    private void createSimpleColumn(Column column) {
        VBox columnBox = new VBox(10);
        columnBox.setPrefWidth(272);
        columnBox.setStyle("-fx-background-color: #ebecf0; -fx-border-color: black; -fx-border-width: 1;");
        columnBox.setPadding(new Insets(10));
        Label titleLabel = new Label(column.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        VBox tasksContainer = new VBox(8);
        List<Task> tasks = taskDAO.findByColumnId(column.getId());
        for (Task task : tasks) {
            tasksContainer.getChildren().add(createSimpleTaskCard(task));
        }
        Button addTaskBtn = new Button("+ Add card");
        addTaskBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
        addTaskBtn.setOnAction(e -> showNewTaskDialog(column));
        columnBox.getChildren().addAll(titleLabel, tasksContainer, addTaskBtn);
        kanbanBoard.getChildren().add(columnBox);
    }

    private Node createSimpleTaskCard(Task task) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1; -fx-padding: 8;");
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) showTaskDetailDialog(task);
        });
        Label title = new Label(task.getTitle());
        title.setStyle("-fx-font-size: 14;");
        title.setWrapText(true);
        Label desc = new Label(task.getDescription() != null ? task.getDescription() : "");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
        desc.setWrapText(true);
        card.getChildren().addAll(title, desc);
        return card;
    }

    private void addCreateColumnButton() {
        VBox addColumnBox = new VBox();
        addColumnBox.setPrefWidth(272);
        addColumnBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand;");
        addColumnBox.setPadding(new Insets(10));
        addColumnBox.setOnMouseClicked(e -> showNewColumnDialog());
        Label addLabel = new Label("+ Add column");
        addLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        addColumnBox.getChildren().add(addLabel);
        kanbanBoard.getChildren().add(addColumnBox);
    }

    public void showNewTaskDialog(Column column) {
        if (column == null || column.isArchived()) return;
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("New task");
        dialog.setHeaderText("Creating task in: " + column.getTitle());
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        TextField titleField = new TextField();
        titleField.setPromptText("Task name...");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description (optional)...");
        descArea.setPrefRowCount(3);
        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setValue(LocalDate.now().plusDays(7));
        ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Task.Priority.values());
        priorityCombo.setValue(Task.Priority.MEDIUM);
        content.getChildren().addAll(
                new Label("Name*:"), titleField,
                new Label("Description:"), descArea,
                new Label("Due date:"), dueDatePicker,
                new Label("Priority:"), priorityCombo
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
                Task task = new Task(column.getId(), titleField.getText().trim(), descArea.getText().trim());
                task.setDueDate(dueDatePicker.getValue());
                task.setPriority(priorityCombo.getValue());
                return task;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(task -> {
            if (taskDAO.create(task)) {
                loadColumns();
                showSuccess("Task created!");
            } else {
                showError("Failed to create task");
            }
        });
    }

    public void showEditTaskDialog(Task task) {
        if (task == null) return;
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit task");
        dialog.setHeaderText("Editing: " + task.getTitle());
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        TextField titleField = new TextField(task.getTitle());
        TextArea descArea = new TextArea(task.getDescription());
        descArea.setPrefRowCount(4);
        DatePicker dueDatePicker = new DatePicker(task.getDueDate());
        ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Task.Priority.values());
        priorityCombo.setValue(task.getPriority());
        grid.add(new Label("Name:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Due date:"), 0, 2);
        grid.add(dueDatePicker, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal.trim().isEmpty());
        });
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                task.setTitle(titleField.getText().trim());
                task.setDescription(descArea.getText());
                task.setDueDate(dueDatePicker.getValue());
                task.setPriority(priorityCombo.getValue());
                return task;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(updatedTask -> {
            if (taskDAO.update(updatedTask)) {
                loadColumns();
                showSuccess("Task updated!");
            } else {
                showError("Failed to update task");
            }
        });
    }

    public void showTaskDetailDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/task_detail.fxml"));
            Parent root = loader.load();
            TaskDetailController controller = loader.getController();
            controller.setTask(task);
            controller.setMainController(this);
            Stage stage = new Stage();
            stage.setTitle(task.getTitle());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(kanbanBoard.getScene().getWindow());
            stage.setScene(new Scene(root, 800, 600));
            stage.showAndWait();
            loadColumns();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load task details");
        }
    }

    public void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("TaskBoard");
        alert.setContentText(
                "Version: 1.0.0\n" +
                        "Task management system\n" +
                        "2026"
        );
        alert.showAndWait();
    }

    public void showProjectSelector() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/project_selector_dialog.fxml"));
            Parent root = loader.load();
            ProjectSelectorController controller = loader.getController();
            controller.setMainController(this);
            Stage stage = createDialogStage("My boards", root, 500, 600);
            controller.setDialogStage(stage);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load boards list: " + e.getMessage());
        }
    }

    public void showFiltersDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/filters_dialog.fxml"));
            Parent root = loader.load();
            FiltersController controller = loader.getController();
            controller.setMainController(this);
            Stage stage = createDialogStage("Task filters", root, 650, 600);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load filters: " + e.getMessage());
        }
    }

    public void showLabelsDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/labels_dialog.fxml"));
            Parent root = loader.load();
            LabelsController controller = loader.getController();
            controller.setMainController(this);
            Stage stage = createDialogStage("Label management", root, 600, 500);
            stage.showAndWait();
            refreshBoard();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load labels: " + e.getMessage());
        }
    }

    public void showMembersDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/members_dialog.fxml"));
            Parent root = loader.load();
            MembersController controller = loader.getController();
            controller.setMainController(this);
            Stage stage = createDialogStage("Project members", root, 800, 600);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load members: " + e.getMessage());
        }
    }

    public void showAutomationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/automation_dialog.fxml"));
            Parent root = loader.load();
            AutomationController controller = loader.getController();
            controller.setMainController(this);
            Stage stage = createDialogStage("Task automation", root, 750, 650);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load automation: " + e.getMessage());
        }
    }

    public void showHelpDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/help_dialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("TaskBoard Help");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(kanbanBoard.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load help: " + e.getMessage());
        }
    }

    public void showNewColumnDialog() {
        if (showArchive) {
            showInformation("Archive", "Cannot create column in archive!");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New column");
        dialog.setHeaderText("Add new column to board");
        dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(columnName -> {
            if (!columnName.trim().isEmpty()) {
                Column column = new Column(currentProject.getId(), columnName.trim(),
                        columnDAO.getMaxPosition(currentProject.getId()) + 1);
                column.setColor("#333");
                if (columnDAO.create(column)) {
                    loadColumns();
                    showSuccess("Column created!");
                } else {
                    showError("Failed to create column");
                }
            }
        });
    }

    private Stage createDialogStage(String title, Parent root, double width, double height) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(kanbanBoard.getScene().getWindow());
        stage.setScene(new Scene(root, width, height));
        Window owner = stage.getOwner();
        if (owner != null) {
            stage.setX(owner.getX() + (owner.getWidth() - width) / 2);
            stage.setY(owner.getY() + (owner.getHeight() - height) / 2);
        }
        return stage;
    }

    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public TaskDAO getTaskDAO() { return taskDAO; }
    public Project getCurrentProject() { return currentProject; }
    public User getCurrentUser() { return currentUser; }
    public ColumnDAO getColumnDAO() { return columnDAO; }
    public void setCurrentProject(Project project) { this.currentProject = project; }
    public void refreshBoard() { loadColumns(); }

    @FXML
    public void handleUserMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> handleLogout());
        menu.getItems().addAll(logoutItem);
        menu.show(userMenuBtn.getScene().getWindow(), userMenuBtn.localToScreen(0, 0).getX(), userMenuBtn.localToScreen(0, 0).getY() + userMenuBtn.getHeight());
    }

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Exit application?");
        alert.setContentText("All data is saved in the database.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    DatabaseConnection.clearCurrentUser();
                    DatabaseConnection.closeConnection();
                    Stage loginStage = new Stage();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/login.fxml"));
                    Parent root = loader.load();
                    loginStage.setTitle("TaskBoard - Login");
                    loginStage.setScene(new Scene(root, 400, 300));
                    ((Stage) kanbanBoard.getScene().getWindow()).close();
                    loginStage.show();
                } catch (IOException e) {
                    showError("Exit error: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleButtonHover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            button.setOpacity(0.8);
        }
    }

    @FXML
    public void handleButtonExit(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            button.setOpacity(1.0);
        }
    }
}