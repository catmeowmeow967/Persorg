package dao;

import models.Label;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LabelDAO {

    public List<Label> findAll() {
        List<Label> labels = new ArrayList<>();
        String sql = "SELECT * FROM labels ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                labels.add(mapResultSetToLabel(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки меток: " + e.getMessage());
        }
        return labels;
    }

    public boolean create(Label label) {
        String sql = "INSERT INTO labels (name, color, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, label.getName());
            stmt.setString(2, label.getColor());
            stmt.setString(3, label.getDescription());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        label.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка создания метки: " + e.getMessage());
        }
        return false;
    }

    private Label mapResultSetToLabel(ResultSet rs) throws SQLException {
        return new Label(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("color"),
                rs.getString("description")
        );
    }
}