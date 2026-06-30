package dao;

import models.Project;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    public List<Project> findByUserId(int userId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE user_id = ? AND is_archived = FALSE ORDER BY position";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске проектов пользователя: " + e.getMessage());
        }
        return projects;
    }

    public Project findById(int id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProject(rs);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске проекта по ID: " + e.getMessage());
        }
        return null;
    }

    public List<Project> findAllArchived(int userId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE user_id = ? AND is_archived = TRUE ORDER BY updated_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске архивных проектов: " + e.getMessage());
        }
        return projects;
    }

    public boolean create(Project project) {
        String sql = "INSERT INTO projects (user_id, title, description, color, position) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, project.getUserId());
            stmt.setString(2, project.getTitle());
            stmt.setString(3, project.getDescription());
            stmt.setString(4, project.getColor());
            stmt.setInt(5, project.getPosition());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        project.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании проекта: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Project project) {
        String sql = "UPDATE projects SET title = ?, description = ?, color = ?, position = ?, " +
                "is_archived = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, project.getTitle());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getColor());
            stmt.setInt(4, project.getPosition());
            stmt.setBoolean(5, project.isArchived());
            stmt.setInt(6, project.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении проекта: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении проекта: " + e.getMessage());
        }
        return false;
    }

    public int countProjectsByUserId(int userId) {
        String sql = "SELECT COUNT(*) as count FROM projects WHERE user_id = ? AND is_archived = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете проектов: " + e.getMessage());
        }
        return 0;
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setUserId(rs.getInt("user_id"));
        project.setTitle(rs.getString("title"));
        project.setDescription(rs.getString("description"));
        project.setColor(rs.getString("color"));
        project.setPosition(rs.getInt("position"));
        project.setArchived(rs.getBoolean("is_archived"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            project.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            project.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return project;
    }
}