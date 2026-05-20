package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.model.Consultation;
import com.securitesociale.service.ConsultationService;
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
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class ConsultationsController {

    @FXML private Label statTotal;
    @FXML private Label statToday;

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<Consultation> tableView;
    @FXML private TableColumn<Consultation, String> colNum;
    @FXML private TableColumn<Consultation, String> colDate;
    @FXML private TableColumn<Consultation, String> colPatient;
    @FXML private TableColumn<Consultation, String> colMotif;
    @FXML private TableColumn<Consultation, String> colDiagnostic;
    @FXML private TableColumn<Consultation, Void>   colActions;

    @FXML private Button btnPrevPage;
    @FXML private Label  pageInfoLabel;
    @FXML private Button btnNextPage;
    @FXML private Button btnNewConsultation;

    private final ConsultationService consultationService = new ConsultationService();

    private ObservableList<Consultation> allConsultations;
    private FilteredList<Consultation>   filteredConsultations;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadConsultations();
    }

    private void setupColumns() {
        colNum.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getNumConsultation())));

        colDate.setCellValueFactory(d -> {
            LocalDate dt = d.getValue().getDateConsult();
            return new SimpleStringProperty(dt != null ? dt.toString() : "—");
        });

        colPatient.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getNomAssure())));

        colMotif.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getMotif())));

        colDiagnostic.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getDiagnostic())));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir = new Button("👁 Voir détails");
            private final Button btnSuppr = new Button("🗑 Supprimer");
            private final HBox pane = new HBox(8, btnVoir, btnSuppr);

            {
                btnVoir.getStyleClass().add("btn-secondary");
                btnVoir.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-pref-height: 30px;");
                btnVoir.setOnAction(e -> handleVoirDetails(getTableView().getItems().get(getIndex())));

                btnSuppr.getStyleClass().add("btn-danger");
                btnSuppr.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-pref-height: 30px;");
                btnSuppr.setOnAction(e -> handleSupprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (filteredConsultations == null) return;
            String lower = val.toLowerCase().trim();
            filteredConsultations.setPredicate(c -> {
                if (lower.isEmpty()) return true;
                return (c.getNomAssure() != null && c.getNomAssure().toLowerCase().contains(lower))
                    || (c.getMotif() != null && c.getMotif().toLowerCase().contains(lower))
                    || (c.getDiagnostic() != null && c.getDiagnostic().toLowerCase().contains(lower));
            });
            currentPage = 0;
            refreshTable();
        });
    }

    private void loadConsultations() {
        if (!SessionManager.getInstance().isMedecin()) return;

        int numMedecin = SessionManager.getInstance().getMedecinConnecte().getNumMedecin();

        countLabel.setText("Chargement…");
        new Thread(() -> {
            try {
                List<Consultation> consultations = consultationService.findByMedecin(numMedecin);

                LocalDate now = LocalDate.now();
                LocalDate firstOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
                long totalMois = consultations.stream()
                    .filter(c -> c.getDateConsult() != null && !c.getDateConsult().isBefore(firstOfMonth))
                    .count();
                long todayCount = consultations.stream()
                    .filter(c -> now.equals(c.getDateConsult()))
                    .count();

                Platform.runLater(() -> {
                    statTotal.setText(String.valueOf(totalMois));
                    statToday.setText(String.valueOf(todayCount));

                    allConsultations      = FXCollections.observableArrayList(consultations);
                    filteredConsultations = new FilteredList<>(allConsultations, c -> true);
                    currentPage = 0;
                    refreshTable();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de charger les consultations", e));
            }
        }).start();
    }

    private void refreshTable() {
        if (filteredConsultations == null) return;

        int total     = filteredConsultations.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage   = Math.min(currentPage, pageCount - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tableView.setItems(FXCollections.observableArrayList(
            total == 0 ? List.of() : filteredConsultations.subList(from, to)));

        countLabel.setText(total + " consultation" + (total > 1 ? "s" : ""));
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
        int pageCount = (int) Math.ceil((double) filteredConsultations.size() / PAGE_SIZE);
        if (currentPage < pageCount - 1) { currentPage++; refreshTable(); }
    }

    @FXML
    private void handleNouvelleConsultation() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/consultation_form.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Nouvelle Consultation");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                App.class.getResource("/css/style.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();

            loadConsultations();
        } catch (IOException e) {
            AlertUtil.erreurDetail("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }

    private void handleVoirDetails(Consultation c) {
        AlertUtil.info("Détails de la consultation N°" + c.getNumConsultation(),
            "Date       : " + (c.getDateConsult() != null ? c.getDateConsult() : "—") + "\n"
          + "Patient    : " + nvl(c.getNomAssure()) + "\n"
          + "Motif      : " + nvl(c.getMotif()) + "\n"
          + "Diagnostic : " + nvl(c.getDiagnostic()));
    }

    private void handleSupprimer(Consultation c) {
        if (!AlertUtil.confirmer("Supprimer la consultation",
            "Voulez-vous vraiment supprimer la consultation N°" + c.getNumConsultation()
          + " du " + c.getDateConsult() + " ?\n\nCette action est irréversible.")) return;

        new Thread(() -> {
            try {
                consultationService.delete(c.getNumConsultation());
                Platform.runLater(() -> {
                    AlertUtil.info("Supprimé", "La consultation a été supprimée avec succès.");
                    loadConsultations();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de supprimer la consultation", e));
            }
        }).start();
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
