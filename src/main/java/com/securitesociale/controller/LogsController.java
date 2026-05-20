package com.securitesociale.controller;

import com.securitesociale.model.LogEntry;
import com.securitesociale.service.LogService;
import com.securitesociale.util.AlertUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogsController {

    @FXML private Label statTotal;

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<LogEntry> tableView;
    @FXML private TableColumn<LogEntry, String> colId;
    @FXML private TableColumn<LogEntry, String> colDate;
    @FXML private TableColumn<LogEntry, String> colUtilisateur;
    @FXML private TableColumn<LogEntry, String> colAction;
    @FXML private TableColumn<LogEntry, String> colDetails;

    @FXML private Button btnPrevPage;
    @FXML private Label  pageInfoLabel;
    @FXML private Button btnNextPage;

    private final LogService logService = new LogService();

    private ObservableList<LogEntry> allLogs;
    private FilteredList<LogEntry>   filteredLogs;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadLogs();
    }

    private void setupColumns() {
        colId.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        colDate.setCellValueFactory(d -> {
            LogEntry e = d.getValue();
            if (e.getCreatedAt() != null)
                return new SimpleStringProperty(e.getCreatedAt().format(DT_FMT));
            return new SimpleStringProperty("—");
        });

        colUtilisateur.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getUtilisateur())));

        colAction.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getAction())));

        colDetails.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getDetails())));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (filteredLogs == null) return;
            String lower = val.toLowerCase().trim();
            filteredLogs.setPredicate(e -> {
                if (lower.isEmpty()) return true;
                return (e.getUtilisateur() != null && e.getUtilisateur().toLowerCase().contains(lower))
                    || (e.getAction() != null && e.getAction().toLowerCase().contains(lower))
                    || (e.getDetails() != null && e.getDetails().toLowerCase().contains(lower));
            });
            currentPage = 0;
            refreshTable();
        });
    }

    private void loadLogs() {
        countLabel.setText("Chargement…");
        new Thread(() -> {
            try {
                List<LogEntry> logs = logService.findAll();
                Platform.runLater(() -> {
                    statTotal.setText(String.valueOf(logs.size()));

                    allLogs     = FXCollections.observableArrayList(logs);
                    filteredLogs = new FilteredList<>(allLogs, e -> true);
                    currentPage  = 0;
                    refreshTable();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de charger le journal", e));
            }
        }).start();
    }

    private void refreshTable() {
        if (filteredLogs == null) return;

        int total     = filteredLogs.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage   = Math.min(currentPage, pageCount - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tableView.setItems(FXCollections.observableArrayList(
            total == 0 ? List.of() : filteredLogs.subList(from, to)));

        countLabel.setText(total + " entrée" + (total > 1 ? "s" : ""));
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
        int pageCount = (int) Math.ceil((double) filteredLogs.size() / PAGE_SIZE);
        if (currentPage < pageCount - 1) { currentPage++; refreshTable(); }
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
