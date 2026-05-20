package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.dao.LogDAO;
import com.securitesociale.model.Consultation;
import com.securitesociale.model.Prescription;
import com.securitesociale.service.ConsultationService;
import com.securitesociale.service.PrescriptionService;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PrescriptionsController {

    @FXML private Label statTotal;

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<Prescription> tableView;
    @FXML private TableColumn<Prescription, String> colNum;
    @FXML private TableColumn<Prescription, String> colDate;
    @FXML private TableColumn<Prescription, String> colType;
    @FXML private TableColumn<Prescription, String> colContenu;
    @FXML private TableColumn<Prescription, String> colConsultation;
    @FXML private TableColumn<Prescription, String> colPatient;
    @FXML private TableColumn<Prescription, Void>   colActions;

    @FXML private Button btnPrevPage;
    @FXML private Label  pageInfoLabel;
    @FXML private Button btnNextPage;
    @FXML private Button btnNewPrescription;

    private final PrescriptionService  prescriptionService  = new PrescriptionService();
    private final ConsultationService  consultationService  = new ConsultationService();

    private ObservableList<Prescription> allPrescriptions;
    private FilteredList<Prescription>   filteredPrescriptions;
    private Map<Integer, String>         consultationPatientMap = new HashMap<>();

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadPrescriptions();
    }

    private void setupColumns() {
        colNum.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getNumPrescription())));

        colDate.setCellValueFactory(d -> {
            Prescription p = d.getValue();
            if (p.getDatePrescription() != null)
                return new SimpleStringProperty(p.getDatePrescription().format(FR_DATE));
            return new SimpleStringProperty("—");
        });

        colType.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getType()));

        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(formatType(item));
                if ("MEDICAMENT".equals(item))
                    badge.getStyleClass().add("badge-green");
                else
                    badge.getStyleClass().add("badge-blue");
                setGraphic(badge);
                setText(null);
            }
        });

        colContenu.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getContenu())));

        colConsultation.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getNumConsultation())));

        colPatient.setCellValueFactory(d -> {
            Prescription p = d.getValue();
            String nom = consultationPatientMap.get(p.getNumConsultation());
            return new SimpleStringProperty(nom != null ? nom : "—");
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final HBox container = new HBox(6);
            private final Button btnSuppr = new Button("🗑 Supprimer");
            {
                btnSuppr.getStyleClass().add("btn-danger");
                btnSuppr.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-pref-height: 30px;");
                btnSuppr.setOnAction(e -> handleSupprimer(getTableView().getItems().get(getIndex())));
                container.getChildren().add(btnSuppr);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (filteredPrescriptions == null) return;
            String lower = val.toLowerCase().trim();
            filteredPrescriptions.setPredicate(p -> {
                if (lower.isEmpty()) return true;
                String patient = consultationPatientMap.getOrDefault(p.getNumConsultation(), "");
                return (p.getType() != null && p.getType().toLowerCase().contains(lower))
                    || (p.getContenu() != null && p.getContenu().toLowerCase().contains(lower))
                    || patient.toLowerCase().contains(lower);
            });
            currentPage = 0;
            refreshTable();
        });
    }

    private void loadPrescriptions() {
        countLabel.setText("Chargement…");
        new Thread(() -> {
            try {
                List<Consultation> consultations;
                if (SessionManager.getInstance().isMedecin()) {
                    int numMedecin = SessionManager.getInstance().getMedecinConnecte().getNumMedecin();
                    consultations = consultationService.findByMedecin(numMedecin);
                } else {
                    consultations = consultationService.findAll();
                }

                consultationPatientMap = consultations.stream()
                    .collect(Collectors.toMap(
                        Consultation::getNumConsultation,
                        c -> c.getNomAssure() != null ? c.getNomAssure() : "Assuré #" + c.getNumAssure(),
                        (a, b) -> a
                    ));

                Set<Integer> consultationIds = consultations.stream()
                    .map(Consultation::getNumConsultation)
                    .collect(Collectors.toSet());

                List<Prescription> all = prescriptionService.findAll();
                List<Prescription> filtered = all.stream()
                    .filter(p -> consultationIds.contains(p.getNumConsultation()))
                    .collect(Collectors.toList());

                Platform.runLater(() -> {
                    statTotal.setText(String.valueOf(filtered.size()));

                    allPrescriptions      = FXCollections.observableArrayList(filtered);
                    filteredPrescriptions = new FilteredList<>(allPrescriptions, p -> true);
                    currentPage           = 0;
                    refreshTable();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de charger les prescriptions", e));
            }
        }).start();
    }

    private void refreshTable() {
        if (filteredPrescriptions == null) return;

        int total     = filteredPrescriptions.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage   = Math.min(currentPage, pageCount - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tableView.setItems(FXCollections.observableArrayList(
            total == 0 ? List.of() : filteredPrescriptions.subList(from, to)));

        countLabel.setText(total + " prescription" + (total > 1 ? "s" : ""));
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
        int pageCount = (int) Math.ceil((double) filteredPrescriptions.size() / PAGE_SIZE);
        if (currentPage < pageCount - 1) { currentPage++; refreshTable(); }
    }

    @FXML
    private void handleNewPrescription() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/prescription_form.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Nouvelle Prescription");
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

    private void handleSupprimer(Prescription p) {
        if (!AlertUtil.confirmer("Supprimer la prescription",
            "Voulez-vous vraiment supprimer la prescription N°" + p.getNumPrescription() + " ?\n\n"
          + "Cette action est irréversible.")) return;

        new Thread(() -> {
            try {
                prescriptionService.delete(p.getNumPrescription());
                LogDAO.insert(
                    SessionManager.getInstance().getLoginUtilisateur(),
                    "DELETE_PRESCRIPTION",
                    "Prescription supprimée N°" + p.getNumPrescription()
                        + " | type: " + p.getType()
                        + " | consultation: " + p.getNumConsultation()
                );
                Platform.runLater(() -> {
                    AlertUtil.info("Supprimée", "La prescription a été supprimée avec succès.");
                    loadPrescriptions();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de supprimer la prescription", e));
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        loadPrescriptions();
    }

    private static String formatType(String type) {
        if ("MEDICAMENT".equals(type)) return "Médicament";
        if ("CONSULTATION_SPECIALISTE".equals(type)) return "Consultation Spécialiste";
        return type;
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
