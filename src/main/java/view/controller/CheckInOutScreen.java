package view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import dao.dao_util.CredentialManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CheckInOutScreen implements Initializable {

    @FXML
    private ComboBox<String> actionComboBox;

    @FXML
    private TextField unitField;

    @FXML
    private TextField serialField;

    @FXML
    private TextField employeeField;

    @FXML
    private TextField processedByField;

    @FXML
    private DatePicker transactionDatePicker;

    @FXML
    private TextArea remarksArea;

    @FXML
    private Label statusLabel;

    @FXML
    private Button processButton;

    @FXML
    private Button backButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        actionComboBox.getItems().addAll("Check-out", "Check-in");
        actionComboBox.setValue("Check-out");
        loadProcessedBy();
        statusLabel.setText("Ready to process a check-out/check-in transaction.");
    }

    private void loadProcessedBy() {
        CredentialManager credentialManager = new CredentialManager();
        if (!credentialManager.exists()) {
            processedByField.setText("Unknown Admin");
            return;
        }

        try {
            Properties properties = credentialManager.load();
            String username = properties.getProperty("username", "Unknown Admin");
            processedByField.setText(username);
        } catch (IOException exception) {
            processedByField.setText("Unknown Admin");
        }
    }

    @FXML
    private void handleProcess() {
        statusLabel.setText("Transaction preview only for now.");
    }

    @FXML
    private void handleBack(ActionEvent event) {
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