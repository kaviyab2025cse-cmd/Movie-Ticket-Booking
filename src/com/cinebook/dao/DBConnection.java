package com.cinebook.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager for MySQL 8.0+ using JDBC.
 * Reads configuration from db.properties if available, otherwise uses sensible defaults.
 */
public class DBConnection {

    private static String url = "jdbc:mysql://localhost:3306/cinebook_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = ""; // Default empty password for local MySQL / XAMPP / Workbench

    static {
        // Attempt to load custom properties if db.properties exists
        try {
            File propFile = new File("db.properties");
            if (propFile.exists()) {
                Properties props = new Properties();
                try (InputStream in = new FileInputStream(propFile)) {
                    props.load(in);
                    if (props.containsKey("db.url")) url = props.getProperty("db.url");
                    if (props.containsKey("db.user")) user = props.getProperty("db.user");
                    if (props.containsKey("db.password")) password = props.getProperty("db.password");
                }
            }
        } catch (Exception ignored) {
        }

        // Register MySQL JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver will be loaded via service provider if modern JDBC
        }
    }

    /**
     * Obtains a new connection to the MySQL database.
     *
     * @return a valid java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks if MySQL database is currently reachable.
     */
    public static boolean isAvailable() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static void setCredentials(String dbUrl, String dbUser, String dbPass) {
        url = dbUrl;
        user = dbUser;
        password = dbPass;
    }
}
