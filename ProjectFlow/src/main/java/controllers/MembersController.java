package controllers;

import dao.ProjectMemberDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.ProjectMember;
import java.util.List;

public class MembersController {

    @FXML private TextField inviteEmailField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button inviteButton;
    @FXML private TableView<ProjectMember> membersTable;
    @FXML private Label projectOwnerLabel;
    @FXML private Label totalMembersLabel;
    @FXML private TextField searchMemberField;

    private MainController mainController;
    private final ProjectMemberDAO memberDAO = new ProjectMemberDAO();
    private final ObservableList<ProjectMember> members = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("MembersController initialized");
        setupEventHandlers();
        setupMembersTable();
        loadMembers();
        roleCombo.setValue("Member");
        searchMemberField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterMembers(newVal);
        });
    }

    private void setupEventHandlers() {
        inviteButton.setOnAction(event -> inviteMember());
        inviteEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.contains("@")) {
                inviteEmailField.setTooltip(new Tooltip("Will use: " + newVal + "@taskboard.local"));
            }
        });
    }

    private void setupMembersTable() {
        membersTable.setItems(members);
        TableColumn<ProjectMember, String> userCol = new TableColumn<>("Member");
        userCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        userCol.setPrefWidth(200);
        TableColumn<ProjectMember, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
        roleCol.setPrefWidth(150);
        TableColumn<ProjectMember, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        TableColumn<ProjectMember, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        membersTable.getColumns().setAll(userCol, roleCol, statusCol, actionsCol);
    }

    private void loadMembers() {
        if (mainController == null || mainController.getCurrentProject() == null) return;
        members.clear();
        List<ProjectMember> projectMembers = memberDAO.findByProjectId(mainController.getCurrentProject().getId());
        members.addAll(projectMembers);
        if (members.isEmpty()) {
            addTestMember("Project Owner", "owner@taskboard.local", "Owner", "Active");
            addTestMember("Alex Ivanov", "alex@taskboard.local", "Admin", "Active");
            addTestMember("Maria Petrova", "maria@taskboard.local", "Member", "Active");
        }
        updateStats();
    }

    private void addTestMember(String username, String email, String role, String status) {
        ProjectMember member = new ProjectMember(0,
                mainController.getCurrentProject().getId(),
                username, email, role, status);
        if (memberDAO.create(member)) {
            members.add(member);
        }
    }

    private void filterMembers(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            membersTable.setItems(members);
            return;
        }
        String lowerSearch = searchText.toLowerCase();
        ObservableList<ProjectMember> filtered = FXCollections.observableArrayList();
        for (ProjectMember member : members) {
            if (member.getUsername().toLowerCase().contains(lowerSearch) ||
                    member.getEmail().toLowerCase().contains(lowerSearch) ||
                    member.getRole().toLowerCase().contains(lowerSearch)) {
                filtered.add(member);
            }
        }
        membersTable.setItems(filtered);
        totalMembersLabel.setText("Found: " + filtered.size());
    }

    @FXML
    public void inviteMember() {
        String email = inviteEmailField.getText().trim();
        String role = roleCombo.getValue();
        if (email.isEmpty()) {
            showAlert("Error", "Enter email", Alert.AlertType.ERROR);
            return;
        }
        if (!email.contains("@")) {
            email += "@taskboard.local";
        }
        if (mainController == null || mainController.getCurrentUser() == null) return;
        for (ProjectMember member : members) {
            if (member.getEmail().equalsIgnoreCase(email)) {
                showAlert("Error", "Member already in project", Alert.AlertType.ERROR);
                return;
            }
        }
        String username = email.split("@")[0];
        ProjectMember newMember = new ProjectMember(0, mainController.getCurrentProject().getId(), username, email, role, "Pending");
        if (memberDAO.create(newMember)) {
            members.add(newMember);
            inviteEmailField.clear();
            updateStats();
            showSuccess("Invitation sent: " + email);
        } else {
            showError("Failed to send invitation");
        }
    }

    private void updateStats() {
        int active = (int) members.stream().filter(m -> m.getStatus().equals("Active")).count();
        int pending = (int) members.stream().filter(m -> m.getStatus().equals("Pending")).count();
        projectOwnerLabel.setText("Owner: " + members.stream().filter(m -> m.getRole().equals("Owner")).findFirst().map(ProjectMember::getUsername).orElse("Unknown"));
        totalMembersLabel.setText("Total: " + members.size() + " (Active: " + active + ", Pending: " + pending + ")");
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

    private void showError(String message) {
        showAlert("Error", message, Alert.AlertType.ERROR);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        loadMembers();
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) inviteEmailField.getScene().getWindow();
        stage.close();
    }
}