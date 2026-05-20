package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.model.LogEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            e.printStackTrace();
        }
    }

    public List<LogEntry> findAll() {
        List<LogEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM logs ORDER BY created_at DESC";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM logs";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private LogEntry map(ResultSet rs) throws SQLException {
        LogEntry e = new LogEntry();
        e.setId(rs.getInt("id"));
        e.setUtilisateur(rs.getString("utilisateur"));
        e.setAction(rs.getString("action"));
        e.setDetails(rs.getString("details"));
        e.setIpAddress(rs.getString("ip_address"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());

        return e;
    }
}
