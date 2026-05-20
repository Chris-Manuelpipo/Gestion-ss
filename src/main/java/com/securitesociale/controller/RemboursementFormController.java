package com.securitesociale.controller;

import com.securitesociale.model.Remboursement;
import com.securitesociale.service.RemboursementService;
import com.securitesociale.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class RemboursementFormController {

    @FXML private TextField natureField;
    @FXML private TextField tauxField;
    @FXML private TextField montantField;
    @FXML private ComboBox<String> modeReglementCombo;
    @FXML private TextField numFeuilleField;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    private final RemboursementService remboursementService = new RemboursementService();

    @FXML
    public void initialize() {
        modeReglementCombo.getItems().addAll("VIREMENT", "CASH");
    }

    @FXML
    private void handleSave() {
        clearError();

        String nature = natureField.getText().trim();
        if (nature.isEmpty()) {
            markFieldError(natureField);
            showError("La nature est obligatoire.");
            return;
        }

        String tauxStr = tauxField.getText().trim();
        if (tauxStr.isEmpty()) {
            markFieldError(tauxField);
            showError("Le taux est obligatoire.");
            return;
        }
        BigDecimal taux;
        try {
            taux = new BigDecimal(tauxStr);
            if (taux.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            markFieldError(tauxField);
            showError("Le taux doit être un nombre valide positif.");
            return;
        }

        String montantStr = montantField.getText().trim();
        if (montantStr.isEmpty()) {
            markFieldError(montantField);
            showError("Le montant est obligatoire.");
            return;
        }
        BigDecimal montant;
        try {
            montant = new BigDecimal(montantStr);
            if (montant.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            markFieldError(montantField);
            showError("Le montant doit être un nombre valide positif.");
            return;
        }

        String modeReglement = modeReglementCombo.getValue();
        if (modeReglement == null) {
            showError("Veuillez sélectionner le mode de règlement.");
            return;
        }

        String numFeuilleStr = numFeuilleField.getText().trim();
        if (numFeuilleStr.isEmpty()) {
            markFieldError(numFeuilleField);
            showError("Le numéro de feuille est obligatoire.");
            return;
        }
        int numFeuille;
        try {
            numFeuille = Integer.parseInt(numFeuilleStr);
        } catch (NumberFormatException e) {
            markFieldError(numFeuilleField);
            showError("Le numéro de feuille doit être un nombre valide.");
            return;
        }

        Remboursement r = new Remboursement();
        r.setNature(nature);
        r.setTaux(taux);
        r.setMontant(montant);
        r.setModeReglement(modeReglement);
        r.setStatut("EN_ATTENTE");
        r.setNumFeuille(numFeuille);

        saveBtn.setDisable(true);
        new Thread(() -> {
            try {
                remboursementService.save(r);
                Platform.runLater(() -> {
                    AlertUtil.info("Remboursement créé",
                        "Le remboursement N°" + r.getNumRemboursement() + " a été créé avec succès.");
                    closeDialog();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    showError("Erreur lors de la création : " + e.getMessage());
                });
            }
        }).start();
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
        for (Control c : new Control[]{natureField, tauxField, montantField, numFeuilleField}) {
            c.setStyle("");
        }
    }

    private void markFieldError(Control field) {
        field.setStyle("-fx-border-color: #DC2626; -fx-border-width: 1.5px; -fx-border-radius: 8px;");
    }
}
