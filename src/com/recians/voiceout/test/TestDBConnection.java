package com.recians.voiceout.test;

import com.recians.voiceout.dao.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * TestDBConnection - Test runner for Stage 3 JDBC connectivity.
 * 
 * Verifies that the Java application can establish a live JDBC connection 
 * with the MySQL database 'recians_voiceout' and run test queries.
 */
public class TestDBConnection {

    public static void main(String[] args) {
        System.out.println("Running Stage 3 JDBC Connection Test...");

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Run simple ping query
            try (ResultSet rs = stmt.executeQuery("SELECT 1 AS check_val")) {
                if (rs.next()) {
                    int val = rs.getInt("check_val");
                    if (val == 1) {
                        System.out.println("[TEST PASSED] SELECT 1 returned expected value: " + val);
                    }
                }
            }

            // Run schema verification query on recians_voiceout
            try (ResultSet rs = stmt.executeQuery("SELECT category_id, category_name FROM categories ORDER BY category_id LIMIT 3")) {
                System.out.println("[TEST PASSED] Successfully read categories from recians_voiceout:");
                while (rs.next()) {
                    System.out.println("   - ID " + rs.getInt("category_id") + ": " + rs.getString("category_name"));
                }
            }

            System.out.println("[ALL TESTS PASSED] Stage 3 JDBC configuration is working perfectly!");

        } catch (Exception e) {
            System.err.println("[TEST FAILED] Could not connect to MySQL database via JDBC!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
