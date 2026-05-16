package com.example.library.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static final String PROPERTIES_PATH = "/config.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream in = Database.class.getResourceAsStream(PROPERTIES_PATH)) {
            if (in == null) {
                throw new IllegalStateException("Missing config.properties on classpath");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    private Database() { }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String pass = properties.getProperty("db.password");
        if (url == null || user == null) {
            throw new IllegalStateException("db.url and db.user must be set in config.properties");
        }
        return DriverManager.getConnection(url, user, pass);
    }
}


