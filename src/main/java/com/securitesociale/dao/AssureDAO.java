package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.model.Assure;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssureDAO {

    public List<Assure> findAll() {
        List<Assure> list = new ArrayList<>();
        String sql = """
            SELECT a.*, CONCAT(m.prenom,' ',m.nom) AS nom_medecin_traitant
            FROM assures a
            LEFT JOIN medecins m ON a.num_medecin_traitant = m.num_medecin
            ORDER BY a.nom, a.prenom
            """;
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void save(Assure a) {
        String sql = """
            INSERT INTO assures
              (nom, prenom, email, date_naissance, sexe, num_compte_bancaire, num_medecin_traitant)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setObject(4, a.getDateNaissance());
            ps.setString(5, a.getSexe());
            ps.setString(6, a.getNumCompteBancaire());
            ps.setObject(7, a.getNumMedecinTraitant() > 0 ? a.getNumMedecinTraitant() : null);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) a.setNumAssure(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Assure a) {
        String sql = """
            UPDATE assures SET nom=?, prenom=?, email=?, date_naissance=?, sexe=?,
              num_compte_bancaire=?, num_medecin_traitant=?
            WHERE num_assure=?
            """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setObject(4, a.getDateNaissance());
            ps.setString(5, a.getSexe());
            ps.setString(6, a.getNumCompteBancaire());
            ps.setObject(7, a.getNumMedecinTraitant() > 0 ? a.getNumMedecinTraitant() : null);
            ps.setInt(8, a.getNumAssure());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int numAssure) {
        String sql = "DELETE FROM assures WHERE num_assure = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, numAssure);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM assures";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Assure map(ResultSet rs) throws SQLException {
        Assure a = new Assure();
        a.setNumAssure(rs.getInt("num_assure"));
        a.setNom(rs.getString("nom"));
        a.setPrenom(rs.getString("prenom"));
        a.setEmail(rs.getString("email"));
        a.setSexe(rs.getString("sexe"));
        a.setNumCompteBancaire(rs.getString("num_compte_bancaire"));

        int numMed = rs.getInt("num_medecin_traitant");
        if (!rs.wasNull()) a.setNumMedecinTraitant(numMed);

        String nomMed = rs.getString("nom_medecin_traitant");
        if (nomMed != null) a.setNomMedecinTraitant("Dr " + nomMed);

        Date dn = rs.getDate("date_naissance");
        if (dn != null) a.setDateNaissance(dn.toLocalDate());

        return a;
    }
}
