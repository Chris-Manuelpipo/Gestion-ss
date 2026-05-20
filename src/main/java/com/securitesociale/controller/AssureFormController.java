package com.securitesociale.controller;

import com.securitesociale.model.Assure;
import com.securitesociale.model.Medecin;
import com.securitesociale.service.AssureService;
import com.securitesociale.service.MedecinService;
import com.securitesociale.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AssureFormController {

    @FXML private TextField       nomField;
    @FXML private TextField       prenomField;
    @FXML private DatePicker      dateNaissancePicker;
    @FXML private ComboBox<String> sexeCombo;
    @FXML private TextField       emailField;
    @FXML private TextField       numCompteField;
    @FXML private ComboBox<String> medecinCombo;
    @FXML private Label           errorLabel;
    @FXML private Button          saveBtn;

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AssureService  assureService  = new AssureService();
    private final MedecinService medecinService = new MedecinService();

    private Assure existingAssure;
    private List<Medecin> generalistes;

    @FXML
    public void initialize() {
        sexeCombo.getItems().addAll("Masculin", "Féminin");

        dateNaissancePicker.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(java.time.LocalDate d) {
                return d != null ? d.format(FR_DATE) : "";
            }
            @Override public java.time.LocalDate fromString(String s) {
                try { return (s == null || s.isBlank()) ? null : java.time.LocalDate.parse(s, FR_DATE); }
                catch (Exception e) { return null; }
            }
        });

        new Thread(() -> {
            try {
                List<Medecin> meds = medecinService.findAllGeneralistes();
                Platform.runLater(() -> {
                    generalistes = meds;
                    for (Medecin m : meds) {
                        medecinCombo.getItems().add(m.getNomComplet());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur", "Impossible de charger la liste des médecins", e));
            }
        }).start();
    }

    public void setAssure(Assure a) {
        this.existingAssure = a;
        nomField.setText(a.getNom());
        prenomField.setText(a.getPrenom());
        emailField.setText(a.getEmail());
        dateNaissancePicker.setValue(a.getDateNaissance());
        sexeCombo.setValue(a.getSexe());
        numCompteField.setText(a.getNumCompteBancaire());

        saveBtn.setText("✔ Modifier l'assuré");
    }

    @FXML
    private void handleSave() {
        clearError();

        if (nomField.getText().trim().isEmpty()) {
            markFieldError(nomField);
            showError("Le nom est obligatoire.");
            return;
        }
        if (prenomField.getText().trim().isEmpty()) {
            markFieldError(prenomField);
            showError("Le prénom est obligatoire.");
            return;
        }

        Assure a = existingAssure != null ? existingAssure : new Assure();
        a.setNom(nomField.getText().trim());
        a.setPrenom(prenomField.getText().trim());
        a.setEmail(emptyToNull(emailField.getText()));
        a.setDateNaissance(dateNaissancePicker.getValue());
        a.setSexe(sexeCombo.getValue());
        a.setNumCompteBancaire(emptyToNull(numCompteField.getText()));

        int idx = medecinCombo.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && generalistes != null && idx < generalistes.size()) {
            a.setNumMedecinTraitant(generalistes.get(idx).getNumMedecin());
        }

        saveBtn.setDisable(true);
        new Thread(() -> {
            try {
                if (existingAssure != null) {
                    assureService.update(a);
                } else {
                    assureService.save(a);
                }
                Platform.runLater(() -> {
                    AlertUtil.info("Succès",
                        "L'assuré " + a.getNomComplet() + " a été "
                        + (existingAssure != null ? "modifié" : "inscrit") + " avec succès.");
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
        for (Control c : new Control[]{nomField, prenomField}) {
            c.setStyle("");
        }
    }

    private void markFieldError(Control field) {
        field.setStyle("-fx-border-color: #DC2626; -fx-border-width: 1.5px; -fx-border-radius: 8px;");
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
