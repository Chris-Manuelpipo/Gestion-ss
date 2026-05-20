package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.util.AlertUtil;
import com.securitesociale.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {

    /** Référence statique utilisée par les sous-contrôleurs pour déclencher la navigation. */
    public static MainController instance;

    @FXML private StackPane contentArea;
    @FXML private VBox      menuContainer;
    @FXML private Label     sidebarUsername;
    @FXML private Label     sidebarRole;
    @FXML private Label     userAvatarLabel;
    @FXML private Label     topbarPageTitle;
    @FXML private Label     topbarDate;

    private Button activeMenuButton;

    // ── Initialisation ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        instance = this;
        SessionManager session = SessionManager.getInstance();

        // Remplir les infos utilisateur
        sidebarUsername.setText(session.getNomAffichage());
        sidebarRole.setText(session.getRoleAffichage());
        userAvatarLabel.setText(String.valueOf(
                session.getNomAffichage().charAt(0)).toUpperCase());

        // Date courante dans la topbar
        topbarDate.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy",
                        java.util.Locale.FRENCH)));

        // Construire le menu selon le rôle
        buildMenu(session.isAgentSS());

        // Vue par défaut : tableau de bord
        loadView("/fxml/dashboard.fxml", "Tableau de bord");
    }

    // ── Construction du menu ──────────────────────────────────────────────────

    private void buildMenu(boolean isAgentSS) {
        Map<String, String[]> items = new LinkedHashMap<>();

        if (isAgentSS) {
            items.put("Tableau de bord",   new String[]{"fas-chart-pie",           "/fxml/dashboard.fxml"});
            items.put("Assurés",           new String[]{"fas-users",               "/fxml/assures.fxml"});
            items.put("Médecins",          new String[]{"fas-user-md",             "/fxml/medecins.fxml"});
            items.put("Remboursements",    new String[]{"fas-file-invoice-dollar", "/fxml/remboursements.fxml"});
            items.put("Journal d'audit",   new String[]{"fas-history",             "/fxml/logs.fxml"});
        } else {
            items.put("Tableau de bord",     new String[]{"fas-chart-pie",     "/fxml/dashboard.fxml"});
            items.put("Mes consultations",   new String[]{"fas-stethoscope",   "/fxml/consultations.fxml"});
            items.put("Mes patients",        new String[]{"fas-users",         "/fxml/patients.fxml"});
            items.put("Prescriptions",       new String[]{"fas-pills",         "/fxml/prescriptions.fxml"});
            items.put("Feuilles de maladie", new String[]{"fas-file-medical",  "/fxml/feuilles_maladie.fxml"});
        }

        // Ajout d'un label de catégorie au-dessus du menu
        Label categoryLabel = new Label("NAVIGATION");
        categoryLabel.getStyleClass().add("sidebar-category-label");
        menuContainer.getChildren().add(categoryLabel);

        items.forEach((label, meta) -> {
            Button btn = createMenuButton(label, meta[0], meta[1]);
            menuContainer.getChildren().add(btn);
        });

        if (!isAgentSS) {
            Region spacer = new Region();
            spacer.setMinHeight(10);
            VBox.setVgrow(spacer, Priority.ALWAYS);
            menuContainer.getChildren().add(spacer);

            Button changePwdBtn = new Button("Mon compte");
            changePwdBtn.getStyleClass().add("menu-item-btn");
            changePwdBtn.setMaxWidth(Double.MAX_VALUE);
            FontIcon pwdIcon = new FontIcon("fas-lock");
            pwdIcon.setIconSize(16);
            pwdIcon.getStyleClass().add("menu-icon");
            changePwdBtn.setGraphic(pwdIcon);
            changePwdBtn.setGraphicTextGap(10);
            changePwdBtn.setContentDisplay(ContentDisplay.LEFT);
            changePwdBtn.setOnAction(e -> handleChangePassword());
            menuContainer.getChildren().add(changePwdBtn);
        }
    }

    private Button createMenuButton(String text, String iconCode, String fxmlPath) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-item-btn");
        btn.setMaxWidth(Double.MAX_VALUE);

        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        icon.getStyleClass().add("menu-icon");
        btn.setGraphic(icon);
        btn.setGraphicTextGap(10);
        btn.setContentDisplay(ContentDisplay.LEFT);

        btn.setOnAction(e -> {
            activateButton(btn);
            loadView(fxmlPath, text);
        });
        return btn;
    }

    private void activateButton(Button btn) {
        if (activeMenuButton != null) {
            activeMenuButton.getStyleClass().remove("menu-item-btn-active");
        }
        btn.getStyleClass().add("menu-item-btn-active");
        activeMenuButton = btn;
    }

    // ── Chargement des vues ───────────────────────────────────────────────────

    public void loadView(String fxmlPath, String pageTitle) {
        topbarPageTitle.setText(pageTitle);
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("[Main] Erreur chargement vue " + fxmlPath + " : " + e.getMessage());
        }
    }

    // ── Mot de passe ──────────────────────────────────────────────────────────

    private void handleChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/password_change.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Changer le mot de passe");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                App.class.getResource("/css/style.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            AlertUtil.erreurDetail("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }

    // ── Déconnexion ───────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        if (!AlertUtil.confirmer("Déconnexion",
                "Voulez-vous vraiment vous déconnecter ?")) return;

        SessionManager.getInstance().deconnecter();

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}