package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.model.FeuilleMaladie;
import com.securitesociale.model.Prescription;
import com.securitesociale.service.FeuilleMaladieService;
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
import java.util.List;
import java.util.stream.Collectors;

public class FeuillesMaladieController {

    @FXML private Label statTotal;

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<FeuilleMaladie> tableView;
    @FXML private TableColumn<FeuilleMaladie, String> colNum;
    @FXML private TableColumn<FeuilleMaladie, String> colDate;
    @FXML private TableColumn<FeuilleMaladie, String> colPatient;
    @FXML private TableColumn<FeuilleMaladie, String> colMotif;
    @FXML private TableColumn<FeuilleMaladie, String> colStatut;
    @FXML private TableColumn<FeuilleMaladie, Void>   colActions;

    @FXML private Button btnPrevPage;
    @FXML private Label  pageInfoLabel;
    @FXML private Button btnNextPage;
    @FXML private Button btnNewFeuille;

    private final FeuilleMaladieService feuilleService = new FeuilleMaladieService();

    private ObservableList<FeuilleMaladie> allFeuilles;
    private FilteredList<FeuilleMaladie>   filteredFeuilles;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadFeuilles();
    }

    private void setupColumns() {
        colNum.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getNumFeuille())));

        colDate.setCellValueFactory(d -> {
            FeuilleMaladie f = d.getValue();
            if (f.getDateEmission() != null)
                return new SimpleStringProperty(f.getDateEmission().format(FR_DATE));
            return new SimpleStringProperty("—");
        });

        colPatient.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getNomAssure())));

        colMotif.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getMotif())));

        colStatut.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getStatut())));

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(formatStatut(item));
                if ("EN_ATTENTE".equals(item))
                    badge.getStyleClass().add("badge-yellow");
                else if ("VALIDEE".equals(item))
                    badge.getStyleClass().add("badge-green");
                else
                    badge.getStyleClass().add("badge-red");
                setGraphic(badge);
                setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir = new Button("👁 Voir");
            private final HBox pane = new HBox(8, btnVoir);
            {
                btnVoir.getStyleClass().add("btn-secondary");
                btnVoir.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-pref-height: 30px;");
                btnVoir.setOnAction(e -> handleVoir(getTableView().getItems().get(getIndex())));
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
            if (filteredFeuilles == null) return;
            String lower = val.toLowerCase().trim();
            filteredFeuilles.setPredicate(f -> {
                if (lower.isEmpty()) return true;
                return (f.getNomAssure() != null && f.getNomAssure().toLowerCase().contains(lower))
                    || (f.getMotif() != null && f.getMotif().toLowerCase().contains(lower))
                    || (f.getStatut() != null && f.getStatut().toLowerCase().contains(lower));
            });
            currentPage = 0;
            refreshTable();
        });
    }

    private void loadFeuilles() {
        if (!SessionManager.getInstance().isMedecin()) return;

        int numMedecin = SessionManager.getInstance().getMedecinConnecte().getNumMedecin();

        countLabel.setText("Chargement…");
        new Thread(() -> {
            try {
                List<FeuilleMaladie> feuilles = feuilleService.findByMedecin(numMedecin);

                Platform.runLater(() -> {
                    statTotal.setText(String.valueOf(feuilles.size()));

                    allFeuilles      = FXCollections.observableArrayList(feuilles);
                    filteredFeuilles = new FilteredList<>(allFeuilles, f -> true);
                    currentPage      = 0;
                    refreshTable();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de charger les feuilles de maladie", e));
            }
        }).start();
    }

    private void refreshTable() {
        if (filteredFeuilles == null) return;

        int total     = filteredFeuilles.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage   = Math.min(currentPage, pageCount - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tableView.setItems(FXCollections.observableArrayList(
            total == 0 ? List.of() : filteredFeuilles.subList(from, to)));

        countLabel.setText(total + " feuille" + (total > 1 ? "s" : ""));
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
        int pageCount = (int) Math.ceil((double) filteredFeuilles.size() / PAGE_SIZE);
        if (currentPage < pageCount - 1) { currentPage++; refreshTable(); }
    }

    @FXML
    private void handleNouvelleFeuille() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/feuille_maladie_form.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Nouvelle Feuille de Maladie");
            dialog.setMaximized(true);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                App.class.getResource("/css/style.css").toExternalForm());
            dialog.setScene(scene);
            dialog.showAndWait();

            loadFeuilles();
        } catch (IOException e) {
            AlertUtil.erreurDetail("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }

    private void handleVoir(FeuilleMaladie feuille) {
        StringBuilder sb = new StringBuilder();
        sb.append("N° Feuille   : ").append(feuille.getNumFeuille()).append("\n");
        sb.append("Date émission : ").append(feuille.getDateEmission() != null ? feuille.getDateEmission().format(FR_DATE) : "—").append("\n");
        sb.append("Patient      : ").append(nvl(feuille.getNomAssure())).append("\n");
        sb.append("Motif        : ").append(nvl(feuille.getMotif())).append("\n");
        sb.append("Statut       : ").append(formatStatut(feuille.getStatut())).append("\n\n");

        sb.append("--- Paramètres médicaux ---\n");
        sb.append("Poids        : ").append(feuille.getPoids() != null ? feuille.getPoids() + " kg" : "—").append("\n");
        sb.append("Taille       : ").append(feuille.getTaille() != null ? feuille.getTaille() + " m" : "—").append("\n");
        sb.append("Température  : ").append(feuille.getTemperature() != null ? feuille.getTemperature() + " °C" : "—").append("\n");
        sb.append("Tension      : ").append(nvl(feuille.getTensionArterielle())).append("\n");
        sb.append("Diagnostic   : ").append(nvl(feuille.getDiagnostic())).append("\n");
        sb.append("Antécédents  : ").append(nvl(feuille.getAntecedents())).append("\n");

        try {
            List<Prescription> prescriptions = feuilleService.findPrescriptions(feuille.getNumConsultation());
            if (!prescriptions.isEmpty()) {
                sb.append("\n--- Prescriptions ---\n");
                for (Prescription p : prescriptions) {
                    sb.append("• ").append("MEDICAMENT".equals(p.getType()) ? "Médicament" : "Consultation Spécialiste");
                    if (p.getNomMedicament() != null) sb.append(" : ").append(p.getNomMedicament());
                    if (p.getNomSpecialiste() != null) sb.append(" : Dr ").append(p.getNomSpecialiste());
                    sb.append("\n");
                }
            }
        } catch (Exception ignored) {}

        AlertUtil.info("Feuille de maladie N°" + feuille.getNumFeuille(), sb.toString());
    }

    @FXML
    private void handleRefresh() {
        loadFeuilles();
    }

    private static String formatStatut(String s) {
        if ("EN_ATTENTE".equals(s)) return "En attente";
        if ("VALIDEE".equals(s)) return "Validée";
        if ("REJETEE".equals(s)) return "Rejetée";
        return s != null ? s : "—";
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
