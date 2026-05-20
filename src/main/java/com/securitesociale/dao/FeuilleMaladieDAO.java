package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.model.FeuilleMaladie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeuilleMaladieDAO {

    public List<FeuilleMaladie> findAll() {
        List<FeuilleMaladie> list = new ArrayList<>();
        String sql = "SELECT * FROM feuilles_maladie ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<FeuilleMaladie> findByMedecin(int numMedecin) {
        List<FeuilleMaladie> list = new ArrayList<>();
        String sql = """
            SELECT f.*, CONCAT(a.prenom,' ',a.nom) AS nom_assure,
                   c.motif, c.date_consult
            FROM feuilles_maladie f
            JOIN consultations c ON f.num_consultation = c.num_consultation
            JOIN assures a ON c.num_assure = a.num_assure
            WHERE f.num_medecin = ?
            ORDER BY f.date_emission DESC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numMedecin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapWithDetails(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<FeuilleMaladie> findByConsultation(int numConsultation) {
        List<FeuilleMaladie> list = new ArrayList<>();
        String sql = "SELECT * FROM feuilles_maladie WHERE num_consultation = ? ORDER BY version DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numConsultation);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public FeuilleMaladie findById(int numFeuille) {
        String sql = "SELECT * FROM feuilles_maladie WHERE num_feuille = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numFeuille);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public FeuilleMaladie save(FeuilleMaladie f) {
        String sql = """
            INSERT INTO feuilles_maladie
                (date_emission, statut, num_consultation, version,
                 temperature, tension_arterielle, poids, taille,
                 frequence_cardiaque, frequence_respiratoire, saturation_oxygene,
                 antecedents, symptomes, diagnostic, traitement_prescrit,
                 observations, num_medecin)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, f.getDateEmission());
            ps.setString(2, f.getStatut() != null ? f.getStatut() : "EN_ATTENTE");
            ps.setInt(3, f.getNumConsultation());
            ps.setInt(4, f.getVersion() > 0 ? f.getVersion() : 1);
            ps.setBigDecimal(5, f.getTemperature());
            ps.setString(6, f.getTensionArterielle());
            ps.setBigDecimal(7, f.getPoids());
            ps.setBigDecimal(8, f.getTaille());
            ps.setObject(9, f.getFrequenceCardiaque());
            ps.setObject(10, f.getFrequenceRespiratoire());
            ps.setBigDecimal(11, f.getSaturationOxygene());
            ps.setString(12, f.getAntecedents());
            ps.setString(13, f.getSymptomes());
            ps.setString(14, f.getDiagnostic());
            ps.setString(15, f.getTraitementPrescrit());
            ps.setString(16, f.getObservations());
            if (f.getNumMedecin() != null) ps.setInt(17, f.getNumMedecin());
            else ps.setNull(17, Types.INTEGER);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) f.setNumFeuille(keys.getInt(1));
            return f;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatut(int numFeuille, String statut) {
        String sql = "UPDATE feuilles_maladie SET statut = ? WHERE num_feuille = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, numFeuille);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countByMedecin(int numMedecin) {
        String sql = "SELECT COUNT(*) FROM feuilles_maladie WHERE num_medecin = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numMedecin);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countEnAttenteByMedecin(int numMedecin) {
        String sql = "SELECT COUNT(*) FROM feuilles_maladie WHERE num_medecin = ? AND statut = 'EN_ATTENTE'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numMedecin);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countEnAttente() {
        String sql = "SELECT COUNT(*) FROM feuilles_maladie WHERE statut = 'EN_ATTENTE'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private FeuilleMaladie map(ResultSet rs) throws SQLException {
        FeuilleMaladie f = new FeuilleMaladie();
        f.setNumFeuille(rs.getInt("num_feuille"));
        f.setStatut(rs.getString("statut"));
        f.setNumConsultation(rs.getInt("num_consultation"));
        f.setVersion(rs.getInt("version"));
        f.setTemperature(rs.getBigDecimal("temperature"));
        f.setTensionArterielle(rs.getString("tension_arterielle"));
        f.setPoids(rs.getBigDecimal("poids"));
        f.setTaille(rs.getBigDecimal("taille"));
        f.setFrequenceCardiaque((Integer) rs.getObject("frequence_cardiaque"));
        f.setFrequenceRespiratoire((Integer) rs.getObject("frequence_respiratoire"));
        f.setSaturationOxygene(rs.getBigDecimal("saturation_oxygene"));
        f.setAntecedents(rs.getString("antecedents"));
        f.setSymptomes(rs.getString("symptomes"));
        f.setDiagnostic(rs.getString("diagnostic"));
        f.setTraitementPrescrit(rs.getString("traitement_prescrit"));
        f.setObservations(rs.getString("observations"));
        f.setNumMedecin((Integer) rs.getObject("num_medecin"));

        Date d = rs.getDate("date_emission");
        if (d != null) f.setDateEmission(d.toLocalDate());

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) f.setCreatedAt(ts.toLocalDateTime());

        return f;
    }

    private FeuilleMaladie mapWithDetails(ResultSet rs) throws SQLException {
        FeuilleMaladie f = map(rs);
        f.setNomAssure(rs.getString("nom_assure"));
        f.setMotif(rs.getString("motif"));
        Date dc = rs.getDate("date_consult");
        if (dc != null) f.setDateConsult(dc.toLocalDate());
        return f;
    }
}
