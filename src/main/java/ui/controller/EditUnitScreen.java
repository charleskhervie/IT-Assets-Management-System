package ui.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import dao.handler.unitHandler;
import dao.impl.UnitDAOImpl;
import dao.intfc.UnitDAO;
import dao.model.Unit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.util.AlertUtil;
/**
 * Controller for the Edit Unit Screen.
 * 
 * Manages the modification of specific hardware unit instances within the inventory. 
 * This controller facilitates data persistence for unit-level attributes such as 
 * serial numbers and personnel assignments while maintaining immutable audit data.
 * 
 * - Handles the injection and visualization of existing {@link Unit} data into 
 *   form fields, formatting timestamps via {@link DateTimeFormatter}.
 * - Enforces data integrity by preventing the editing of read-only fields like 
 *   creation timestamps.
 * - Performs comprehensive input validation to ensure mandatory fields are 
 *   populated and numerical constraints are respected.
 * - Synchronizes state updates with the database through the {@link unitHandler} 
 *   and {@link UnitDAO} persistence layer.
 * - Manages modal lifecycle by providing safe window closure mechanisms for 
 *   both successful save operations and user cancellations.
 */
public class EditUnitScreen implements Initializable {

    @FXML private TextField equipmentIdField;
    @FXML private TextField serialNumberField;
    @FXML private TextField assignedToField;
    @FXML private TextField createdAtField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_STATUS = "available";

    private final unitHandler handler = new unitHandler();
    private final UnitDAO dao = new UnitDAOImpl();
    private Unit unit;

    public void setUnit(Unit unit) {
        this.unit = unit;
        if (unit == null) return;
        equipmentIdField.setText(String.valueOf(unit.getEquipmentId()));
        serialNumberField.setText(unit.getSerialNumber());
        assignedToField.setText(unit.getAssignedTo() != null ? String.valueOf(unit.getAssignedTo()) : "");
        createdAtField.setText(unit.getCreatedAt().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createdAtField.setEditable(false);
    }


    @FXML
    private void handleSave() {
        if (!isInputValid()) return;

        try {
            Unit updated = buildUpdatedUnit();
            String error = handler.updateUnit(dao, updated);

            if (error != null) {
                AlertUtil.showError("Update Failed", error);
                return;
            }

            closeWindow();

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Equipment ID must be a number.");
        } catch (Exception e) {
            AlertUtil.showError("Unexpected Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private Unit buildUpdatedUnit() {
        int equipmentId = Integer.parseInt(equipmentIdField.getText().trim());
        String serial = serialNumberField.getText().trim();
        Integer assignedTo = parseNullableInt(assignedToField.getText().trim());

        return new Unit(
            unit.getUnitId(),
            equipmentId,
            serial,
            DEFAULT_STATUS,
            unit.getAddedBy(),
            unit.getCreatedAt(),
            assignedTo
        );
    }

    private boolean isInputValid() {
        String equipmentId = equipmentIdField.getText().trim();
        String serial = serialNumberField.getText().trim();

        if (equipmentId.isEmpty() || serial.isEmpty()) {
            AlertUtil.showError("Missing Fields", "Equipment ID and Serial Number are required.");
            return false;
        }

        if (!isNumeric(equipmentId)) {
            AlertUtil.showError("Invalid Input", "Equipment ID must be a number.");
            return false;
        }

        return true;
    }

    private Integer parseNullableInt(String value) {
        return value.isEmpty() ? null : Integer.valueOf(value);
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}