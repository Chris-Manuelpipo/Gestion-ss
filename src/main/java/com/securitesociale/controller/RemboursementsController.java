package com.securitesociale.controller;

import com.securitesociale.App;
import com.securitesociale.model.Remboursement;
import com.securitesociale.service.RemboursementService;
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

public class RemboursementsController {

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label statTotal;
    @FXML private Label statEnAttente;
    @FXML private Label statEffectues;

    @FXML private ComboBox<String> filtreStatut;
    @FXML private Label countLabel;

    @FXML private TableView<Remboursement> tableView;
    @FXML private TableColumn<Remboursement, String> colNum;
    @FXML private TableColumn<Remboursement, String> colNature;
    @FXML private TableColumn<Remboursement, String> colTaux;
    @FXML private TableColumn<Remboursement, String> colMontant;
    @FXML private TableColumn<Remboursement, String> colMode;
    @FXML private TableColumn<Remboursement, String> colStatut;
    @FXML private TableColumn<Remboursement, String> colDate;
    @FXML private TableColumn<Remboursement, String> colAgent;
    @FXML private TableColumn<Remboursement, Void> colActions;

    @FXML private Button btnPrevPage;
    @FXML private Label pageInfoLabel;
    @FXML private Button btnNextPage;
    @FXML private Button btnAjouter;

    private final RemboursementService remboursementService = new RemboursementService();

    private ObservableList<Remboursement> allRemboursements;
    private FilteredList<Remboursement> filteredRemboursements;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    @FXML
    public void initialize() {
        setupFilter();
        setupColumns();
        loadData();
    }

    private void setupFilter() {
        filtreStatut.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE", "EFFECTUE", "REJETEE"));
        filtreStatut.setValue("Tous");
        filtreStatut.valueProperty().addListener((obs, old, val) -> {
            if (filteredRemboursements == null) return;
            if ("Tous".equals(val)) {
                filteredRemboursements.setPredicate(r -> true);
            } else {
                filteredRemboursements.setPredicate(r -> val.equals(r.getStatut()));
            }
            currentPage = 0;
            refreshTable();
        });
    }

