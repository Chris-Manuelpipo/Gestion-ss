package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.model.Assure;
import com.securitesociale.service.AssureService;
import com.securitesociale.util.AlertUtil;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AssuresController {

    @FXML private Label statTotal;
    @FXML private Label statHommes;
    @FXML private Label statFemmes;

    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML private TableView<Assure> tableView;
    @FXML private TableColumn<Assure, String> colNum;
    @FXML private TableColumn<Assure, String> colNom;
    @FXML private TableColumn<Assure, String> colEmail;
    @FXML private TableColumn<Assure, String> colSexe;
    @FXML private TableColumn<Assure, String> colDateNaiss;
    @FXML private TableColumn<Assure, String> colCompte;
    @FXML private TableColumn<Assure, String> colMedecin;
    @FXML private TableColumn<Assure, Void>   colActions;

    @FXML private Button btnPrevPage;
    @FXML private Label  pageInfoLabel;
    @FXML private Button btnNextPage;

    private final AssureService assureService = new AssureService();

    private ObservableList<Assure> allAssures;
    private FilteredList<Assure>   filteredAssures;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadAssures();
    }

    private void setupColumns() {
        colNum.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getNumAssure())));

        colNom.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getNomComplet()));

        colEmail.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getEmail())));

        colSexe.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getSexe())));

        colDateNaiss.setCellValueFactory(d -> {
            var dn = d.getValue().getDateNaissance();
            return new SimpleStringProperty(dn != null ? dn.format(FR_DATE) : "—");
        });

        colCompte.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getNumCompteBancaire())));

        colMedecin.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getNomMedecinTraitant())));

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

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (filteredAssures == null) return;
            String lower = val.toLowerCase().trim();
            filteredAssures.setPredicate(a -> {
                if (lower.isEmpty()) return true;
                return a.getNom().toLowerCase().contains(lower)
                    || a.getPrenom().toLowerCase().contains(lower)
                    || (a.getEmail() != null && a.getEmail().toLowerCase().contains(lower));
            });
            currentPage = 0;
            refreshTable();
        });
    }

    private void loadAssures() {
        countLabel.setText("Chargement…");
        new Thread(() -> {
            try {
                List<Assure> assures = assureService.findAll();
                long hommes = assures.stream().filter(a -> "Masculin".equals(a.getSexe())).count();
                long femmes = assures.stream().filter(a -> "Féminin".equals(a.getSexe())).count();
                Platform.runLater(() -> {
                    statTotal.setText(String.valueOf(assures.size()));
                    statHommes.setText(String.valueOf(hommes));
                    statFemmes.setText(String.valueOf(femmes));

                    allAssures      = FXCollections.observableArrayList(assures);
                    filteredAssures = new FilteredList<>(allAssures, a -> true);
                    currentPage     = 0;
                    refreshTable();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de charger les assurés", e));
            }
        }).start();
    }

    private void refreshTable() {
        if (filteredAssures == null) return;

        int total     = filteredAssures.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage   = Math.min(currentPage, pageCount - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tableView.setItems(FXCollections.observableArrayList(
            total == 0 ? List.of() : filteredAssures.subList(from, to)));

        countLabel.setText(total + " assuré" + (total > 1 ? "s" : ""));
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
        int pageCount = (int) Math.ceil((double) filteredAssures.size() / PAGE_SIZE);
        if (currentPage < pageCount - 1) { currentPage++; refreshTable(); }
    }

    @FXML
    private void handleNouvelAssure() {
        openForm(null);
    }

    private void openForm(Assure assure) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/assure_form.fxml"));
            Parent root = loader.load();

            AssureFormController ctrl = loader.getController();
            if (assure != null) ctrl.setAssure(assure);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(assure == null ? "Nouvel assuré" : "Modifier l'assuré");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                App.class.getResource("/css/style.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();

            loadAssures();
        } catch (IOException e) {
            AlertUtil.erreurDetail("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }

    private void handleSupprimer(Assure a) {
        if (!AlertUtil.confirmer("Supprimer l'assuré",
            "Voulez-vous vraiment supprimer " + a.getNomComplet() + " ?\n\n"
          + "Cette action est irréversible.")) return;

        new Thread(() -> {
            try {
                assureService.delete(a.getNumAssure(), a.getNomComplet());
                Platform.runLater(() -> {
                    AlertUtil.info("Supprimé", a.getNomComplet() + " a été supprimé avec succès.");
                    loadAssures();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de supprimer l'assuré", e));
            }
        }).start();
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
