package com.securitesociale.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class AlertUtil {

    public static void info(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void erreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(480);
        alert.showAndWait();
    }

    public static void erreurDetail(String titre, String message, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);

        VBox content = new VBox(10);
        content.getChildren().add(new Label(message));

        if (ex != null) {
            TextArea textArea = new TextArea(getStackTrace(ex));
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(600);
            textArea.setPrefHeight(300);
            textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
            content.getChildren().add(textArea);
        }

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(640);
        alert.getDialogPane().setPrefHeight(440);
        alert.showAndWait();
    }

    public static void avertissement(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean confirmer(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static String getStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
