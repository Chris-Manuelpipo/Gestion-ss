package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.dao.AssureDAO;
import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.dao.RemboursementDAO;
import com.securitesociale.dao.LogDAO;
import com.securitesociale.dao.FeuilleMaladieDAO;
import com.securitesociale.dao.ConsultationDAO;
import com.securitesociale.model.LogEntry;
import com.securitesociale.service.ConsultationService;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label dateLabel;
    @FXML private Label connectedAsLabel;

    // Agent SS KPIs
    @FXML private VBox agentSection;
    @FXML private Label totalAssures;
    @FXML private Label totalGeneralistes;
    @FXML private Label totalSpecialistes;
    @FXML private Label feuillesEnAttente;
    @FXML private Label rembEnAttente;
    @FXML private Label rembEffectues;
    @FXML private Label rembRejetes;
    @FXML private VBox recentLogsContainer;

    // Médecin KPIs
    @FXML private VBox medecinSection;
    @FXML private Label mesConsultations;
    @FXML private Label mesPatients;
    @FXML private Label feuillesEnAttenteMed;
    @FXML private Label totalFeuillesMed;

    private final AssureDAO assureDAO = new AssureDAO();
    private final MedecinDAO medecinDAO = new MedecinDAO();
    private final RemboursementDAO remboursementDAO = new RemboursementDAO();
    private final LogDAO logDAO = new LogDAO();
    private final FeuilleMaladieDAO feuilleMaladieDAO = new FeuilleMaladieDAO();
    private final ConsultationDAO consultationDAO = new ConsultationDAO();

    private static final DateTimeFormatter FR_DATE =
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter FR_DATETIME =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();

        welcomeLabel.setText("Bonjour, " + session.getNomAffichage());
        subtitleLabel.setText(session.getRoleAffichage() + "  ·  Gestion Sécurité Sociale");
        connectedAsLabel.setText(session.getLoginUtilisateur());
        dateLabel.setText(capitalize(LocalDate.now().format(FR_DATE)));

        if (session.isAgentSS()) {
            agentSection.setVisible(true);
            agentSection.setManaged(true);
            medecinSection.setVisible(false);
            medecinSection.setManaged(false);
            loadAgentStats();
        } else {
            agentSection.setVisible(false);
            agentSection.setManaged(false);
            medecinSection.setVisible(true);
            medecinSection.setManaged(true);
            loadMedecinStats();
        }
    }

    private void loadAgentStats() {
        new Thread(() -> {
            try {
                int assures      = assureDAO.count();
                int generalistes = medecinDAO.countGeneralistes();
                int specialistes = medecinDAO.countSpecialistes();
                int feuilles     = countFeuillesEnAttente();
                int enAttente    = remboursementDAO.countByStatut("EN_ATTENTE");
                int effectues    = remboursementDAO.countByStatut("EFFECTUE");
                int rejetes      = remboursementDAO.countByStatut("REJETEE");
                List<LogEntry> allLogs = logDAO.findAll();
                List<LogEntry> recentLogs = allLogs.size() > 8 ? allLogs.subList(0, 8) : allLogs;

                Platform.runLater(() -> {
                    totalAssures.setText(String.valueOf(assures));
                    totalGeneralistes.setText(String.valueOf(generalistes));
                    totalSpecialistes.setText(String.valueOf(specialistes));
                    feuillesEnAttente.setText(String.valueOf(feuilles));
                    rembEnAttente.setText(String.valueOf(enAttente));
                    rembEffectues.setText(String.valueOf(effectues));
                    rembRejetes.setText(String.valueOf(rejetes));
                    populateRecentLogs(recentLogs);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadMedecinStats() {
        new Thread(() -> {
            try {
                SessionManager session = SessionManager.getInstance();
                int numMedecin = session.getMedecinConnecte().getNumMedecin();

                int consultations = consultationDAO.countByMedecin(numMedecin);
                int patients      = consultationDAO.countPatientsByMedecin(numMedecin);
                int feuillesEnAtt = feuilleMaladieDAO.countEnAttenteByMedecin(numMedecin);
                int totalFeuilles = feuilleMaladieDAO.countByMedecin(numMedecin);

                Platform.runLater(() -> {
                    mesConsultations.setText(String.valueOf(consultations));
                    mesPatients.setText(String.valueOf(patients));
                    feuillesEnAttenteMed.setText(String.valueOf(feuillesEnAtt));
                    totalFeuillesMed.setText(String.valueOf(totalFeuilles));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateRecentLogs(List<LogEntry> logs) {
        recentLogsContainer.getChildren().clear();
        if (logs.isEmpty()) {
            Label empty = new Label("Aucune activité récente");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-padding: 16 0;");
            recentLogsContainer.getChildren().add(empty);
            return;
        }
        for (int i = 0; i < logs.size(); i++) {
            LogEntry entry = logs.get(i);
            VBox row = new VBox(4);
            row.setStyle("-fx-padding: 10 0;");
            if (i > 0) row.setStyle("-fx-padding: 10 0; -fx-border-color: #F1F5F9; -fx-border-width: 1 0 0 0;");

            Label actionLabel = new Label(formatAction(entry.getAction()));
            actionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E293B;");

            Label detailLabel = new Label(entry.getDetails() + "  ·  " + entry.getUtilisateur());
            detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

            Label timeLabel = new Label(formatDateTime(entry.getCreatedAt()));
            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

            row.getChildren().addAll(actionLabel, detailLabel, timeLabel);
            recentLogsContainer.getChildren().add(row);
        }
    }

    private String formatAction(String action) {
        if (action == null) return "";
        return switch (action) {
            case "LOGIN_SUCCESS" -> "Connexion";
            case "CREATE_ASSURE" -> "Création d'assuré";
            case "UPDATE_ASSURE" -> "Modification d'assuré";
            case "DELETE_ASSURE" -> "Suppression d'assuré";
            case "CREATE_MEDECIN" -> "Inscription médecin";
            case "CREATE_CONSULTATION" -> "Nouvelle consultation";
            case "UPDATE_CONSULTATION" -> "Modification consultation";
            case "DELETE_CONSULTATION" -> "Suppression consultation";
            case "CREATE_PRESCRIPTION" -> "Nouvelle prescription";
            case "CREATE_FEUILLE_MALADIE" -> "Nouvelle feuille de maladie";
            case "CREATE_REMBOURSEMENT" -> "Nouveau remboursement";
            case "VALIDER_REMBOURSEMENT" -> "Remboursement validé";
            case "REJETER_REMBOURSEMENT" -> "Remboursement rejeté";
            case "DELETE_REMBOURSEMENT" -> "Suppression remboursement";
            default -> action.replace("_", " ");
        };
    }

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(FR_DATETIME);
    }

    private int countFeuillesEnAttente() {
        String sql = "SELECT COUNT(*) FROM feuilles_maladie WHERE statut = 'EN_ATTENTE'";
        try (Connection c = com.securitesociale.config.DatabaseManager.getConnection();
             Statement  st = c.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    @FXML private void handleNavAssures() { navigate("/fxml/assures.fxml", "Assurés"); }
    @FXML private void handleNavMedecins() { navigate("/fxml/medecins.fxml", "Médecins"); }
    @FXML private void handleNavRemboursements() { navigate("/fxml/remboursements.fxml", "Remboursements"); }
    @FXML private void handleNavLogs() { navigate("/fxml/logs.fxml", "Journal d'audit"); }

    @FXML private void handleNewConsultation() { openDialog("/fxml/consultation_form.fxml", "Nouvelle Consultation"); }
    @FXML private void handleNewFeuille() { openDialog("/fxml/feuilles_maladie.fxml", "Feuilles de maladie"); }
    @FXML private void handleNewPrescription() { openDialog("/fxml/prescription_form.fxml", "Nouvelle Prescription"); }

    private void navigate(String fxmlPath, String title) {
        if (MainController.instance != null) {
            MainController.instance.loadView(fxmlPath, title);
        }
    }

    private void openDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("/css/style.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
