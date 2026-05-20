package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.model.Prescription;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public List<Prescription> findByConsultation(int numConsultation) {
        List<Prescription> list = new ArrayList<>();
        String sql = """
            SELECT p.*, CONCAT(m.prenom,' ',m.nom) AS nom_spec
            FROM prescriptions p
            LEFT JOIN medecins m ON p.num_specialiste = m.num_medecin
            WHERE p.num_consultation = ?
            ORDER BY p.date_prescription DESC
            """;
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

    public List<Prescription> findAll() {
        List<Prescription> list = new ArrayList<>();
        String sql = """
            SELECT p.*, CONCAT(m.prenom,' ',m.nom) AS nom_spec
            FROM prescriptions p
            LEFT JOIN medecins m ON p.num_specialiste = m.num_medecin
            ORDER BY p.date_prescription DESC
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

    public Prescription save(Prescription p) {
        String sql = """
            INSERT INTO prescriptions
                (date_prescription, type, contenu, num_consultation,
                 code_medicament, nom_medicament, posologie, dosage,
                 type_examen, motif_medical, num_specialiste)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, p.getDatePrescription());
            ps.setString(2, p.getType());
            ps.setString(3, p.getContenu());
            ps.setInt(4, p.getNumConsultation());
            ps.setString(5, p.getCodeMedicament());
            ps.setString(6, p.getNomMedicament());
            ps.setString(7, p.getPosologie());
            ps.setString(8, p.getDosage());
            ps.setString(9, p.getTypeExamen());
            ps.setString(10, p.getMotifMedical());
            if (p.getNumSpecialiste() != null) ps.setInt(11, p.getNumSpecialiste());
            else ps.setNull(11, Types.INTEGER);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setNumPrescription(keys.getInt(1));
            return p;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int numPrescription) {
        String sql = "DELETE FROM prescriptions WHERE num_prescription = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numPrescription);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Prescription map(ResultSet rs) throws SQLException {
        Prescription p = new Prescription();
        p.setNumPrescription(rs.getInt("num_prescription"));
        p.setType(rs.getString("type"));
        p.setContenu(rs.getString("contenu"));
        p.setNumConsultation(rs.getInt("num_consultation"));
        p.setCodeMedicament(rs.getString("code_medicament"));
        p.setNomMedicament(rs.getString("nom_medicament"));
        p.setPosologie(rs.getString("posologie"));
        p.setDosage(rs.getString("dosage"));
        p.setTypeExamen(rs.getString("type_examen"));
        p.setMotifMedical(rs.getString("motif_medical"));
        p.setNomSpecialiste(rs.getString("nom_spec"));

        int ns = rs.getInt("num_specialiste");
        if (!rs.wasNull()) p.setNumSpecialiste(ns);

        Date d = rs.getDate("date_prescription");
        if (d != null) p.setDatePrescription(d.toLocalDate());

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());

        return p;
    }
}
