package models;

public class ProjectMember {
    private int id;
    private int projectId;
    private String username;
    private String email;
    private String role;
    private String status;
    public ProjectMember() {}

    public ProjectMember(int id, int projectId, String username, String email, String role, String status) {
        this.id = id;
        this.projectId = projectId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}