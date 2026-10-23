package com.recians.voiceout.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * DBConnection - Centralized JDBC Connection Manager for RECian's VoiceOut.
 * 
 * Provides database connections to the MySQL 'recians_voiceout' database.
 * Credentials are read dynamically from 'db.properties' or environment variables
 * to avoid hardcoding passwords in source code.
 */
public class DBConnection {

    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;

    static {
        loadConfiguration();
    }

    /**
     * Loads database configuration safely from properties file or environment variables.
     * Passwords are never logged or printed to the console.
     */
    private static void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Try loading from ClassLoader (e.g. WEB-INF/classes/db.properties)
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                loaded = true;
            }
        } catch (Exception ignored) {
        }

        // 2. Fallback: try loading from project root or src directory if running standalone
        if (!loaded) {
            String[] fallbackPaths = {"db.properties", "src/db.properties", "WebContent/WEB-INF/classes/db.properties"};
            for (String path : fallbackPaths) {
                File file = new File(path);
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        props.load(fis);
                        loaded = true;
                        break;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Check environment variables first, then properties file, then safe defaults
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        jdbcUrl = (envUrl != null && !envUrl.trim().isEmpty()) 
                    ? envUrl 
                    : props.getProperty("db.url", "jdbc:mysql://localhost:3306/recians_voiceout?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");

        dbUser = (envUser != null && !envUser.trim().isEmpty()) 
                    ? envUser 
                    : props.getProperty("db.user", "root");

        dbPassword = (envPass != null) 
                    ? envPass 
                    : props.getProperty("db.password", "root");

        // Register the MySQL Connector/J driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] ERROR: MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in classpath.");
        }
    }

    /**
     * Establishes and returns a new JDBC Connection to MySQL.
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (jdbcUrl == null || dbUser == null) {
            loadConfiguration();
        }
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    /**
     * Closes JDBC resources safely without throwing exceptions to caller.
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Overload to close connection and statement when no result set is used.
     */
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    /**
     * Diagnostic test method to verify connectivity and run sample queries.
     * 
     * @return true if connection and verification queries succeed
     */
    public static boolean testConnection() {
        System.out.println("=================================================");
        System.out.println("  RECian's VoiceOut - JDBC Connection Diagnostic");
        System.out.println("=================================================");
        System.out.println("Testing connection to: " + jdbcUrl);
        System.out.println("Connecting as DB User: " + dbUser);
        System.out.println("(Password is masked for security)");
        System.out.println("-------------------------------------------------");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            long startTime = System.currentTimeMillis();
            conn = getConnection();
            long elapsed = System.currentTimeMillis() - startTime;

            System.out.println("[SUCCESS] Connected to MySQL in " + elapsed + " ms.");

            stmt = conn.createStatement();

            // Test 1: Simple query (SELECT 1)
            rs = stmt.executeQuery("SELECT 1 AS ping_check");
            if (rs.next() && rs.getInt("ping_check") == 1) {
                System.out.println("[SUCCESS] Query 'SELECT 1' passed successfully.");
            }
            rs.close();

            // Test 2: Verify recians_voiceout database categories table
            rs = stmt.executeQuery("SELECT COUNT(*) AS total_cats FROM categories");
            if (rs.next()) {
                int count = rs.getInt("total_cats");
                System.out.println("[SUCCESS] Table 'categories' verified: " + count + " categories found.");
            }
            rs.close();

            // Test 3: Verify MySQL Server Version
            rs = stmt.executeQuery("SELECT VERSION() AS db_version");
            if (rs.next()) {
                System.out.println("[SUCCESS] MySQL Server Version: " + rs.getString("db_version"));
            }

            System.out.println("-------------------------------------------------");
            System.out.println("[VERDICT] JDBC Connection to 'recians_voiceout' is 100% OPERATIONAL!");
            System.out.println("=================================================");
            return true;

        } catch (SQLException e) {
            System.err.println("[FAILURE] Database Connection Error!");
            System.err.println("SQL State : " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message   : " + e.getMessage());
            return false;
        } finally {
            close(conn, stmt, rs);
        }
    }

    /**
     * Standalone main method allowing terminal execution:
     * java com.recians.voiceout.dao.DBConnection
     */
    public static void main(String[] args) {
        boolean ok = testConnection();
        System.exit(ok ? 0 : 1);
    }
}
