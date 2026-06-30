package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.AutomationRule;
import java.util.ArrayList;
import java.util.List;

public class AutomationController {

    @FXML private ComboBox<String> triggerCombo;
    @FXML private ComboBox<String> actionCombo;
    @FXML private TextField parametersField;
    @FXML private Button createRuleButton;
    @FXML private TableView<AutomationRule> rulesTable;
    @FXML private Label statsLabel;
    @FXML private TextArea ruleDescriptionArea;
    private final ObservableList<AutomationRule> rules = FXCollections.observableArrayList();
    private final List<AutomationRule> executedRules = new ArrayList<>();
    private MainController mainController;

    @FXML
    public void initialize() {
        System.out.println("AutomationController initialized");
        setupEventHandlers();
        setupRulesTable();
        loadTriggersAndActions();
        loadExampleRules();
        updateStats();
    }

    private void setupEventHandlers() {
        createRuleButton.setOnAction(event -> createRule());
        triggerCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateRuleDescription());
        actionCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateRuleDescription());
        parametersField.textProperty().addListener((obs, oldVal, newVal) -> updateRuleDescription());
    }

    private void updateRuleDescription() {
        String trigger = triggerCombo.getValue();
        String action = actionCombo.getValue();
        String params = parametersField.getText();
        if (trigger == null || action == null) return;
        String description = "When: " + trigger + "\n" +
                "Then: " + action + "\n" +
                "Params: " + (params.isEmpty() ? "[none]" : params);
        if (ruleDescriptionArea != null) {
            ruleDescriptionArea.setText(description);
        }
    }

    private void loadTriggersAndActions() {
        triggerCombo.setItems(FXCollections.observableArrayList("Task Created", "Task Moved", "Due Date Passed", "Priority Changed", "Task Completed"));
        triggerCombo.setValue("Task Created");
        actionCombo.setItems(FXCollections.observableArrayList("Add Label", "Change Priority", "Move to Column", "Assign Member", "Set Due Date"));
        actionCombo.setValue("Add Label");
    }

    private void setupRulesTable() {
        rulesTable.setItems(rules);
        TableColumn<AutomationRule, String> ruleCol = new TableColumn<>("Rule");
        ruleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        ruleCol.setPrefWidth(350);
        TableColumn<AutomationRule, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        TableColumn<AutomationRule, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        rulesTable.getColumns().setAll(ruleCol, statusCol, actionsCol);
    }

    private void loadExampleRules() {
        rules.add(new AutomationRule("Due Date Passed", "Change Priority", "High", "Active"));
        rules.add(new AutomationRule("Task Created", "Add Label", "New", "Inactive"));
        rules.add(new AutomationRule("Task Completed", "Move to Column", "Done", "Active"));
        rules.get(0).incrementExecution();
        rules.get(2).incrementExecution();
    }

    @FXML
    public void createRule() {
        String trigger = triggerCombo.getValue();
        String action = actionCombo.getValue();
        String parameters = parametersField.getText().trim();
        if (trigger == null || action == null) {
            showAlert("Error", "Select trigger and action", Alert.AlertType.ERROR);
            return;
        }
        if (parameters.isEmpty()) {
            showAlert("Error", "Enter parameters", Alert.AlertType.ERROR);
            return;
        }
        for (AutomationRule rule : rules) {
            if (rule.getTrigger().equals(trigger) && rule.getAction().equals(action) &&
                    rule.getParameters().equalsIgnoreCase(parameters)) {
                showAlert("Error", "Rule already exists", Alert.AlertType.ERROR);
                return;
            }
        }
        AutomationRule rule = new AutomationRule(trigger, action, parameters, "Active");
        rules.add(rule);
        parametersField.clear();
        updateStats();
        showSuccess("Rule created");
        simulateRuleExecution(rule);
    }

    private void simulateRuleExecution(AutomationRule rule) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    rule.incrementExecution();
                    rulesTable.refresh();
                    updateStats();
                    if (mainController != null) {
                        mainController.showSuccess("Automation executed: " + rule.getTrigger());
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateStats() {
        int active = (int) rules.stream().filter(r -> r.getStatus().equals("Active")).count();
        int totalExecutions = rules.stream().mapToInt(AutomationRule::getExecutionCount).sum();
        if (statsLabel != null) {
            statsLabel.setText(String.format("%d rules (%d active) | Executed: %d", rules.size(), active, totalExecutions));
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        showAlert("Success", message, Alert.AlertType.INFORMATION);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) triggerCombo.getScene().getWindow();
        stage.close();
    }
}