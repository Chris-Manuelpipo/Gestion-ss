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
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
            items.put("Tableau de bord",   new String[]{"🏠", "/fxml/dashboard.fxml"});
            items.put("Assurés",           new String[]{"👥", "/fxml/assures.fxml"});
            items.put("Médecins",          new String[]{"👨‍⚕️", "/fxml/medecins.fxml"});
            items.put("Remboursements",    new String[]{"💰", "/fxml/remboursements.fxml"});
            items.put("Journal d'audit",   new String[]{"📋", "/fxml/logs.fxml"});
        } else {
            items.put("Tableau de bord",   new String[]{"🏠", "/fxml/dashboard.fxml"});
            items.put("Mes consultations", new String[]{"📄", "/fxml/consultations.fxml"});
            items.put("Mes patients",      new String[]{"👤", "/fxml/patients.fxml"});
            items.put("Prescriptions",     new String[]{"💊", "/fxml/prescriptions.fxml"});
        }

        items.forEach((label, meta) -> {
            Button btn = createMenuButton(meta[0] + "  " + label, meta[1]);
            menuContainer.getChildren().add(btn);
        });
    }

    private Button createMenuButton(String text, String fxmlPath) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-item-btn");
        btn.setMaxWidth(Double.MAX_VALUE);

        String title = text.replaceAll("^.{2}\\s+", ""); // retirer l'émoji
        btn.setOnAction(e -> {
            activateButton(btn);
            loadView(fxmlPath, title);
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
            // Vue non encore implémentée → placeholder
            contentArea.getChildren().setAll(buildPlaceholder(pageTitle));
        }
    }

    private VBox buildPlaceholder(String titre) {
        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 48px;");

        Label t = new Label(titre);
        t.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Label sub = new Label("Cette section est en cours de développement.");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        VBox box = new VBox(12, icon, t, sub);
        box.setStyle("-fx-alignment: CENTER; -fx-padding: 80;");
        return box;
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
