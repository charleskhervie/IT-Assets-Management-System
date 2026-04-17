package view.controller;

import java.net.URL;
import java.util.ResourceBundle;

import dao.impl.EquipmentDAOImpl;
import dao.model.Equipment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddEquipmentScreen implements Initializable {

    @FXML
    private TextField equipmentNameField;

    @FXML
    private TextField brandField;

    @FXML
    private TextField modelField;

    @FXML
    private TextField categoryIdField;

    @FXML
    private TextArea specificationsArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // No-op for now.
    }

    @FXML
    private void handleSave() {
        String equipmentName = equipmentNameField.getText().trim();
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String categoryText = categoryIdField.getText().trim();
        String specifications = specificationsArea.getText().trim();

        if (equipmentName.isEmpty() || brand.isEmpty() || model.isEmpty() || categoryText.isEmpty()) {
            return;
        }

        try {
            int categoryId = Integer.parseInt(categoryText);
            Equipment equipment = new Equipment(0, equipmentName, brand, model, specifications, categoryId);
            new EquipmentDAOImpl().add(equipment);
            closeWindow();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to save equipment", exception);
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
}