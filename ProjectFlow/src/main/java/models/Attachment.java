package models;

import java.time.LocalDateTime;

public class Attachment {
    private int id;
    private int taskId;
    private String filename;
    private String filePath;
    private String fileType;
    private LocalDateTime uploadedAt;
    public Attachment() {}

    public Attachment(int taskId, String filename, String filePath, String fileType) {
        this.taskId = taskId;
        this.filename = filename;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    @Override
    public String toString() {
        return filename;
    }
}