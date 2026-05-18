package com.securitesociale.controller;

import com.securitesociale.dao.LogDAO;
import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.model.Medecin;
import com.securitesociale.model.TypeMedecin;
import com.securitesociale.util.AlertUtil;
import com.securitesociale.util.PasswordUtil;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class MedecinFormController {

    @FXML private TextField      nomField;
    @FXML private TextField      prenomField;
    @FXML private DatePicker     dateNaissancePicker;
    @FXML private ComboBox<String> sexeCombo;
    @FXML private TextField      emailField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private VBox           formationSection;
    @FXML private TextField      formationField;
    @FXML private VBox           specialiteSection;
    @FXML private TextField      specialiteField;
    @FXML private TextField      loginField;
    @FXML private Label          loginErrorLabel;
    @FXML private Label          passwordLabel;
    @FXML private Button         copyBtn;
    @FXML private Label          errorLabel;
    @FXML private Button         saveBtn;

    private static final String GENERATED_PASSWORD = "MedOSS@2026";
    private static final DateTimeFormatter FR_DATE  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MedecinDAO medecinDAO = new MedecinDAO();

    // ── Initialisation ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        sexeCombo.getItems().addAll("Masculin", "Féminin");
        typeCombo.getItems().addAll("Médecin Généraliste", "Médecin Spécialiste");
        passwordLabel.setText(GENERATED_PASSWORD);

        // Format de date français pour le DatePicker
        dateNaissancePicker.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(java.time.LocalDate d) {
                return d != null ? d.format(FR_DATE) : "";
            }
            @Override public java.time.LocalDate fromString(String s) {
                try { return (s == null || s.isBlank()) ? null : java.time.LocalDate.parse(s, FR_DATE); }
                catch (Exception e) { return null; }
            }
        });

        // Effacer l'erreur de login lors de la saisie
        loginField.textProperty().addListener((obs, old, val) -> clearLoginError());
    }

    // ── Gestion type médecin ──────────────────────────────────────────────────

    @FXML
    private void handleTypeChange() {
        boolean isGen  = "Médecin Généraliste".equals(typeCombo.getValue());
        boolean isSpec = "Médecin Spécialiste".equals(typeCombo.getValue());

        formationSection.setVisible(isGen);
        formationSection.setManaged(isGen);
        specialiteSection.setVisible(isSpec);
        specialiteSection.setManaged(isSpec);
    }

    // ── Copier le mot de passe ────────────────────────────────────────────────

    @FXML
    private void handleCopyPassword() {
        ClipboardContent content = new ClipboardContent();
        content.putString(GENERATED_PASSWORD);
        Clipboard.getSystemClipboard().setContent(content);
        copyBtn.setText("✔ Copié");
        copyBtn.setDisable(true);
    }

    // ── Sauvegarde ────────────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        clearError();
        clearLoginError();

        // ── Validation champs obligatoires ──
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
        if (typeCombo.getValue() == null) {
            showError("Veuillez sélectionner un type de médecin.");
            return;
        }
        if ("Médecin Spécialiste".equals(typeCombo.getValue())
                && specialiteField.getText().trim().isEmpty()) {
            markFieldError(specialiteField);
            showError("La spécialité est obligatoire pour un médecin spécialiste.");
            return;
        }
        String login = loginField.getText().trim();
        if (login.isEmpty()) {
            markFieldError(loginField);
            showError("Le login est obligatoire.");
            return;
        }

        // ── Unicité du login ──
        if (medecinDAO.loginExists(login)) {
            loginErrorLabel.setText("Ce login est déjà utilisé par un autre médecin.");
            loginErrorLabel.setVisible(true);
            loginErrorLabel.setManaged(true);
            markFieldError(loginField);
            return;
        }

        // ── Construction de l'objet ──
        Medecin m = new Medecin();
        m.setNom(nomField.getText().trim());
        m.setPrenom(prenomField.getText().trim());
        m.setEmail(emptyToNull(emailField.getText()));
        m.setDateNaissance(dateNaissancePicker.getValue());
        m.setSexe(sexeCombo.getValue());
        m.setLogin(login);
        m.setMotDePasse(PasswordUtil.hash(GENERATED_PASSWORD));

        TypeMedecin type = "Médecin Généraliste".equals(typeCombo.getValue())
            ? TypeMedecin.GENERALISTE : TypeMedecin.SPECIALISTE;
        m.setTypeMedecin(type);

        if (type == TypeMedecin.GENERALISTE) {
            m.setTypeFormation(emptyToNull(formationField.getText()));
        } else {
            m.setNomSpecialite(specialiteField.getText().trim());
        }

        // ── Enregistrement asynchrone ──
        saveBtn.setDisable(true);
        new Thread(() -> {
            try {
                medecinDAO.save(m);
                LogDAO.insert(
                    SessionManager.getInstance().getLoginUtilisateur(),
                    "CREATE_MEDECIN",
                    "Médecin inscrit : " + m.getNomComplet()
                        + " | type: " + type.getLibelle()
                        + " | login: " + login
                );
                Platform.runLater(() -> {
                    AlertUtil.info("Médecin inscrit avec succès",
                        "Le médecin " + m.getNomComplet() + " a été inscrit.\n\n"
                      + "Login         : " + login + "\n"
                      + "Mot de passe  : " + GENERATED_PASSWORD + "\n\n"
                      + "Communiquez ces identifiants au médecin.");
                    closeDialog();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    showError("Erreur lors de l'inscription : " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    // ── Utilitaires UI ────────────────────────────────────────────────────────

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
        // Effacer bordures rouges des champs
        for (Control c : new Control[]{nomField, prenomField, specialiteField, loginField}) {
            c.setStyle("");
        }
    }

    private void clearLoginError() {
        loginErrorLabel.setVisible(false);
        loginErrorLabel.setManaged(false);
        loginField.setStyle("");
    }

    private void markFieldError(Control field) {
        field.setStyle("-fx-border-color: #DC2626; -fx-border-width: 1.5px; -fx-border-radius: 8px;");
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
