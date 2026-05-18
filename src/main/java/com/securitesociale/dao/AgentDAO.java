package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;

import java.sql.*;

/**
 * DAO pour l'agent de sécurité sociale (table agents_ss).
 */
public class AgentDAO {

    public boolean exists(String login) {
        String sql = "SELECT COUNT(*) FROM agents_ss WHERE login = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void create(String login, String hashedPassword) {
        String sql = "INSERT INTO agents_ss (login, mot_de_passe) VALUES (?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Retourne le hash BCrypt du mot de passe pour le login donné, ou null. */
    public String getHashedPassword(String login) {
        String sql = "SELECT mot_de_passe FROM agents_ss WHERE login = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("mot_de_passe") : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
