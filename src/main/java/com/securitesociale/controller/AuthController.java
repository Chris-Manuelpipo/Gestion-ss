package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.dao.AgentDAO;
import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.model.Medecin;
import com.securitesociale.util.PasswordUtil;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {

    @FXML private TextField     loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;

    private final AgentDAO   agentDAO   = new AgentDAO();
    private final MedecinDAO medecinDAO = new MedecinDAO();

    @FXML
    public void initialize() {
        // Connexion avec Entrée depuis le champ mot de passe
        passwordField.setOnAction(e -> handleLogin());

        // Cacher l'erreur quand l'utilisateur re-saisit
        loginField.textProperty().addListener((o, ov, nv) -> hideError());
        passwordField.textProperty().addListener((o, ov, nv) -> hideError());
    }

    @FXML
    private void handleLogin() {
        String login = loginField.getText().trim();
        String mdp   = passwordField.getText();

        if (login.isEmpty() || mdp.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Connexion en cours…");

        // Authentification en arrière-plan pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                boolean authentifie = authenticate(login, mdp);
                Platform.runLater(() -> {
                    if (authentifie) {
                        navigateToMain();
                    } else {
                        showError("Identifiant ou mot de passe incorrect.");
                        loginButton.setDisable(false);
                        loginButton.setText("Se connecter");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Erreur de connexion à la base de données.");
                    loginButton.setDisable(false);
                    loginButton.setText("Se connecter");
                });
            }
        }).start();
    }

    /** Retourne true si les identifiants sont valides et remplit la session. */
    private boolean authenticate(String login, String mdp) {
        // 1. Vérifier si c'est l'agent SS
        String agentHash = agentDAO.getHashedPassword(login);
        if (agentHash != null && PasswordUtil.verify(mdp, agentHash)) {
            SessionManager.getInstance().connecterAgentSS(login);
            return true;
        }

        // 2. Vérifier si c'est un médecin
        Medecin medecin = medecinDAO.findByLogin(login);
        if (medecin != null && PasswordUtil.verify(mdp, medecin.getMotDePasse())) {
            SessionManager.getInstance().connecterMedecin(medecin);
            return true;
        }

        return false;
    }

    private void navigateToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showError("Impossible de charger l'interface principale.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
