package com.securitesociale.controller;

import com.securitesociale.dao.LogDAO;
import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.model.Consultation;
import com.securitesociale.model.Medecin;
import com.securitesociale.model.Prescription;
import com.securitesociale.service.ConsultationService;
import com.securitesociale.service.PrescriptionService;
import com.securitesociale.util.AlertUtil;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class PrescriptionFormController {

    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<Consultation> consultationCombo;
    @FXML private TextArea contenuField;

    @FXML private VBox medicamentSection;
    @FXML private TextField codeMedicamentField;
    @FXML private TextField nomMedicamentField;
    @FXML private TextField posologieField;
    @FXML private TextField dosageField;

    @FXML private VBox consultationSpecSection;
    @FXML private TextField typeExamenField;
    @FXML private TextField motifMedicalField;
    @FXML private ComboBox<Medecin> specialisteCombo;

    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    private final PrescriptionService  prescriptionService  = new PrescriptionService();
    private final ConsultationService  consultationService  = new ConsultationService();
    private final MedecinDAO           medecinDAO           = new MedecinDAO();

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("MEDICAMENT", "CONSULTATION_SPECIALISTE");

        setupConsultationCombo();
        setupSpecialisteCombo();

        loadConsultations();
        loadSpecialistes();

        medicamentSection.setVisible(false);
        medicamentSection.setManaged(false);
        consultationSpecSection.setVisible(false);
        consultationSpecSection.setManaged(false);
    }

    private void setupConsultationCombo() {
        consultationCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Consultation c) {
                if (c == null) return "";
                return "N°" + c.getNumConsultation()
                    + " — " + (c.getNomAssure() != null ? c.getNomAssure() : "Assuré #" + c.getNumAssure())
                    + " (" + c.getDateConsult() + ")";
            }
            @Override
            public Consultation fromString(String s) { return null; }
        });
    }

    private void setupSpecialisteCombo() {
        specialisteCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Medecin m) {
                return m != null ? m.getNomComplet() + " (" + nvl(m.getNomSpecialite()) + ")" : "";
            }
            @Override
            public Medecin fromString(String s) { return null; }
        });
    }

    private void loadConsultations() {
        new Thread(() -> {
            try {
                List<Consultation> consultations;
                if (SessionManager.getInstance().isMedecin()) {
                    int numMedecin = SessionManager.getInstance().getMedecinConnecte().getNumMedecin();
                    consultations = consultationService.findByMedecin(numMedecin);
                } else {
                    consultations = consultationService.findAll();
                }
                Platform.runLater(() ->
                    consultationCombo.setItems(FXCollections.observableArrayList(consultations)));
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur", "Impossible de charger les consultations", e));
            }
        }).start();
    }

    private void loadSpecialistes() {
        new Thread(() -> {
            try {
                List<Medecin> specialistes = medecinDAO.findAllSpecialistes();
                Platform.runLater(() ->
                    specialisteCombo.setItems(FXCollections.observableArrayList(specialistes)));
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur", "Impossible de charger les spécialistes", e));
            }
        }).start();
    }

    @FXML
    private void handleTypeChange() {
        boolean isMedicament = "MEDICAMENT".equals(typeCombo.getValue());
        boolean isConsultationSpec = "CONSULTATION_SPECIALISTE".equals(typeCombo.getValue());

        medicamentSection.setVisible(isMedicament);
        medicamentSection.setManaged(isMedicament);
        consultationSpecSection.setVisible(isConsultationSpec);
        consultationSpecSection.setManaged(isConsultationSpec);
    }

    @FXML
    private void handleSave() {
        clearError();

        if (typeCombo.getValue() == null) {
            showError("Veuillez sélectionner un type de prescription.");
            return;
        }
        if (consultationCombo.getValue() == null) {
            showError("Veuillez sélectionner une consultation.");
            return;
        }
        if (contenuField.getText().trim().isEmpty()) {
            showError("Le contenu est obligatoire.");
            return;
        }

        String type = typeCombo.getValue();

        if ("MEDICAMENT".equals(type)) {
            if (codeMedicamentField.getText().trim().isEmpty()) {
                showError("Le code médicament est obligatoire.");
                return;
            }
            if (nomMedicamentField.getText().trim().isEmpty()) {
                showError("Le nom du médicament est obligatoire.");
                return;
            }
        }

        if ("CONSULTATION_SPECIALISTE".equals(type)) {
            if (typeExamenField.getText().trim().isEmpty()) {
                showError("Le type d'examen est obligatoire.");
                return;
            }
            if (motifMedicalField.getText().trim().isEmpty()) {
                showError("Le motif médical est obligatoire.");
                return;
            }
            if (specialisteCombo.getValue() == null) {
                showError("Veuillez sélectionner un spécialiste.");
                return;
            }
        }

        Prescription p = new Prescription();
        p.setType(type);
        p.setContenu(contenuField.getText().trim());
        p.setNumConsultation(consultationCombo.getValue().getNumConsultation());
        p.setDatePrescription(LocalDate.now());

        if ("MEDICAMENT".equals(type)) {
            p.setCodeMedicament(codeMedicamentField.getText().trim());
            p.setNomMedicament(nomMedicamentField.getText().trim());
            p.setPosologie(emptyToNull(posologieField.getText()));
            p.setDosage(emptyToNull(dosageField.getText()));
        } else {
            p.setTypeExamen(typeExamenField.getText().trim());
            p.setMotifMedical(motifMedicalField.getText().trim());
            Medecin spec = specialisteCombo.getValue();
            p.setNumSpecialiste(spec.getNumMedecin());
            p.setNomSpecialiste(spec.getNomComplet());
        }

        saveBtn.setDisable(true);
        new Thread(() -> {
            try {
                prescriptionService.save(p);
                LogDAO.insert(
                    SessionManager.getInstance().getLoginUtilisateur(),
                    "CREATE_PRESCRIPTION",
                    "Prescription créée N°" + p.getNumPrescription()
                        + " | type: " + type
                        + " | consultation: " + p.getNumConsultation()
                );
                Platform.runLater(() -> {
                    AlertUtil.info("Prescription créée",
                        "La prescription a été créée avec succès.");
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
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
