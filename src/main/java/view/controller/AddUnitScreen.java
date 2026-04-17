package view.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;

import dao.dao_util.CredentialManager;
import dao.impl.UnitDAOImpl;
import dao.model.Unit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddUnitScreen implements Initializable {

    @FXML
    private TextField equipmentIdField;

    @FXML
    private TextField serialNumberField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField addedByField;

    @FXML
    private TextField assignedToField;

    @FXML
    private TextField createdAtField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusComboBox.getItems().addAll("available", "checked-out", "maintenance");
        statusComboBox.setValue("available");
        createdAtField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        addedByField.setText(getCurrentUserId());
    }

    @FXML
    private void handleSave() {
        String equipmentText = equipmentIdField.getText().trim();
        String serialNumber = serialNumberField.getText().trim();
        String addedByText = addedByField.getText().trim();

        if (equipmentText.isEmpty() || serialNumber.isEmpty() || addedByText.isEmpty()) {
            return;
        }

        try {
            int equipmentId = Integer.parseInt(equipmentText);
            int addedBy = Integer.parseInt(addedByText);
            Integer assignedTo = assignedToField.getText().trim().isEmpty() ? null : Integer.valueOf(assignedToField.getText().trim());

            Unit unit = new Unit(
                0,
                equipmentId,
                serialNumber,
                statusComboBox.getValue(),
                addedBy,
                LocalDateTime.now(),
                assignedTo
            );

            new UnitDAOImpl().add(unit);
            closeWindow();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to save unit", exception);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private String getCurrentUserId() {
        CredentialManager credentialManager = new CredentialManager();
        if (!credentialManager.exists()) {
            return "1";
        }

        try {
            Properties properties = credentialManager.load();
            String userId = properties.getProperty("emp_id");
            return userId == null || userId.isBlank() ? "1" : userId;
        } catch (IOException exception) {
            return "1";
        }
    }
}