package connectDB;

import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton quan ly ket noi database
 * Su dung: DatabaseConnection.getInstance().getConnection()
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.connection = DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USERNAME,
                DatabaseConfig.PASSWORD
            );
            System.out.println("Ket noi database thanh cong!");
            runMigrations();
        } catch (ClassNotFoundException e) {
            System.err.println("Khong tim thay JDBC Driver: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Loi ket noi database: " + e.getMessage());
        }
    }

    /**
     * Tu dong them cot moi, cap nhat du lieu.
     * An toan khi chay lai nhieu lan (idempotent).
     */
    private void runMigrations() {
        try (java.sql.Statement stmt = connection.createStatement()) {
            // 1. Them cot khoiLuongDongGoi vao NguyenLieu neu chua co
            stmt.execute(
                "IF COL_LENGTH('NguyenLieu', 'khoiLuongDongGoi') IS NULL " +
                "ALTER TABLE NguyenLieu ADD khoiLuongDongGoi FLOAT DEFAULT 0"
            );

            // 2. Set khoiLuongDongGoi = 1 cho NL chua co gia tri (de phep chia khong bi loi)
            stmt.execute(
                "UPDATE NguyenLieu SET khoiLuongDongGoi = 1 WHERE khoiLuongDongGoi IS NULL OR khoiLuongDongGoi = 0"
            );

            System.out.println("Database migration: OK");
        } catch (SQLException e) {
            System.err.println("Loi migration: " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Re-establish connection for the current instance
                this.connection = DriverManager.getConnection(
                    DatabaseConfig.URL,
                    DatabaseConfig.USERNAME,
                    DatabaseConfig.PASSWORD
                );
            }
        } catch (SQLException e) {
            System.err.println("Loi khi lay ket noi database: " + e.getMessage());
        }
        return connection;
    }

    private static boolean isConnectionClosed() {
        try {
            return instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Loi khi dong ket noi: " + e.getMessage());
        }
    }
}
