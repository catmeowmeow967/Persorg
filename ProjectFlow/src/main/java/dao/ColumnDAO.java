package dao;

import models.Column;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ColumnDAO {

    public List<Column> findByProjectId(int projectId) {
        return findByProjectId(projectId, false);
    }

    public List<Column> findByProjectId(int projectId, boolean includeArchived) {
        List<Column> columns = new ArrayList<>();
        String sql = "SELECT * FROM columns WHERE project_id = ? " +
                (includeArchived ? "" : "AND is_archived = FALSE ") +
                "ORDER BY position";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                columns.add(mapResultSetToColumn(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске колонок проекта: " + e.getMessage());
            e.printStackTrace();
        }
        return columns;
    }

    public List<Column> findArchivedByProjectId(int projectId) {
        List<Column> columns = new ArrayList<>();
        String sql = "SELECT * FROM columns WHERE project_id = ? AND is_archived = TRUE ORDER BY archived_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                columns.add(mapResultSetToColumn(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске архивных колонок: " + e.getMessage());
            e.printStackTrace();
        }
        return columns;
    }

    public Column findById(int id) {
        String sql = "SELECT * FROM columns WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToColumn(rs);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске колонки по ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Column column) {
        String sql = "INSERT INTO columns (project_id, title, color, position, is_archived) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, column.getProjectId());
            stmt.setString(2, column.getTitle());
            stmt.setString(3, column.getColor());
            stmt.setInt(4, column.getPosition());
            stmt.setBoolean(5, column.isArchived());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        column.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании колонки: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Column column) {
        String sql = "UPDATE columns SET title = ?, color = ?, position = ?, is_archived = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, column.getTitle());
            stmt.setString(2, column.getColor());
            stmt.setInt(3, column.getPosition());
            stmt.setBoolean(4, column.isArchived());
            stmt.setInt(5, column.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении колонки: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean archive(int columnId) {
        String sql = "UPDATE columns SET is_archived = TRUE, archived_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, columnId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при архивации колонки: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean restore(int columnId) {
        String sql = "UPDATE columns SET is_archived = FALSE, archived_at = NULL WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, columnId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при восстановлении колонки: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM columns WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении колонки: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByProjectId(int projectId) {
        String sql = "DELETE FROM columns WHERE project_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении колонок проекта: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public int countColumnsByProjectId(int projectId) {
        return countColumnsByProjectId(projectId, false);
    }

    public int countColumnsByProjectId(int projectId, boolean includeArchived) {
        String sql = "SELECT COUNT(*) as count FROM columns WHERE project_id = ? " +
                (includeArchived ? "" : "AND is_archived = FALSE");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете колонок: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getMaxPosition(int projectId) {
        String sql = "SELECT MAX(position) as max_position FROM columns WHERE project_id = ? AND is_archived = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("max_position");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении максимальной позиции: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private Column mapResultSetToColumn(ResultSet rs) throws SQLException {
        Column column = new Column();
        column.setId(rs.getInt("id"));
        column.setProjectId(rs.getInt("project_id"));
        column.setTitle(rs.getString("title"));
        column.setColor(rs.getString("color"));
        column.setPosition(rs.getInt("position"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            column.setCreatedAt(createdAt.toLocalDateTime());
        }
        column.setArchived(rs.getBoolean("is_archived"));
        Timestamp archivedAt = rs.getTimestamp("archived_at");
        if (archivedAt != null) {
            column.setArchivedAt(archivedAt.toLocalDateTime());
        }
        return column;
    }
}