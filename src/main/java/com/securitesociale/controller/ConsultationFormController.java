package com.securitesociale.controller;

import com.securitesociale.model.Assure;
import com.securitesociale.model.Consultation;
import com.securitesociale.service.AssureService;
import com.securitesociale.service.ConsultationService;
import com.securitesociale.util.AlertUtil;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class ConsultationFormController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Assure> assureCombo;
    @FXML private TextField motifField;
    @FXML private TextArea diagnosticArea;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    private final ConsultationService consultationService = new ConsultationService();
    private final AssureService assureService = new AssureService();

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        loadAssures();
    }

    private void loadAssures() {
        new Thread(() -> {
            try {
                List<Assure> assures = assureService.findAll();
                Platform.runLater(() ->
                    assureCombo.setItems(FXCollections.observableArrayList(assures)));
            } catch (Exception e) {
                Platform.runLater(() ->
                    showError("Erreur lors du chargement des assurés : " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleSave() {
        clearError();

        if (datePicker.getValue() == null) {
            showError("Veuillez sélectionner une date.");
            return;
        }
        if (assureCombo.getValue() == null) {
            showError("Veuillez sélectionner un patient.");
            return;
        }
        String motif = motifField.getText().trim();
        if (motif.isEmpty()) {
            showError("Le motif est obligatoire.");
            return;
        }

        if (!SessionManager.getInstance().isMedecin()) {
            showError("Seul un médecin peut enregistrer une consultation.");
            return;
        }

        Consultation c = new Consultation();
        c.setDateConsult(datePicker.getValue());
        c.setNumAssure(assureCombo.getValue().getNumAssure());
        c.setMotif(motif);
        c.setDiagnostic(emptyToNull(diagnosticArea.getText()));
        c.setNumMedecin(SessionManager.getInstance().getMedecinConnecte().getNumMedecin());

        saveBtn.setDisable(true);
        new Thread(() -> {
            try {
                consultationService.save(c);
                Platform.runLater(() -> {
                    AlertUtil.info("Consultation enregistrée",
                        "La consultation N°" + c.getNumConsultation() + " a été créée avec succès.");
                    closeDialog();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    showError("Erreur lors de l'enregistrement : " + e.getMessage());
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
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
