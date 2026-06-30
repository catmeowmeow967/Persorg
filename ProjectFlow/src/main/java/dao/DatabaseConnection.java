package dao;

import models.User;
import java.sql.*;

public class DatabaseConnection {
    private static Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/projectflow_db";
    private static final String USER = "root";
    private static final String PASSWORD = "yynao-YAY22";
    private static User currentUser = null;
    private static int currentUserId = 0;
    private static String currentUsername = "";
    private static String currentUserEmail = "";

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DatabaseConnection] Создание нового подключения к БД...");
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    System.err.println("[DatabaseConnection] MySQL Driver не найден!");
                    throw new RuntimeException("MySQL Driver не найден", e);
                }
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DatabaseConnection] Подключение к БД успешно создано");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Ошибка подключения к БД: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            currentUserId = user.getId();
            currentUsername = user.getUsername();
            currentUserEmail = user.getEmail();
            System.out.println("[DatabaseConnection] Установлен текущий пользователь: " +
                    currentUsername + " (ID: " + currentUserId + ")");
        } else {
            clearCurrentUser();
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserId() {
        if (currentUserId == 0) {
            System.err.println("[DatabaseConnection] ВНИМАНИЕ: user_id = 0! Проверьте аутентификацию.");
        }
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static boolean isUserLoggedIn() {
        return currentUser != null && currentUserId > 0;
    }

    public static void clearCurrentUser() {
        System.out.println("[DatabaseConnection] Очистка данных пользователя: " + currentUsername);
        currentUser = null;
        currentUserId = 0;
        currentUsername = "";
        currentUserEmail = "";
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("[DatabaseConnection] Не удалось получить соединение");
                return false;
            }
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            rs.close();
            stmt.close();
            System.out.println("[DatabaseConnection] Тест подключения успешен");
            return true;
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Ошибка тестирования подключения: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[DatabaseConnection] Общая ошибка при тестировании: " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    try (Statement stmt = connection.createStatement()) {
                        try (ResultSet rs = stmt.executeQuery("SHOW PROCESSLIST")) {
                        }
                    }
                    connection.close();
                    System.out.println("[DatabaseConnection] Подключение к БД закрыто");
                }
            } catch (SQLException e) {
                System.err.println("[DatabaseConnection] Ошибка при закрытии соединения: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    public static ResultSet executeQuery(String sql) throws SQLException {
        Connection conn = getConnection();
        if (conn == null) {
            throw new SQLException("Нет подключения к базе данных");
        }
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        if (conn == null) {
            throw new SQLException("Нет подключения к базе данных");
        }
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }

    public static int executeUpdate(String sql) throws SQLException {
        Connection conn = getConnection();
        if (conn == null) {
            throw new SQLException("Нет подключения к базе данных");
        }
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        if (conn == null) {
            throw new SQLException("Нет подключения к базе данных");
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    public static ResultSet executeInsert(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        if (conn == null) {
            throw new SQLException("Нет подключения к базе данных");
        }
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        stmt.executeUpdate();
        return stmt.getGeneratedKeys();
    }

    public static void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        if (conn != null) {
            conn.setAutoCommit(false);
            System.out.println("[DatabaseConnection] Начало транзакции");
        }
    }

    public static void commitTransaction() throws SQLException {
        Connection conn = getConnection();
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("[DatabaseConnection] Транзакция зафиксирована");
        }
    }

    public static void rollbackTransaction() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                conn.rollback();
                conn.setAutoCommit(true);
                System.out.println("[DatabaseConnection] Транзакция откачена");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Ошибка при откате транзакции: " + e.getMessage());
        }
    }

    public static boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Ошибка проверки таблицы: " + e.getMessage());
        }
        return false;
    }

    public static int getRowCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Ошибка подсчета строк: " + e.getMessage());
        }
        return 0;
    }

    public static void clearTable(String tableName) {
        String sql = "DELETE FROM " + tableName;
        try {
            executeUpdate(sql);
            System.out.println("[DatabaseConnection] Таблица " + tableName + " очищена");
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Ошибка очистки таблицы: " + e.getMessage());
        }
    }

    public static String getDatabaseInfo() {
        StringBuilder info = new StringBuilder();
        try (Connection conn = getConnection()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                info.append("Database URL: ").append(meta.getURL()).append("\n");
                info.append("Database Product: ").append(meta.getDatabaseProductName()).append("\n");
                info.append("Database Version: ").append(meta.getDatabaseProductVersion()).append("\n");
                info.append("Driver Name: ").append(meta.getDriverName()).append("\n");
                info.append("Driver Version: ").append(meta.getDriverVersion()).append("\n");
                info.append("User: ").append(meta.getUserName()).append("\n");
                if (isUserLoggedIn()) {
                    info.append("\nCurrent App User:\n");
                    info.append("  ID: ").append(getCurrentUserId()).append("\n");
                    info.append("  Username: ").append(getCurrentUsername()).append("\n");
                    info.append("  Email: ").append(getCurrentUserEmail()).append("\n");
                }
            }
        } catch (SQLException e) {
            info.append("Error getting database info: ").append(e.getMessage());
        }
        return info.toString();
    }

    public static void backupDatabase(String backupPath) {
        System.out.println("[DatabaseConnection] Backup functionality not implemented yet");
    }

    public static void restoreDatabase(String backupPath) {
        System.out.println("[DatabaseConnection] Restore functionality not implemented yet");
    }
}