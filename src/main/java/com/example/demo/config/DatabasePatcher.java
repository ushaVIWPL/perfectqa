package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabasePatcher implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== RUNNING DATABASE PATCHES ===");
        try {
            // Alter screenshot_paths to TEXT to allow longer data (MySQL)
            jdbcTemplate.execute("ALTER TABLE testcase_transactions MODIFY screenshot_paths TEXT");
            System.out.println("Successfully patched testcase_transactions.screenshot_paths to TEXT");
        } catch (Exception e) {
            System.out.println("Database patch failed (might already be patched): " + e.getMessage());
        }
        
        try {
            // Also ensure testcase_header screenshot_paths is TEXT just in case
            jdbcTemplate.execute("ALTER TABLE testcase_header MODIFY screenshot_paths TEXT");
            System.out.println("Successfully patched testcase_header.screenshot_paths to TEXT");
        } catch (Exception e) {
            System.out.println("Database patch failed for testcase_header: " + e.getMessage());
        }

        try {
            // Fix project_description truncation error
            jdbcTemplate.execute("ALTER TABLE project_ref MODIFY project_description TEXT");
            System.out.println("Successfully patched project_ref.project_description to TEXT");
        } catch (Exception e) {
            System.out.println("Database patch failed for project_ref.project_description: " + e.getMessage());
        }

        try {
            // Fix application_ref description truncation error
            jdbcTemplate.execute("ALTER TABLE application_ref MODIFY description TEXT");
            System.out.println("Successfully patched application_ref.description to TEXT");
        } catch (Exception e) {
            System.out.println("Database patch failed for application_ref.description: " + e.getMessage());
        }
    }
}
