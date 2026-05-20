package com.securitesociale.controller;

import com.securitesociale.service.MedecinService;
import com.securitesociale.util.AlertUtil;
import com.securitesociale.util.PasswordUtil;
import com.securitesociale.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class PasswordChangeController {

    @FXML private PasswordField currentField;
    @FXML private PasswordField newField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    private final MedecinService medecinService = new MedecinService();

    @FXML
    private void initialize() {
        currentField.textProperty().addListener((o, ov, nv) -> clearError());
        newField.textProperty().addListener((o, ov, nv) -> clearError());
        confirmField.textProperty().addListener((o, ov, nv) -> clearError());
    }

    @FXML
    private void handleSave() {
        clearError();

        String current = currentField.getText();
        String newPwd = newField.getText();
        String confirm = confirmField.getText();

        if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            showError("Tous les champs sont obligatoires.");
            return;
        }

        var medecin = SessionManager.getInstance().getMedecinConnecte();
        if (medecin == null) {
            showError("Session invalide.");
            return;
        }

        if (!PasswordUtil.verify(current, medecin.getMotDePasse())) {
            showError("Le mot de passe actuel est incorrect.");
            return;
        }

        if (!newPwd.equals(confirm)) {
            showError("Les nouveaux mots de passe ne correspondent pas.");
            return;
        }

        if (newPwd.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }

        saveBtn.setDisable(true);
        try {
            medecinService.updatePassword(medecin.getNumMedecin(), PasswordUtil.hash(newPwd));
            AlertUtil.info("Mot de passe modifié",
                "Votre mot de passe a été changé avec succès.");
            closeDialog();
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            saveBtn.setDisable(false);
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
