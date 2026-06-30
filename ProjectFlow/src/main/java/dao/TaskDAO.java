package dao;

import models.Task;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> findByColumnId(int columnId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE column_id = ? AND is_archived = FALSE ORDER BY position, created_at";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, columnId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске задач колонки: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    public Task findById(int id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске задачи по ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Task> findArchivedTasksByProjectId(int projectId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.* FROM tasks t " +
                "JOIN columns c ON t.column_id = c.id " +
                "WHERE c.project_id = ? AND t.is_archived = TRUE " +
                "ORDER BY t.updated_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске архивных задач проекта: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    public boolean archiveTask(int taskId) {
        String sql = "UPDATE tasks SET is_archived = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            int result = stmt.executeUpdate();
            System.out.println("Задача ID=" + taskId + " архивирована. Затронуто строк: " + result);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при архивации задачи: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean restoreTask(int taskId) {
        String sql = "UPDATE tasks SET is_archived = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            int result = stmt.executeUpdate();
            System.out.println("Задача ID=" + taskId + " восстановлена. Затронуто строк: " + result);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при восстановлении задачи: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Task> findByProjectId(int projectId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.* FROM tasks t " +
                "JOIN columns c ON t.column_id = c.id " +
                "WHERE c.project_id = ? AND t.is_archived = FALSE ORDER BY t.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске задач проекта: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    public boolean create(Task task) {
        String sql = "INSERT INTO tasks (column_id, title, description, position, priority, due_date, " +
                "is_completed, is_archived) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, task.getColumnId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setInt(4, task.getPosition());
            stmt.setString(5, task.getPriority().name().toLowerCase());
            if (task.getDueDate() != null) {
                stmt.setDate(6, Date.valueOf(task.getDueDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            stmt.setBoolean(7, task.isCompleted());
            stmt.setBoolean(8, task.isArchived());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании задачи: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Task task) {
        String sql = "UPDATE tasks SET column_id = ?, title = ?, description = ?, position = ?, " +
                "priority = ?, due_date = ?, is_completed = ?, is_archived = ?, " +
                "completed_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, task.getColumnId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setInt(4, task.getPosition());
            stmt.setString(5, task.getPriority().name().toLowerCase());
            if (task.getDueDate() != null) {
                stmt.setDate(6, Date.valueOf(task.getDueDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            stmt.setBoolean(7, task.isCompleted());
            stmt.setBoolean(8, task.isArchived());
            if (task.getCompletedAt() != null) {
                stmt.setTimestamp(9, Timestamp.valueOf(task.getCompletedAt()));
            } else {
                stmt.setNull(9, Types.TIMESTAMP);
            }
            stmt.setInt(10, task.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении задачи: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении задачи: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByColumnId(int columnId) {
        String sql = "DELETE FROM tasks WHERE column_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, columnId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении задач колонки: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Task> searchInProject(int projectId, String query) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.* FROM tasks t " +
                "JOIN columns c ON t.column_id = c.id " +
                "WHERE c.project_id = ? AND (t.title LIKE ? OR t.description LIKE ?) " +
                "AND t.is_archived = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка поиска задач: " + e.getMessage());
        }
        return tasks;
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setColumnId(rs.getInt("column_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setPosition(rs.getInt("position"));
        String priorityStr = rs.getString("priority");
        if (priorityStr != null) {
            try {
                task.setPriority(Task.Priority.valueOf(priorityStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                task.setPriority(Task.Priority.MEDIUM);
            }
        }
        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            task.setDueDate(dueDate.toLocalDate());
        }
        task.setCompleted(rs.getBoolean("is_completed"));
        task.setArchived(rs.getBoolean("is_archived"));
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            task.setCompletedAt(completedAt.toLocalDateTime());
        }
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            task.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            task.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return task;
    }
}