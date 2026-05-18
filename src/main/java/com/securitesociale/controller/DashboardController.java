package com.securitesociale.controller;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.dao.AssureDAO;
import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label dateLabel;
    @FXML private Label connectedAsLabel;

    @FXML private Label totalAssures;
    @FXML private Label totalGeneralistes;
    @FXML private Label totalSpecialistes;
    @FXML private Label feuillesEnAttente;

    private final AssureDAO  assureDAO  = new AssureDAO();
    private final MedecinDAO medecinDAO = new MedecinDAO();

    private static final DateTimeFormatter FR_DATE =
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

    // ── Initialisation ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();

        welcomeLabel.setText("Bonjour, " + session.getNomAffichage());
        subtitleLabel.setText(session.getRoleAffichage() + "  ·  Gestion Sécurité Sociale");
        connectedAsLabel.setText(session.getLoginUtilisateur());
        dateLabel.setText(capitalize(LocalDate.now().format(FR_DATE)));

        loadStats();
    }

    // ── Chargement des statistiques ───────────────────────────────────────────

    private void loadStats() {
        new Thread(() -> {
            try {
                int assures      = assureDAO.count();
                int generalistes = medecinDAO.countGeneralistes();
                int specialistes = medecinDAO.countSpecialistes();
                int enAttente    = countFeuillesEnAttente();

                Platform.runLater(() -> {
                    totalAssures.setText(String.valueOf(assures));
                    totalGeneralistes.setText(String.valueOf(generalistes));
                    totalSpecialistes.setText(String.valueOf(specialistes));
                    feuillesEnAttente.setText(String.valueOf(enAttente));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int countFeuillesEnAttente() {
        String sql = "SELECT COUNT(*) FROM feuilles_maladie WHERE statut = 'EN_ATTENTE'";
        try (Connection c = DatabaseManager.getConnection();
             Statement  st = c.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Navigation rapide ─────────────────────────────────────────────────────

    @FXML private void handleNavAssures() {
        navigate("/fxml/assures.fxml", "Assurés");
    }

    @FXML private void handleNavMedecins() {
        navigate("/fxml/medecins.fxml", "Médecins");
    }

    @FXML private void handleNavRemboursements() {
        navigate("/fxml/remboursements.fxml", "Remboursements");
    }

    @FXML private void handleNavLogs() {
        navigate("/fxml/logs.fxml", "Journal d'audit");
    }

    private void navigate(String fxmlPath, String title) {
        if (MainController.instance != null) {
            MainController.instance.loadView(fxmlPath, title);
        }
    }

    // ── Utilitaire ────────────────────────────────────────────────────────────

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
