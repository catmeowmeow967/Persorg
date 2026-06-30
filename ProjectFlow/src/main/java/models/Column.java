package models;

import java.time.LocalDateTime;

public class Column {
    private int id;
    private int projectId;
    private String title;
    private String color;
    private int position;
    private LocalDateTime createdAt;
    private boolean isArchived;
    private LocalDateTime archivedAt;

    public Column() {}

    public Column(int projectId, String title, int position) {
        this.projectId = projectId;
        this.title = title;
        this.position = position;
        this.color = "#5e6c84";
        this.isArchived = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }

    @Override
    public String toString() {
        return title + (isArchived ? " (Архив)" : "");
    }
}