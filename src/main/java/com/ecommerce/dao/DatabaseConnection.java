package com.ecommerce.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static HikariDataSource dataSource;

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("com/ecommerce/config/db.properties")) {
            Properties props = new Properties();
            if (input == null) {
                // Fallback for some environments where resource path might differ
                try (InputStream input2 = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                   if (input2 != null) props.load(input2);
                   else throw new RuntimeException("Unable to find db.properties");
                }
            } else {
                props.load(input);
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.poolSize", "10")));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading database properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
