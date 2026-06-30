package dao;

import models.Attachment;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttachmentDAO {

    public List<Attachment> findByTaskId(int taskId) {
        List<Attachment> attachments = new ArrayList<>();
        String sql = "SELECT * FROM attachments WHERE task_id = ? ORDER BY uploaded_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attachments.add(mapResultSetToAttachment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки вложений: " + e.getMessage());
        }
        return attachments;
    }

    public boolean create(Attachment attachment) {
        String sql = "INSERT INTO attachments (task_id, filename, file_path, file_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attachment.getTaskId());
            stmt.setString(2, attachment.getFilename());
            stmt.setString(3, attachment.getFilePath());
            stmt.setString(4, attachment.getFileType());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        attachment.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка создания вложения: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM attachments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка удаления вложения: " + e.getMessage());
        }
        return false;
    }

    private Attachment mapResultSetToAttachment(ResultSet rs) throws SQLException {
        Attachment attachment = new Attachment();
        attachment.setId(rs.getInt("id"));
        attachment.setTaskId(rs.getInt("task_id"));
        attachment.setFilename(rs.getString("filename"));
        attachment.setFilePath(rs.getString("file_path"));
        attachment.setFileType(rs.getString("file_type"));
        Timestamp timestamp = rs.getTimestamp("uploaded_at");
        if (timestamp != null) {
            attachment.setUploadedAt(timestamp.toLocalDateTime());
        }
        return attachment;
    }
}