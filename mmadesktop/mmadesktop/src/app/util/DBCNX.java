package app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBCNX
{
    // Database credentials – change if needed
    private static final String URL = "jdbc:mysql://localhost:3306/mma_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // ← your MySQL password (empty for XAMPP default)

    static {
        try {
            // Load MySQL JDBC driver (required for older Java versions, safe to keep)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found!", e);
        }
    }

    /**
     * Returns a new database connection.
     * Caller must close it (preferably with try‑with‑resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}