    private void setupColumns() {
        colNum.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getNumRemboursement())));

        colNature.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getNature()));

        colTaux.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTaux() != null
                ? d.getValue().getTaux().stripTrailingZeros().toPlainString() + " %"
                : "—"));

        colMontant.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getMontant() != null
                ? String.format("%,.0f", d.getValue().getMontant()) + " FCFA"
                : "—"));

        colMode.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getModeReglement() != null
                ? d.getValue().getModeReglement() : "—"));

        colStatut.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getStatut()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item.replace("_", " "));
                switch (item) {
                    case "EN_ATTENTE" -> badge.getStyleClass().add("badge-orange");
                    case "EFFECTUE"   -> badge.getStyleClass().add("badge-green");
                    case "REJETEE"    -> badge.getStyleClass().add("badge-red");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        colDate.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getDateRemboursement() != null
                ? d.getValue().getDateRemboursement().format(FR_DATE)
                : "—"));

        colAgent.setCellValueFactory(d ->
            new SimpleStringProperty(nvl(d.getValue().getAgentLogin())));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnValider = new Button("Valider");
            private final Button btnRejeter = new Button("Rejeter");
            private final Button btnSuppr   = new Button("Supprimer");
            private final HBox box = new HBox(6, btnValider, btnRejeter, btnSuppr);

            {
                btnValider.getStyleClass().add("btn-success");
                btnValider.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-pref-height: 30px;");
                btnValider.setOnAction(e -> handleValider(getTableView().getItems().get(getIndex())));

                btnRejeter.getStyleClass().add("btn-warning");
                btnRejeter.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-pref-height: 30px;");
                btnRejeter.setOnAction(e -> handleRejeter(getTableView().getItems().get(getIndex())));

                btnSuppr.getStyleClass().add("btn-danger");
                btnSuppr.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-pref-height: 30px;");
                btnSuppr.setOnAction(e -> handleSupprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                String statut = getTableRow().getItem().getStatut();
                boolean enAttente = "EN_ATTENTE".equals(statut);
                btnValider.setVisible(enAttente);
                btnValider.setManaged(enAttente);
                btnRejeter.setVisible(enAttente);
                btnRejeter.setManaged(enAttente);
                setGraphic(box);
            }
        });
    }

    private void loadData() {
        countLabel.setText("Chargement…");
        new Thread(() -> {
            try {
                List<Remboursement> list = remboursementService.findAll();
                int total       = remboursementService.count();
                int enAttente   = remboursementService.countEnAttente();
                int effectues   = (int) list.stream().filter(r -> "EFFECTUE".equals(r.getStatut())).count();

                Platform.runLater(() -> {
                    statTotal.setText(String.valueOf(total));
                    statEnAttente.setText(String.valueOf(enAttente));
                    statEffectues.setText(String.valueOf(effectues));

                    allRemboursements      = FXCollections.observableArrayList(list);
                    filteredRemboursements = new FilteredList<>(allRemboursements, r -> true);
                    currentPage            = 0;
                    refreshTable();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur base de données",
                        "Impossible de charger les remboursements", e));
            }
        }).start();
    }

    private void refreshTable() {
        if (filteredRemboursements == null) return;

        int total     = filteredRemboursements.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage   = Math.min(currentPage, pageCount - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        tableView.setItems(FXCollections.observableArrayList(
            total == 0 ? List.of() : filteredRemboursements.subList(from, to)));

        countLabel.setText(total + " remboursement" + (total > 1 ? "s" : ""));
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
        int pageCount = (int) Math.ceil((double) filteredRemboursements.size() / PAGE_SIZE);
        if (currentPage < pageCount - 1) { currentPage++; refreshTable(); }
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/remboursement_form.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Nouveau Remboursement");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                App.class.getResource("/css/style.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();

            loadData();
        } catch (IOException e) {
            AlertUtil.erreurDetail("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }

    private void handleValider(Remboursement r) {
        if (!AlertUtil.confirmer("Valider le remboursement",
            "Voulez-vous valider le remboursement N°" + r.getNumRemboursement()
                + " (" + r.getNature() + ") ?\n\n"
                + "Cette action est irréversible.")) return;

        new Thread(() -> {
            try {
                remboursementService.valider(r.getNumRemboursement());
                Platform.runLater(() -> {
                    AlertUtil.info("Validé", "Le remboursement N°" + r.getNumRemboursement() + " a été validé.");
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur", "Impossible de valider le remboursement", e));
            }
        }).start();
    }

    private void handleRejeter(Remboursement r) {
        if (!AlertUtil.confirmer("Rejeter le remboursement",
            "Voulez-vous rejeter le remboursement N°" + r.getNumRemboursement()
                + " (" + r.getNature() + ") ?\n\n"
                + "Cette action est irréversible.")) return;

        new Thread(() -> {
            try {
                remboursementService.rejeter(r.getNumRemboursement());
                Platform.runLater(() -> {
                    AlertUtil.info("Rejeté", "Le remboursement N°" + r.getNumRemboursement() + " a été rejeté.");
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur", "Impossible de rejeter le remboursement", e));
            }
        }).start();
    }

    private void handleSupprimer(Remboursement r) {
        if (!AlertUtil.confirmer("Supprimer le remboursement",
            "Voulez-vous vraiment supprimer le remboursement N°" + r.getNumRemboursement()
                + " (" + r.getNature() + ") ?\n\n"
                + "Cette action est irréversible.")) return;

        new Thread(() -> {
            try {
                remboursementService.delete(r.getNumRemboursement());
                Platform.runLater(() -> {
                    AlertUtil.info("Supprimé", "Le remboursement N°" + r.getNumRemboursement() + " a été supprimé.");
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    AlertUtil.erreurDetail("Erreur", "Impossible de supprimer le remboursement", e));
            }
        }).start();
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
