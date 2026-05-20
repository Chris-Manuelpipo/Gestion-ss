package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.model.Consultation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultationDAO {

    public List<Consultation> findAll() {
        List<Consultation> list = new ArrayList<>();
        String sql = """
            SELECT c.*, CONCAT(m.prenom,' ',m.nom) AS nom_med,
                   CONCAT(a.prenom,' ',a.nom) AS nom_ass
            FROM consultations c
            JOIN medecins m ON c.num_medecin = m.num_medecin
            JOIN assures a  ON c.num_assure  = a.num_assure
            ORDER BY c.date_consult DESC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Consultation> findByMedecin(int numMedecin) {
        List<Consultation> list = new ArrayList<>();
        String sql = """
            SELECT c.*, CONCAT(m.prenom,' ',m.nom) AS nom_med,
                   CONCAT(a.prenom,' ',a.nom) AS nom_ass
            FROM consultations c
            JOIN medecins m ON c.num_medecin = m.num_medecin
            JOIN assures a  ON c.num_assure  = a.num_assure
            WHERE c.num_medecin = ?
            ORDER BY c.date_consult DESC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numMedecin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Consultation> findByAssure(int numAssure) {
        List<Consultation> list = new ArrayList<>();
        String sql = """
            SELECT c.*, CONCAT(m.prenom,' ',m.nom) AS nom_med,
                   CONCAT(a.prenom,' ',a.nom) AS nom_ass
            FROM consultations c
            JOIN medecins m ON c.num_medecin = m.num_medecin
            JOIN assures a  ON c.num_assure  = a.num_assure
            WHERE c.num_assure = ?
            ORDER BY c.date_consult DESC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numAssure);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Consultation findById(int numConsultation) {
        String sql = """
            SELECT c.*, CONCAT(m.prenom,' ',m.nom) AS nom_med,
                   CONCAT(a.prenom,' ',a.nom) AS nom_ass
            FROM consultations c
            JOIN medecins m ON c.num_medecin = m.num_medecin
            JOIN assures a  ON c.num_assure  = a.num_assure
            WHERE c.num_consultation = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numConsultation);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Consultation save(Consultation c) {
        String sql = """
            INSERT INTO consultations (date_consult, motif, diagnostic, num_medecin, num_assure)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, c.getDateConsult());
            ps.setString(2, c.getMotif());
            ps.setString(3, c.getDiagnostic());
            ps.setInt(4, c.getNumMedecin());
            ps.setInt(5, c.getNumAssure());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setNumConsultation(keys.getInt(1));
            return c;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Consultation c) {
        String sql = """
            UPDATE consultations SET date_consult=?, motif=?, diagnostic=?
            WHERE num_consultation=?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, c.getDateConsult());
            ps.setString(2, c.getMotif());
            ps.setString(3, c.getDiagnostic());
            ps.setInt(4, c.getNumConsultation());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int numConsultation) {
        String sql = "DELETE FROM consultations WHERE num_consultation = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numConsultation);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM consultations";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countByMedecin(int numMedecin) {
        String sql = "SELECT COUNT(*) FROM consultations WHERE num_medecin = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numMedecin);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countPatientsByMedecin(int numMedecin) {
        String sql = "SELECT COUNT(DISTINCT num_assure) FROM consultations WHERE num_medecin = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numMedecin);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Consultation map(ResultSet rs) throws SQLException {
        Consultation c = new Consultation();
        c.setNumConsultation(rs.getInt("num_consultation"));
        c.setMotif(rs.getString("motif"));
        c.setDiagnostic(rs.getString("diagnostic"));
        c.setNumMedecin(rs.getInt("num_medecin"));
        c.setNumAssure(rs.getInt("num_assure"));
        c.setNomMedecin(rs.getString("nom_med"));
        c.setNomAssure(rs.getString("nom_ass"));

        Date d = rs.getDate("date_consult");
        if (d != null) c.setDateConsult(d.toLocalDate());

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());

        return c;
    }
}
