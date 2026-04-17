package view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardScreen implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Dashboard is static for now.
    }

    @FXML
    private void handleUnits(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/unitsList.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load unitsList.fxml", exception);
        }
    }
}