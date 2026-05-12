package ui.controller;

import dao.handler.EquipmentHandler;
import dao.impl.EquipmentDAOImpl;
import dao.intfc.EquipmentDAO;
import dao.model.Equipment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.util.AlertUtil;
/**
 * Controller for the Add Equipment Modal Screen.
 * 
 * Facilitates the entry of new equipment types into the organizational asset catalog. 
 * This controller serves as the primary interface for defining high-level hardware 
 * templates, including technical specifications and categorization.
 * - Manages the instantiation of new {@link Equipment} objects, mapping UI 
 *   text fields to the underlying data model.
 * - Interfaces with the {@link EquipmentHandler} and {@link EquipmentDAO} to 
 *   persist new records while providing immediate error feedback via {@link AlertUtil}.
 */
public class AddEquipmentModalScreen {

    @FXML private TextField equipmentNameField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField categoryIdField;
    @FXML private TextArea specificationsArea;

    private final EquipmentHandler handler = new EquipmentHandler();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();

    @FXML
    private void handleSave(ActionEvent event) {
        String name = equipmentNameField.getText().trim();
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String specifications = specificationsArea.getText().trim();
        String categoryIdText = categoryIdField.getText().trim();

        if (name.isEmpty()) {
            AlertUtil.showError("Validation Error", "Equipment name cannot be empty.");
            return;
        }
        if (categoryIdText.isEmpty()) {
            AlertUtil.showError("Validation Error", "Category ID cannot be empty.");
            return;
        }

        int categoryId;
        try {
            categoryId = Integer.parseInt(categoryIdText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Category ID must be a number.");
            return;
        }

        Equipment equipment = new Equipment(0, name, brand, model, specifications, categoryId);
        String error = handler.addEquipment(equipmentDAO, equipment);        
        if (error != null) {
            AlertUtil.showError("Save Failed", error);
            return;
        }
        close(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        close(event);
    }

    private void close(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}