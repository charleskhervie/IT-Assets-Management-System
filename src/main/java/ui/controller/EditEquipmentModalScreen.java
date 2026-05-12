package ui.controller;

import dao.handler.EquipmentHandler;
import dao.impl.CategoryDAOImpl;
import dao.impl.EquipmentDAOImpl;
import dao.intfc.CategoryDAO;
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
 * Controller for the Edit Equipment Modal Screen.
 * 
 * Manages the interface for updating existing equipment specifications and 
 * metadata. This controller acts as a specialized dialogue for refining asset 
 * details, ensuring that modifications adhere to database constraints and 
 * relational integrity.
 * 
 * - Populates form fields with current {@link Equipment} data, including 
 *   descriptive text, branding, and technical specifications.
 * - Implements strict input validation to ensure mandatory fields such as 
 *   equipment name and category identifiers are correctly formatted.
 * - Enforces referential integrity by verifying the existence of the 
 *   provided Category ID via {@link CategoryDAO} before committing updates.
 * - Orchestrates data persistence by delegating the update logic to 
 *   {@link EquipmentHandler} and handling potential SQL exceptions.
 * - Controls the modal lifecycle by providing event-driven methods to 
 *   dismiss the window upon successful completion or user cancellation.
 */
public class EditEquipmentModalScreen {

    @FXML private TextField equipmentNameField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField categoryIdField;
    @FXML private TextArea specificationsArea;

    private final EquipmentHandler handler = new EquipmentHandler();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final CategoryDAO categoryDAO = new CategoryDAOImpl();
    private Equipment equipment;

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
        equipmentNameField.setText(equipment.getEquipmentName());
        brandField.setText(equipment.getBrand());
        modelField.setText(equipment.getModel());
        categoryIdField.setText(String.valueOf(equipment.getCategoryId()));
        specificationsArea.setText(equipment.getSpecifications());
    }

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
            if (categoryId <= 0) {
                AlertUtil.showError("Validation Error", "Category ID must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Category ID must be a valid number.");
            return;
        }

        try {
            boolean categoryExists = !categoryDAO.findWithAttribute("category_id", String.valueOf(categoryId)).isEmpty();
            if (!categoryExists) {
                AlertUtil.showError("Validation Error", "Category ID " + categoryId + " does not exist. Please enter a valid category.");
                return;
            }
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Could not verify category ID: " + e.getMessage());
            return;
        }

        equipment.setEquipmentName(name);
        equipment.setBrand(brand);
        equipment.setModel(model);
        equipment.setSpecifications(specifications);
        equipment.setCategoryId(categoryId);

        String error = handler.updateEquipment(equipmentDAO, equipment);
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