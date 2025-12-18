package com.fashionstore.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {
    
    public static void checkAndCreatePasswordColumn() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if password column exists by querying INFORMATION_SCHEMA
                boolean columnExists = false;
                try {
                    Statement checkStmt = conn.createStatement();
                    String checkSql = "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.COLUMNS " +
                                     "WHERE TABLE_SCHEMA = 'fashionstore' " +
                                     "AND TABLE_NAME = 'users' " +
                                     "AND COLUMN_NAME = 'password'";
                    var rs = checkStmt.executeQuery(checkSql);
                    if (rs.next() && rs.getInt("count") > 0) {
                        columnExists = true;
                    }
                } catch (Exception e) {
                    // If INFORMATION_SCHEMA query fails, try direct SELECT
                    try {
                        Statement testStmt = conn.createStatement();
                        testStmt.executeQuery("SELECT password FROM users LIMIT 1");
                        columnExists = true;
                    } catch (Exception ex) {
                        columnExists = false;
                    }
                }
                
                // If column doesn't exist, create it
                if (!columnExists) {
                    try {
                        Statement alterStmt = conn.createStatement();
                        alterStmt.execute("ALTER TABLE users ADD COLUMN password VARCHAR(255)");
                        System.out.println("✓ Password column added successfully!");
                        
                        // Update existing users with default password
                        Statement updateStmt = conn.createStatement();
                        int updated = updateStmt.executeUpdate("UPDATE users SET password = '123456' WHERE password IS NULL OR password = ''");
                        if (updated > 0) {
                            System.out.println("✓ Updated " + updated + " existing user(s) with default password '123456'");
                        }
                    } catch (Exception alterEx) {
                        // Column might already exist or table doesn't exist
                        if (alterEx.getMessage().contains("Duplicate column")) {
                            System.out.println("✓ Password column already exists");
                        } else {
                            System.err.println("Error adding password column: " + alterEx.getMessage());
                        }
                    }
                } else {
                    System.out.println("✓ Password column already exists");
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking password column: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
    }
}

