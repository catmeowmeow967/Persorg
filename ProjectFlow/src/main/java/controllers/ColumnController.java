package controllers;

import dao.TaskDAO;
import dao.ColumnDAO;
import models.Column;
import models.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ColumnController {

    @FXML private TextField columnTitleField;
    @FXML private Label taskCountLabel;
    @FXML private VBox tasksContainer;
    @FXML private Button addTaskButton;
    @FXML private VBox columnContainer;

    private Column column;
    private MainController mainController;
    private TaskDAO taskDAO = new TaskDAO();
    private ColumnDAO columnDAO = new ColumnDAO();

    @FXML
    public void initialize() {
        setupButtonHoverEffects();
    }

    private void setupButtonHoverEffects() {
        addTaskButton.setOnMouseEntered(event -> handleButtonHover(event));
        addTaskButton.setOnMouseExited(event -> handleButtonExit(event));
    }

    @FXML
    public void handleButtonHover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            if (button == addTaskButton && column != null && column.getColor() != null) {
                button.setStyle("-fx-background-color: " + column.getColor() + "40;");
            } else {
                button.setStyle(button.getStyle().replace("rgba(255,255,255,0.2)", "rgba(255,255,255,0.3)").replace("rgba(0,0,0,0.06)", "rgba(0,0,0,0.1)"));
            }
        }
    }

    @FXML
    public void handleButtonExit(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            if (button == addTaskButton && column != null && column.getColor() != null) {
                button.setStyle("-fx-background-color: " + column.getColor() + "20;");
            } else {
                button.setStyle(button.getStyle().replace("rgba(255,255,255,0.3)", "rgba(255,255,255,0.2)").replace("rgba(0,0,0,0.1)", "rgba(0,0,0,0.06)"));
            }
        }
    }

    @FXML
    public void handleTemplateTask() {
        if (mainController != null && column != null) {
            Task task = new Task(column.getId(), "New Task", "");
            task.setPriority(Task.Priority.MEDIUM);
            task.setDueDate(LocalDate.now().plusDays(7));
            if (taskDAO.create(task)) {
                loadTasks();
            }
        }
    }

    public void setColumn(Column column, MainController mainController) {
        this.column = column;
        this.mainController = mainController;
        updateUI();
    }

    private void updateUI() {
        if (column == null) return;
        columnTitleField.setText(column.getTitle());
        applyColumnColor();
        loadTasks();
    }

    private void applyColumnColor() {
        if (column.getColor() != null && !column.getColor().isEmpty()) {
            try {
                String lightColor = getLightColorVariant(column.getColor());
                columnContainer.setStyle(
                        "-fx-background-color: " + lightColor + ";" +
                                "-fx-border-color: " + column.getColor() + "40;" +
                                "-fx-border-width: 1;" +
                                "-fx-padding: 0;"
                );
                columnTitleField.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: transparent;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14;" +
                                "-fx-text-fill: " + getContrastColor(column.getColor()) + ";" +
                                "-fx-padding: 2 0;" +
                                "-fx-pref-width: 180;"
                );
                taskCountLabel.setStyle(
                        "-fx-text-fill: " + getContrastColor(column.getColor()) + ";" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 4 8;" +
                                "-fx-background-color: " + column.getColor() + "40;"
                );
                addTaskButton.setStyle(
                        "-fx-background-color: " + column.getColor() + "20;" +
                                "-fx-text-fill: " + getContrastColor(column.getColor()) + ";" +
                                "-fx-border-color: " + column.getColor() + "40;" +
                                "-fx-font-size: 14;" +
                                "-fx-cursor: hand;" +
                                "-fx-alignment: center-left;"
                );
            } catch (Exception e) {
                System.err.println("Color error: " + e.getMessage());
                setDefaultStyles();
            }
        } else {
            setDefaultStyles();
        }
    }

    private String getLightColorVariant(String hexColor) {
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            r = Math.min(255, r + 100);
            g = Math.min(255, g + 100);
            b = Math.min(255, b + 100);
            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            return "#f5f5f5";
        }
    }

    private String getContrastColor(String hexColor) {
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            double yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
            return (yiq >= 150) ? "black" : "white";
        } catch (Exception e) {
            return "black";
        }
    }

    private void setDefaultStyles() {
        columnContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: black; -fx-border-width: 1;");
        columnTitleField.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        taskCountLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 12;");
        addTaskButton.setStyle("-fx-text-fill: #333; -fx-font-size: 14;");
    }

    public void loadTasks() {
        tasksContainer.getChildren().clear();
        if (column == null) return;
        List<Task> tasks = taskDAO.findByColumnId(column.getId());
        taskCountLabel.setText(String.valueOf(tasks.size()));

        for (Task task : tasks) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectflow/task_card.fxml"));
                VBox taskCard = loader.load();
                TaskCardController controller = loader.getController();
                controller.setTask(task, this);
                tasksContainer.getChildren().add(taskCard);
            } catch (IOException e) {
                System.err.println("Failed to load card: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleAddTask() {
        if (mainController != null && column != null) {
            mainController.showNewTaskDialog(column);
        }
    }

    @FXML
    public void handleTitleUpdate() {
        if (column != null && mainController != null) {
            String newTitle = columnTitleField.getText().trim();
            if (!newTitle.isEmpty() && !newTitle.equals(column.getTitle())) {
                column.setTitle(newTitle);
                if (columnDAO.update(column)) {
                    mainController.refreshBoard();
                }
            }
        }
    }

    @FXML
    public void handleMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> columnTitleField.requestFocus());
        menu.getItems().add(renameItem);
        menu.show(columnTitleField, 0, columnTitleField.getHeight());
    }

    public void refresh() {
        loadTasks();
    }

    public int getColumnId() {
        return column != null ? column.getId() : -1;
    }

    public String getColumnTitle() {
        return column != null ? column.getTitle() : "";
    }

    public void setAddTaskButtonVisible(boolean visible) {
        addTaskButton.setVisible(visible);
    }

    public MainController getMainController() {
        return mainController;
    }

    public TaskDAO getTaskDAO() {
        return taskDAO;
    }

    public Column getColumn() {
        return column;
    }
}