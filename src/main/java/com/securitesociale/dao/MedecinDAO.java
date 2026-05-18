package com.securitesociale.dao;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.model.Medecin;
import com.securitesociale.model.TypeMedecin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedecinDAO {

    // ── Authentification ──────────────────────────────────────────────────────

    /** Retourne le médecin (avec le hash) pour le login donné, ou null. */
    public Medecin findByLogin(String login) {
        String sql = "SELECT * FROM medecins WHERE login = ? AND actif = TRUE";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public List<Medecin> findAll() {
        List<Medecin> list = new ArrayList<>();
        String sql = "SELECT * FROM medecins WHERE actif = TRUE ORDER BY nom, prenom";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void save(Medecin m) {
        String sql = """
            INSERT INTO medecins
              (nom, prenom, email, date_naissance, sexe, login, mot_de_passe,
               type_medecin, type_formation, nom_specialite)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getPrenom());
            ps.setString(3, m.getEmail());
            ps.setObject(4, m.getDateNaissance());
            ps.setString(5, m.getSexe());
            ps.setString(6, m.getLogin());
            ps.setString(7, m.getMotDePasse());
            ps.setString(8, m.getTypeMedecin() != null ? m.getTypeMedecin().name() : null);
            ps.setString(9, m.getTypeFormation());
            ps.setString(10, m.getNomSpecialite());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) m.setNumMedecin(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Medecin m) {
        String sql = """
            UPDATE medecins SET nom=?, prenom=?, email=?, date_naissance=?, sexe=?,
              type_medecin=?, type_formation=?, nom_specialite=?
            WHERE num_medecin=?
            """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getPrenom());
            ps.setString(3, m.getEmail());
            ps.setObject(4, m.getDateNaissance());
            ps.setString(5, m.getSexe());
            ps.setString(6, m.getTypeMedecin() != null ? m.getTypeMedecin().name() : null);
            ps.setString(7, m.getTypeFormation());
            ps.setString(8, m.getNomSpecialite());
            ps.setInt(9, m.getNumMedecin());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Soft delete — désactive le médecin sans supprimer ses données. */
    public void delete(int numMedecin) {
        String sql = "UPDATE medecins SET actif = FALSE WHERE num_medecin = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, numMedecin);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM medecins WHERE actif = TRUE";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countGeneralistes() {
        String sql = "SELECT COUNT(*) FROM medecins WHERE actif = TRUE AND type_medecin = 'GENERALISTE'";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countSpecialistes() {
        String sql = "SELECT COUNT(*) FROM medecins WHERE actif = TRUE AND type_medecin = 'SPECIALISTE'";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Vérifie si un login est déjà utilisé (tous médecins, actifs ou archivés). */
    public boolean loginExists(String login) {
        String sql = "SELECT 1 FROM medecins WHERE login = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Medecin> findAllGeneralistes() {
        List<Medecin> list = new ArrayList<>();
        String sql = "SELECT * FROM medecins WHERE actif = TRUE AND type_medecin = 'GENERALISTE' ORDER BY nom, prenom";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Medecin> findAllSpecialistes() {
        List<Medecin> list = new ArrayList<>();
        String sql = "SELECT * FROM medecins WHERE actif = TRUE AND type_medecin = 'SPECIALISTE' ORDER BY nom, prenom";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void updatePassword(int numMedecin, String hashedPassword) {
        String sql = "UPDATE medecins SET mot_de_passe = ? WHERE num_medecin = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, numMedecin);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Medecin map(ResultSet rs) throws SQLException {
        Medecin m = new Medecin();
        m.setNumMedecin(rs.getInt("num_medecin"));
        m.setNom(rs.getString("nom"));
        m.setPrenom(rs.getString("prenom"));
        m.setEmail(rs.getString("email"));
        m.setSexe(rs.getString("sexe"));
        m.setLogin(rs.getString("login"));
        m.setMotDePasse(rs.getString("mot_de_passe"));
        m.setActif(rs.getBoolean("actif"));
        m.setTypeFormation(rs.getString("type_formation"));
        m.setNomSpecialite(rs.getString("nom_specialite"));

        String type = rs.getString("type_medecin");
        if (type != null) m.setTypeMedecin(TypeMedecin.valueOf(type));

        Date dn = rs.getDate("date_naissance");
        if (dn != null) m.setDateNaissance(dn.toLocalDate());

        return m;
    }
}
