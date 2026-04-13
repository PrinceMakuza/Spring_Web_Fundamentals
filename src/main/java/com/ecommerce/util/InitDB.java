package com.ecommerce.util;

import com.ecommerce.dao.DatabaseConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility to initialize the database schema and seed data.
 */
public class InitDB {
    public static void main(String[] args) {
        System.out.println("Initializing Database...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Execute schema.sql
            System.out.println("Executing schema.sql...");
            String schemaSql = new String(Files.readAllBytes(Paths.get("sql/schema.sql")));
            stmt.execute(schemaSql);
            
            // Execute indexes.sql
            System.out.println("Executing indexes.sql...");
            String indexesSql = new String(Files.readAllBytes(Paths.get("sql/indexes.sql")));
            stmt.execute(indexesSql);
            
            // Execute seed.sql
            System.out.println("Executing seed.sql...");
            String seedSql = new String(Files.readAllBytes(Paths.get("sql/seed.sql")));
            stmt.execute(seedSql);
            
            System.out.println("Database Initialized Successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
