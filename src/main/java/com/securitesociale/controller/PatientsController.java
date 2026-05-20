package com.securitesociale.controller;

import com.securitesociale.model.Assure;
import com.securitesociale.model.Consultation;
import com.securitesociale.service.AssureService;
import com.securitesociale.service.ConsultationService;
import com.securitesociale.util.AlertUtil;
import com.securitesociale.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PatientsController {

    @FXML private Label statTotal;

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<Assure> tableView;
    @FXML private TableColumn<Assure, String> colNum;
    @FXML private TableColumn<Assure, String> colNom;
    @FXML private TableColumn<Assure, String> colEmail;
    @FXML private TableColumn<Assure, String> colSexe;
    @FXML private TableColumn<Assure, String> colDateNaiss;
    @FXML private TableColumn<Assure, String> colCompte;

    @FXML private Button btnPrevPage;
    @FXML private Label  pageInfoLabel;
    @FXML private Button btnNextPage;

    private final AssureService       assureService       = new AssureService();
    private final ConsultationService consultationService = new ConsultationService();

    private ObservableList<Assure> allPatients;
    private FilteredList<Assure>   filteredPatients;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadPatients();
    }

    private void setupColumns() {
        colNum.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getNumAssure())));

        colNom.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getNomComplet()));

        colEmail.setCellValueFactory(d -> {
            String e = d.getValue().getEmail();
            return new SimpleStringProperty(e != null ? e : "—");
        });

        colSexe.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getSexe())));

        colDateNaiss.setCellValueFactory(d -> {
            Assure a = d.getValue();
            if (a.getDateNaissance() != null)
                return new SimpleStringProperty(a.getDateNaissance().format(FR_DATE));
            return new SimpleStringProperty("—");
        });

        colCompte.setCellValueFactory(d -> {
            String c = d.getValue().getNumCompteBancaire();
            return new SimpleStringProperty(c != null ? c : "—");
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (filteredPatients == null) return;
            String lower = val.toLowerCase().trim();
            filteredPatients.setPredicate(p -> {
                if (lower.isEmpty()) return true;
                return p.getNom().toLowerCase().contains(lower)
                    || p.getPrenom().toLowerCase().contains(lower)
                    || (p.getEmail() != null && p.getEmail().toLowerCase().contains(lower));
            });
            currentPage = 0;
            refreshTable();
        });
    }

    private void loadPatients() {
        countLabel.setText("Chargement…");
        new Thread(() -> {
            try {
                int numMedecin = SessionManager.getInstance().getMedecinConnecte().getNumMedecin();

                List<Assure> assuresTraitant = assureService.findAll().stream()
                    .filter(a -> a.getNumMedecinTraitant() == numMedecin)
                    .collect(Collectors.toList());

                List<Consultation> consultations = consultationService.findByMedecin(numMedecin);
                Set<Integer> assureIds = new HashSet<>();
                for (Assure a : assuresTraitant) assureIds.add(a.getNumAssure());

                List<Assure> assuresConsultes = consultations.stream()
                    .map(c -> assureService.findAll().stream()
                        .filter(a -> a.getNumAssure() == c.getNumAssure())
                        .findFirst().orElse(null))
                    .filter(a -> a != null && assureIds.add(a.getNumAssure()))
                    .collect(Collectors.toList());

                assuresTraitant.addAll(assuresConsultes);

                Platform.runLater(() -> {
                    statTotal.setText(String.valueOf(assuresTraitant.size()));

                    allPatients      = FXCollections.observableArrayList(assuresTraitant);
                    filteredPatients = new FilteredList<>(allPatients, p -> true);
                    currentPage      = 0;
                    refreshTable();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de charger les patients", e));
            }
        }).start();
    }

    private void refreshTable() {
        if (filteredPatients == null) return;

        int total     = filteredPatients.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage   = Math.min(currentPage, pageCount - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tableView.setItems(FXCollections.observableArrayList(
            total == 0 ? List.of() : filteredPatients.subList(from, to)));

        countLabel.setText(total + " patient" + (total > 1 ? "s" : ""));
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
        int pageCount = (int) Math.ceil((double) filteredPatients.size() / PAGE_SIZE);
        if (currentPage < pageCount - 1) { currentPage++; refreshTable(); }
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
