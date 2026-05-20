package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.model.Remboursement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RemboursementDAO {

    public List<Remboursement> findAll() {
        List<Remboursement> list = new ArrayList<>();
        String sql = "SELECT * FROM remboursements ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Remboursement> findByStatut(String statut) {
        List<Remboursement> list = new ArrayList<>();
        String sql = "SELECT * FROM remboursements WHERE statut = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Remboursement findById(int numRemboursement) {
        String sql = "SELECT * FROM remboursements WHERE num_remboursement = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numRemboursement);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Remboursement save(Remboursement r) {
        String sql = """
            INSERT INTO remboursements (nature, taux, montant, mode_reglement, statut, num_feuille)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNature());
            ps.setBigDecimal(2, r.getTaux());
            ps.setBigDecimal(3, r.getMontant());
            ps.setString(4, r.getModeReglement());
            ps.setString(5, r.getStatut() != null ? r.getStatut() : "EN_ATTENTE");
            ps.setInt(6, r.getNumFeuille());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) r.setNumRemboursement(keys.getInt(1));
            return r;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatut(int numRemboursement, String statut, String agentLogin) {
        String sql = "UPDATE remboursements SET statut=?, date_remboursement=NOW(), agent_login=? WHERE num_remboursement=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setString(2, agentLogin);
            ps.setInt(3, numRemboursement);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int numRemboursement) {
        String sql = "DELETE FROM remboursements WHERE num_remboursement = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numRemboursement);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM remboursements";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countByStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM remboursements WHERE statut = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countEnAttente() {
        String sql = "SELECT COUNT(*) FROM remboursements WHERE statut = 'EN_ATTENTE'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Remboursement map(ResultSet rs) throws SQLException {
        Remboursement r = new Remboursement();
        r.setNumRemboursement(rs.getInt("num_remboursement"));
        r.setNature(rs.getString("nature"));
        r.setTaux(rs.getBigDecimal("taux"));
        r.setMontant(rs.getBigDecimal("montant"));
        r.setModeReglement(rs.getString("mode_reglement"));
        r.setStatut(rs.getString("statut"));
        r.setNumFeuille(rs.getInt("num_feuille"));
        r.setAgentLogin(rs.getString("agent_login"));

        Date dr = rs.getDate("date_remboursement");
        if (dr != null) r.setDateRemboursement(dr.toLocalDate());

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());

        return r;
    }
}
