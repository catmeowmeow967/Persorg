package controllers;

import dao.TaskDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Task;
import models.Project;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FiltersController {

    @FXML private TextField searchField;
    @FXML private CheckBox priorityLow;
    @FXML private CheckBox priorityMedium;
    @FXML private CheckBox priorityHigh;
    @FXML private CheckBox priorityCritical;
    @FXML private RadioButton dueDateAny;
    @FXML private RadioButton dueDateToday;
    @FXML private RadioButton dueDateWeek;
    @FXML private RadioButton dueDateOverdue;
    @FXML private RadioButton statusAll;
    @FXML private RadioButton statusActive;
    @FXML private RadioButton statusCompleted;
    @FXML private Button applyButton;
    @FXML private Button resetButton;
    @FXML private Button todayButton;
    @FXML private Button overdueButton;
    @FXML private Button completedButton;

    private MainController mainController;
    private final TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        System.out.println("FiltersController initialized");
        ToggleGroup dueDateGroup = new ToggleGroup();
        dueDateAny.setToggleGroup(dueDateGroup);
        dueDateToday.setToggleGroup(dueDateGroup);
        dueDateWeek.setToggleGroup(dueDateGroup);
        dueDateOverdue.setToggleGroup(dueDateGroup);
        dueDateAny.setSelected(true);
        ToggleGroup statusGroup = new ToggleGroup();
        statusAll.setToggleGroup(statusGroup);
        statusActive.setToggleGroup(statusGroup);
        statusCompleted.setToggleGroup(statusGroup);
        statusAll.setSelected(true);
        applyButton.setOnAction(event -> applyFilters());
        resetButton.setOnAction(event -> resetFilters());
        todayButton.setOnAction(event -> setQuickFilterToday());
        overdueButton.setOnAction(event -> setQuickFilterOverdue());
        completedButton.setOnAction(event -> setQuickFilterCompleted());
    }

    @FXML
    public void applyFilters() {
        if (mainController == null || mainController.getCurrentProject() == null) {
            showAlert("Error", "No active project", Alert.AlertType.ERROR);
            return;
        }
        List<Task> filteredTasks = filterTasks();
        showFilterResults(filteredTasks);
        mainController.refreshBoard();
    }

    @FXML
    public void resetFilters() {
        searchField.clear();
        priorityLow.setSelected(false);
        priorityMedium.setSelected(false);
        priorityHigh.setSelected(false);
        priorityCritical.setSelected(false);
        dueDateAny.setSelected(true);
        statusAll.setSelected(true);
    }

    private void setQuickFilter(String filterType) {
        resetFilters();
        switch (filterType) {
            case "today":
                dueDateToday.setSelected(true);
                break;
            case "overdue":
                dueDateOverdue.setSelected(true);
                break;
            case "completed":
                statusCompleted.setSelected(true);
                break;
        }
        applyFilters();
    }

    @FXML
    public void setQuickFilterToday() {
        setQuickFilter("today");
    }

    @FXML
    public void setQuickFilterOverdue() {
        setQuickFilter("overdue");
    }

    @FXML
    public void setQuickFilterCompleted() {
        setQuickFilter("completed");
    }

    private List<Task> filterTasks() {
        List<Task> filteredTasks = new ArrayList<>();
        Project currentProject = mainController.getCurrentProject();
        if (currentProject == null) return filteredTasks;
        List<Task> allTasks = taskDAO.findByProjectId(currentProject.getId());
        for (Task task : allTasks) {
            if (matchesAllFilters(task)) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    private boolean matchesAllFilters(Task task) {
        return matchesSearch(task) &&
                matchesPriority(task) &&
                matchesDueDate(task) &&
                matchesStatus(task);
    }

    private boolean matchesSearch(Task task) {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) return true;
        return task.getTitle().toLowerCase().contains(searchText) ||
                (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchText));
    }

    private boolean matchesPriority(Task task) {
        if (!priorityLow.isSelected() && !priorityMedium.isSelected() &&
                !priorityHigh.isSelected() && !priorityCritical.isSelected()) {
            return true;
        }

        if (task.getPriority() == null) return false;
        switch (task.getPriority()) {
            case LOW: return priorityLow.isSelected();
            case MEDIUM: return priorityMedium.isSelected();
            case HIGH: return priorityHigh.isSelected();
            case CRITICAL: return priorityCritical.isSelected();
            default: return false;
        }
    }

    private boolean matchesDueDate(Task task) {
        if (dueDateAny.isSelected()) return true;
        if (task.getDueDate() == null) return false;
        LocalDate today = LocalDate.now();
        if (dueDateToday.isSelected()) {
            return task.getDueDate().equals(today);
        }
        if (dueDateWeek.isSelected()) {
            LocalDate weekEnd = today.plusDays(7);
            return !task.getDueDate().isBefore(today) && !task.getDueDate().isAfter(weekEnd);
        }
        if (dueDateOverdue.isSelected()) {
            return task.getDueDate().isBefore(today) && !task.isCompleted();
        }
        return true;
    }

    private boolean matchesStatus(Task task) {
        if (statusAll.isSelected()) return true;
        if (statusActive.isSelected()) return !task.isCompleted();
        if (statusCompleted.isSelected()) return task.isCompleted();
        return true;
    }

    private void showFilterResults(List<Task> tasks) {
        StringBuilder message = new StringBuilder();
        message.append("Found: ").append(tasks.size()).append("\n\n");
        if (tasks.isEmpty()) {
            message.append("No tasks found.");
        } else {
            message.append("Results:\n");
            for (int i = 0; i < Math.min(tasks.size(), 10); i++) {
                Task task = tasks.get(i);
                String status = task.isCompleted() ? "[D]" : "[P]";
                String due = task.getDueDate() != null ? " (due " + task.getDueDate() + ")" : "";
                message.append(i + 1).append(". ").append(status).append(" ")
                        .append(task.getTitle()).append(due).append("\n");
            }
            if (tasks.size() > 10) {
                message.append("\n... and ").append(tasks.size() - 10).append(" more");
            }
        }
        showAlert("Filter Results", message.toString(), Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }
}