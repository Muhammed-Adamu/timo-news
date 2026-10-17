package com.news.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Utility class for managing MySQL connections.
 *
 * Connection settings are resolved per property in this order (first match wins):
 *   1. Environment variables — DB_URL, DB_USER, DB_PASSWORD
 *   2. db.properties on the classpath (src/main/resources/db.properties)
 *
 * The URL and username fall back to local defaults, but the password has no
 * default: if neither source supplies one, getConnection() fails with a clear
 * message rather than silently trying an empty password.
 *
 * No credentials are stored in this file, so it is safe to commit. Copy
 * src/main/resources/db.properties.example to db.properties to set them locally.
 */
public class DBUtil {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/timo_news"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            + "&useUnicode=true&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";

    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        Properties fileProps = loadDbProperties();

        DB_URL = resolve("DB_URL", fileProps, "db.url", DEFAULT_URL);
        DB_USER = resolve("DB_USER", fileProps, "db.user", DEFAULT_USER);
        DB_PASSWORD = resolve("DB_PASSWORD", fileProps, "db.password", null);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. "
                    + "Add mysql-connector-j to classpath.", e);
        }
    }

    /**
     * Reads db.properties from the classpath. Missing file is not an error —
     * environment variables may supply everything.
     */
    private static Properties loadDbProperties() {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("[DBUtil] Could not read db.properties: " + e.getMessage());
        }
        return props;
    }

    /**
     * Returns the first non-blank value: environment variable, then the
     * properties file, then the supplied fallback.
     */
    private static String resolve(String envKey, Properties props, String propKey, String fallback) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        String fromFile = props.getProperty(propKey);
        if (fromFile != null && !fromFile.trim().isEmpty()) {
            return fromFile.trim();
        }
        return fallback;
    }

    /**
     * Gets a new database connection.
     * Caller is responsible for closing it.
     */
    public static Connection getConnection() throws SQLException {
        if (DB_PASSWORD == null) {
            throw new SQLException("No database password configured. Set the DB_PASSWORD "
                    + "environment variable, or copy src/main/resources/db.properties.example "
                    + "to db.properties and fill in db.password.");
        }
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("[DBUtil] DATABASE CONNECTION FAILED!");
            System.err.println("[DBUtil] URL: " + DB_URL);
            System.err.println("[DBUtil] User: " + DB_USER);
            System.err.println("[DBUtil] Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Closes an AutoCloseable resource quietly (Connection, Statement, ResultSet).
     */
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Suppress — nothing we can do at this level
                }
            }
        }
    }
}
