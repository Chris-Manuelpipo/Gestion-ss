package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LogDAO {

    public static void insert(String utilisateur, String action, String details) {
        String sql = "INSERT INTO logs (utilisateur, action, details) VALUES (?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, utilisateur);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Logging failure must not break the main flow
            e.printStackTrace();
        }
    }
}
