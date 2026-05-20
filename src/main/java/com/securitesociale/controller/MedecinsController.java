package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.dao.LogDAO;
import com.securitesociale.dao.MedecinDAO;
import com.securitesociale.model.Medecin;
import com.securitesociale.model.TypeMedecin;
import com.securitesociale.util.AlertUtil;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MedecinsController {

    @FXML private Label statTotal;
    @FXML private Label statGeneralistes;
    @FXML private Label statSpecialistes;

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<Medecin> tableView;
    @FXML private TableColumn<Medecin, String> colNum;
    @FXML private TableColumn<Medecin, String> colNom;
    @FXML private TableColumn<Medecin, String> colType;
    @FXML private TableColumn<Medecin, String> colSpecialite;
    @FXML private TableColumn<Medecin, String> colEmail;
    @FXML private TableColumn<Medecin, String> colLogin;
    @FXML private TableColumn<Medecin, Void>   colActions;

    @FXML private Button btnPrevPage;
    @FXML private Label  pageInfoLabel;
    @FXML private Button btnNextPage;

    private final MedecinDAO medecinDAO = new MedecinDAO();

    private ObservableList<Medecin> allMedecins;
    private FilteredList<Medecin>   filteredMedecins;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    // ── Initialisation ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadMedecins();
    }

    // ── Colonnes ──────────────────────────────────────────────────────────────

    private void setupColumns() {
        colNum.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getNumMedecin())));

        colNom.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getNomComplet()));

        colType.setCellValueFactory(d -> {
            TypeMedecin t = d.getValue().getTypeMedecin();
            return new SimpleStringProperty(t != null ? t.getLibelle() : "—");
        });
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                if (item.contains("Généraliste")) badge.getStyleClass().add("badge-green");
                else                              badge.getStyleClass().add("badge-blue");
                setGraphic(badge);
                setText(null);
            }
        });

        colSpecialite.setCellValueFactory(d -> {
            Medecin m = d.getValue();
            if (m.getTypeMedecin() == TypeMedecin.SPECIALISTE)
                return new SimpleStringProperty(nvl(m.getNomSpecialite()));
            if (m.getTypeMedecin() == TypeMedecin.GENERALISTE)
                return new SimpleStringProperty(nvl(m.getTypeFormation()));
            return new SimpleStringProperty("—");
        });

        colEmail.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getEmail())));

        colLogin.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getLogin()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnSuppr = new Button("🗑 Supprimer");
            {
                btnSuppr.getStyleClass().add("btn-danger");
                btnSuppr.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-pref-height: 30px;");
                btnSuppr.setOnAction(e -> handleSupprimer(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnSuppr);
            }
        });
    }

    // ── Recherche ─────────────────────────────────────────────────────────────

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (filteredMedecins == null) return;
            String lower = val.toLowerCase().trim();
            filteredMedecins.setPredicate(m -> {
                if (lower.isEmpty()) return true;
                return m.getNom().toLowerCase().contains(lower)
                    || m.getPrenom().toLowerCase().contains(lower)
                    || m.getLogin().toLowerCase().contains(lower)
                    || (m.getEmail() != null && m.getEmail().toLowerCase().contains(lower))
                    || (m.getNomSpecialite() != null && m.getNomSpecialite().toLowerCase().contains(lower));
            });
            currentPage = 0;
            refreshTable();
        });
    }

    // ── Chargement des données ────────────────────────────────────────────────

    private void loadMedecins() {
        countLabel.setText("Chargement…");
        new Thread(() -> {
            try {
                List<Medecin> medecins = medecinDAO.findAll();
                int gen  = medecinDAO.countGeneralistes();
                int spec = medecinDAO.countSpecialistes();
                Platform.runLater(() -> {
                    statTotal.setText(String.valueOf(medecins.size()));
                    statGeneralistes.setText(String.valueOf(gen));
                    statSpecialistes.setText(String.valueOf(spec));

                    allMedecins      = FXCollections.observableArrayList(medecins);
                    filteredMedecins = new FilteredList<>(allMedecins, m -> true);
                    currentPage      = 0;
                    refreshTable();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de charger les médecins", e));
            }
        }).start();
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    private void refreshTable() {
        if (filteredMedecins == null) return;

        int total     = filteredMedecins.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage   = Math.min(currentPage, pageCount - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tableView.setItems(FXCollections.observableArrayList(
            total == 0 ? List.of() : filteredMedecins.subList(from, to)));

        countLabel.setText(total + " médecin" + (total > 1 ? "s" : ""));
        pageInfoLabel.setText("Page " + (currentPage + 1) + " / " + pageCount);
        btnPrevPage.setDisable(currentPage == 0);
        btnNextPage.setDisable(currentPage >= pageCount - 1);
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) { currentPage--; refreshTable(); }
    }

    @FXML
    private void handleNextPage() {
        int pageCount = (int) Math.ceil((double) filteredMedecins.size() / PAGE_SIZE);
        if (currentPage < pageCount - 1) { currentPage++; refreshTable(); }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @FXML
    private void handleInscrire() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/medecin_form.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Inscrire un médecin");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                App.class.getResource("/css/style.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();

            loadMedecins();
        } catch (IOException e) {
            AlertUtil.erreurDetail("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }

    private void handleSupprimer(Medecin m) {
        if (!AlertUtil.confirmer("Archiver le médecin",
            "Voulez-vous vraiment archiver " + m.getNomComplet() + " ?\n\n"
          + "Ses consultations passées seront conservées.\n"
          + "Il ne pourra plus se connecter.")) return;

        new Thread(() -> {
            try {
                medecinDAO.delete(m.getNumMedecin());
                LogDAO.insert(
                    SessionManager.getInstance().getLoginUtilisateur(),
                    "ARCHIVE_MEDECIN",
                    "Médecin archivé : " + m.getNomComplet() + " (ID: " + m.getNumMedecin() + ")"
                );
                Platform.runLater(() -> {
                    AlertUtil.info("Archivé", m.getNomComplet() + " a été archivé avec succès.");
                    loadMedecins();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible d'archiver le médecin", e));
            }
        }).start();
    }

    // ── Utilitaire ────────────────────────────────────────────────────────────

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
