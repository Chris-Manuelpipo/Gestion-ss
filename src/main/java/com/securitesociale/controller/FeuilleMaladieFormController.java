package com.securitesociale.controller;

import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.model.Assure;
import com.securitesociale.model.Consultation;
import com.securitesociale.model.FeuilleMaladie;
import com.securitesociale.model.Medecin;
import com.securitesociale.model.Prescription;
import com.securitesociale.service.AssureService;
import com.securitesociale.service.FeuilleMaladieService;
import com.securitesociale.util.AlertUtil;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FeuilleMaladieFormController {

    @FXML private ComboBox<Assure> assureCombo;

    @FXML private TextField poidsField;
    @FXML private TextField tailleField;
    @FXML private TextField temperatureField;
    @FXML private TextField tensionField;
    @FXML private TextField frequenceCardiaqueField;
    @FXML private TextField frequenceRespiratoireField;
    @FXML private TextField saturationField;
    @FXML private TextArea antecedentsArea;

    @FXML private DatePicker dateConsultPicker;
    @FXML private TextField motifField;
    @FXML private TextArea symptomesArea;
    @FXML private TextArea diagnosticArea;

    @FXML private ComboBox<String> prescriptionTypeCombo;
    @FXML private VBox medicamentFields;
    @FXML private TextField codeMedicamentField;
    @FXML private TextField nomMedicamentField;
    @FXML private TextField posologieField;
    @FXML private TextField dosageField;
    @FXML private VBox consultationSpecFields;
    @FXML private TextField typeExamenField;
    @FXML private TextField motifMedicalField;
    @FXML private TextField specialisteField;
    @FXML private Button btnSearchSpecialiste;

    @FXML private ListView<String> prescriptionListView;
    @FXML private Button btnAddPrescription;
    @FXML private Button btnRemovePrescription;

    @FXML private TextArea observationsArea;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    private final AssureService assureService = new AssureService();
    private final FeuilleMaladieService feuilleService = new FeuilleMaladieService();
    private final MedecinDAO medecinDAO = new MedecinDAO();

    private final ObservableList<String> prescriptionItems = FXCollections.observableArrayList();
    private final List<Prescription> prescriptions = new ArrayList<>();

    @FXML
    public void initialize() {
        dateConsultPicker.setValue(LocalDate.now());
        prescriptionTypeCombo.getItems().addAll("MEDICAMENT", "CONSULTATION_SPECIALISTE");
        prescriptionListView.setItems(prescriptionItems);

        medicamentFields.setVisible(false);
        medicamentFields.setManaged(false);
        consultationSpecFields.setVisible(false);
        consultationSpecFields.setManaged(false);

        loadAssures();
    }

    private void loadAssures() {
        new Thread(() -> {
            try {
                int numMedecin = SessionManager.getInstance().getMedecinConnecte().getNumMedecin();
                List<Assure> assures = assureService.findAll().stream()
                    .filter(a -> a.getNumMedecinTraitant() == numMedecin)
                    .collect(Collectors.toList());
                Platform.runLater(() ->
                    assureCombo.setItems(FXCollections.observableArrayList(assures)));
            } catch (Exception e) {
                Platform.runLater(() ->
                    showError("Erreur chargement assurés : " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handlePrescriptionTypeChange() {
        String type = prescriptionTypeCombo.getValue();
        boolean isMedic = "MEDICAMENT".equals(type);
        boolean isSpec = "CONSULTATION_SPECIALISTE".equals(type);

        medicamentFields.setVisible(isMedic);
        medicamentFields.setManaged(isMedic);
        consultationSpecFields.setVisible(isSpec);
        consultationSpecFields.setManaged(isSpec);
    }

    @FXML
    private void handleSearchSpecialiste() {
        List<Medecin> specialistes;
        try {
            specialistes = medecinDAO.findAllSpecialistes();
        } catch (Exception e) {
            showError("Erreur chargement spécialistes : " + e.getMessage());
            return;
        }

        if (specialistes.isEmpty()) {
            AlertUtil.info("Recherche", "Aucun spécialiste trouvé.");
            return;
        }

        ChoiceDialog<Medecin> dialog = new ChoiceDialog<>(specialistes.get(0), specialistes);
        dialog.setTitle("Sélectionner un spécialiste");
        dialog.setHeaderText("Choisissez le médecin spécialiste");
        dialog.setContentText("Spécialiste :");

        dialog.showAndWait().ifPresent(spec -> {
            specialisteField.setText(spec.getNomComplet() + " (" + nvl(spec.getNomSpecialite()) + ")");
            specialisteField.setUserData(spec);
        });
    }

    @FXML
    private void handleAddPrescription() {
        String type = prescriptionTypeCombo.getValue();
        if (type == null) {
            showError("Sélectionnez un type de prescription.");
            return;
        }

        Prescription p = new Prescription();
        p.setType(type);
        p.setDatePrescription(LocalDate.now());

        if ("MEDICAMENT".equals(type)) {
            if (codeMedicamentField.getText().trim().isEmpty() ||
                nomMedicamentField.getText().trim().isEmpty()) {
                showError("Le code et le nom du médicament sont obligatoires.");
                return;
            }
            p.setCodeMedicament(codeMedicamentField.getText().trim());
            p.setNomMedicament(nomMedicamentField.getText().trim());
            p.setPosologie(emptyToNull(posologieField.getText()));
            p.setDosage(emptyToNull(dosageField.getText()));
            p.setContenu("Médicament : " + p.getNomMedicament() + " (" + p.getCodeMedicament() + ")");

            clearMedicamentFields();
        } else {
            if (typeExamenField.getText().trim().isEmpty()) {
                showError("Le type d'examen est obligatoire.");
                return;
            }
            p.setTypeExamen(typeExamenField.getText().trim());
            p.setMotifMedical(emptyToNull(motifMedicalField.getText()));
            Medecin spec = (Medecin) specialisteField.getUserData();
            if (spec == null) {
                showError("Veuillez rechercher et sélectionner un spécialiste.");
                return;
            }
            p.setNumSpecialiste(spec.getNumMedecin());
            p.setNomSpecialiste(spec.getNomComplet());
            p.setContenu("Consultation spécialiste : " + p.getTypeExamen()
                + " — Dr " + p.getNomSpecialiste());

            clearConsultationSpecFields();
        }

        prescriptions.add(p);
        prescriptionItems.add(0, p.getContenu());
        clearError();
    }

    @FXML
    private void handleRemovePrescription() {
        int idx = prescriptionListView.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            showError("Sélectionnez une prescription à supprimer.");
            return;
        }
        prescriptions.remove(idx);
        prescriptionItems.remove(idx);
    }

    @FXML
    private void handleSave() {
        clearError();

        if (assureCombo.getValue() == null) {
            showError("Veuillez sélectionner un patient.");
            return;
        }
        if (motifField.getText().trim().isEmpty()) {
            showError("Le motif est obligatoire.");
            return;
        }
        if (dateConsultPicker.getValue() == null) {
            showError("Veuillez sélectionner une date.");
            return;
        }

        Assure assure = assureCombo.getValue();
        Medecin medecin = SessionManager.getInstance().getMedecinConnecte();

        Consultation consultation = new Consultation();
        consultation.setDateConsult(dateConsultPicker.getValue());
        consultation.setNumAssure(assure.getNumAssure());
        consultation.setNumMedecin(medecin.getNumMedecin());
        consultation.setMotif(motifField.getText().trim());
        consultation.setDiagnostic(emptyToNull(diagnosticArea.getText()));

        FeuilleMaladie feuille = new FeuilleMaladie();
        feuille.setDateEmission(LocalDate.now());
        feuille.setStatut("EN_ATTENTE");
        feuille.setNumMedecin(medecin.getNumMedecin());

        feuille.setPoids(parseDecimal(poidsField.getText()));
        feuille.setTaille(parseDecimal(tailleField.getText()));
        feuille.setTemperature(parseDecimal(temperatureField.getText()));
        feuille.setTensionArterielle(emptyToNull(tensionField.getText()));
        feuille.setFrequenceCardiaque(parseInt(frequenceCardiaqueField.getText()));
        feuille.setFrequenceRespiratoire(parseInt(frequenceRespiratoireField.getText()));
        feuille.setSaturationOxygene(parseDecimal(saturationField.getText()));
        feuille.setAntecedents(emptyToNull(antecedentsArea.getText()));
        feuille.setSymptomes(emptyToNull(symptomesArea.getText()));
        feuille.setDiagnostic(emptyToNull(diagnosticArea.getText()));
        feuille.setObservations(emptyToNull(observationsArea.getText()));

        String traitement = prescriptions.stream()
            .map(Prescription::getContenu)
            .collect(Collectors.joining("; "));
        feuille.setTraitementPrescrit(emptyToNull(traitement));

        saveBtn.setDisable(true);
        new Thread(() -> {
            try {
                feuilleService.saveWithConsultation(feuille, consultation, prescriptions);
                Platform.runLater(() -> {
                    AlertUtil.info("Feuille de maladie créée",
                        "La feuille de maladie N°" + feuille.getNumFeuille()
                            + " a été créée avec succès.\n"
                            + prescriptions.size() + " prescription(s) enregistrée(s).");
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

    private void clearMedicamentFields() {
        codeMedicamentField.clear();
        nomMedicamentField.clear();
        posologieField.clear();
        dosageField.clear();
    }

    private void clearConsultationSpecFields() {
        typeExamenField.clear();
        motifMedicalField.clear();
        specialisteField.clear();
        specialisteField.setUserData(null);
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

    private static java.math.BigDecimal parseDecimal(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return new java.math.BigDecimal(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private static Integer parseInt(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
