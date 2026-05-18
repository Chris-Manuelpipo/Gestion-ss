package com.securitesociale;

import com.securitesociale.config.DatabaseManager;
import com.securitesociale.dao.AgentDAO;
import com.securitesociale.util.PasswordUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        initializeDatabase();

        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(App.class.getResource("/css/style.css").toExternalForm());

        stage.setTitle("Gestion Sécurité Sociale — ENSPY");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private void initializeDatabase() {
        try {
            DatabaseManager.initSchema();

            AgentDAO agentDAO = new AgentDAO();
            if (!agentDAO.exists("admin")) {
                agentDAO.create("admin", PasswordUtil.hash("Admin@2026"));
                System.out.println("[Init] Compte agent SS créé");
                System.out.println("[Init] Login : admin   |   Mot de passe : Admin@2026");
            }
        } catch (Exception e) {
            System.err.println("[Erreur] Initialisation base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        DatabaseManager.close();
    }

    public static void main(String[] args) {
        launch();
    }
}
