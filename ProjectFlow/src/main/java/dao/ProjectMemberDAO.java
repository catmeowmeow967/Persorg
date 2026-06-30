package dao;

import models.ProjectMember;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectMemberDAO {

    public List<ProjectMember> findByProjectId(int projectId) {
        List<ProjectMember> members = new ArrayList<>();
        String sql = "SELECT * FROM project_members WHERE project_id = ? ORDER BY role, username";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки участников: " + e.getMessage());
        }
        return members;
    }

    public boolean create(ProjectMember member) {
        String sql = "INSERT INTO project_members (project_id, username, email, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, member.getProjectId());
            stmt.setString(2, member.getUsername());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getRole());
            stmt.setString(5, member.getStatus());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        member.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления участника: " + e.getMessage());
        }
        return false;
    }

    private ProjectMember mapResultSetToMember(ResultSet rs) throws SQLException {
        return new ProjectMember(
                rs.getInt("id"),
                rs.getInt("project_id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getString("status")
        );
    }
}