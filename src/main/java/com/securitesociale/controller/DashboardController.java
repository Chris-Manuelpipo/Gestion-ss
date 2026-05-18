package com.securitesociale.controller;

import com.securitesociale.dao.AssureDAO;
import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalAssures;
    @FXML private Label totalMedecins;
    @FXML private Label consultationsAujourdhui;
    @FXML private Label remboursementsAttente;

    private final AssureDAO  assureDAO  = new AssureDAO();
    private final MedecinDAO medecinDAO = new MedecinDAO();

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();
        welcomeLabel.setText("Bonjour, " + session.getNomAffichage() + " \uD83D\uDC4B");

        // Chargement des stats en arrière-plan
        new Thread(() -> {
            int nbAssures  = assureDAO.count();
            int nbMedecins = medecinDAO.count();

            Platform.runLater(() -> {
                totalAssures.setText(String.valueOf(nbAssures));
                totalMedecins.setText(String.valueOf(nbMedecins));
                // À implémenter quand les DAO correspondants existent
                consultationsAujourdhui.setText("0");
                remboursementsAttente.setText("0");
            });
        }).start();
    }
}
