package ui.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;

import dao.dao_util.CredentialManager;
import dao.handler.unitHandler;
import dao.impl.UnitDAOImpl;
import dao.intfc.UnitDAO;
import dao.model.Unit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
/**
 * Controller for the Add Unit Screen.
 * 
 * Facilitates the registration of new hardware assets into the inventory system. 
 * This controller ensures that every new unit is initialized with correct 
 * ownership data and audit timestamps before being persisted.
 * 
 * - Automates administrative tracking by retrieving the current user's identity 
 *   via {@link CredentialManager} and populating read-only audit fields.
 * - Enforces robust data entry standards through real-time validation of 
 *   mandatory fields and numerical constraints for equipment identifiers.
 * - Bridges the UI and database layers by transforming form input into valid 
 *   {@link Unit} objects for processing by {@link unitHandler}.
 * - Implements secure session fallback mechanisms to ensure system stability 
 *   even when local credential properties are inaccessible.
 * - Provides immediate diagnostic feedback using localized alert dialogs to 
 *   inform the user of validation errors or successful record creation.
 */
public class AddUnitScreen implements Initializable {


    @FXML private TextField equipmentIdField;
    @FXML private TextField serialNumberField;
    @FXML private TextField addedByField;
    @FXML private TextField createdAtField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;


    private static final String DEFAULT_STATUS = "Available";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String FALLBACK_USER_ID = "1";


    private final CredentialManager credentialManager = new CredentialManager();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initReadOnlyFields();
    }


    private void initReadOnlyFields() {
        createdAtField.setText(currentTimestamp());
        createdAtField.setEditable(false);

        addedByField.setText(getCurrentUserId());
        addedByField.setEditable(false);
    }


    @FXML
    private void handleSave() {
        System.out.println("Save button clicked.");
        if (!isInputValid()){
            System.out.println("Input is not valid.");
            return;
        }

        try {
            Unit unit = buildUnitFromFields();
            System.out.println("Unit built: " + unit.getEquipmentId() + " " + unit.getSerialNumber());
            unitHandler handler = new unitHandler();
            UnitDAO dao = new UnitDAOImpl();

            String result = handler.addUnit(dao, unit);
            System.out.println("Handler result: " + result);

            if (result != null) {
                showError("Save Failed", result);
                return;
            }

            closeWindow();

        } catch (NumberFormatException e) {
            System.out.println("Number format error: " + e.getMessage());
            showError("Invalid Input", "Equipment ID must be a number.");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showError("Unexpected Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }


    private Unit buildUnitFromFields() {
        int equipmentId = Integer.parseInt(equipmentIdField.getText().trim());
        String serial = serialNumberField.getText().trim();
        int addedBy = Integer.parseInt(addedByField.getText().trim());

        return new Unit(0, equipmentId, serial, DEFAULT_STATUS, addedBy, LocalDateTime.now(), null);
    }


    private boolean isInputValid() {
        String equipmentId = equipmentIdField.getText().trim();
        String serial = serialNumberField.getText().trim();
        String addedBy = addedByField.getText().trim();

        if (equipmentId.isEmpty() || serial.isEmpty() || addedBy.isEmpty()) {
            showError("Missing Fields", "Equipment ID, Serial Number, and Added By are required.");
            return false;
        }

        if (!isNumeric(equipmentId)) {
            showError("Invalid Input", "Equipment ID must be a number.");
            return false;
        }

        return true;
    }


    private String currentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private String getCurrentUserId() {
        if (!credentialManager.exists()) return FALLBACK_USER_ID;
        try {
            Properties props = credentialManager.load();
            String userId    = props.getProperty("emp_id");
            return (userId == null || userId.isBlank()) ? FALLBACK_USER_ID : userId;
        } catch (IOException e) {
            return FALLBACK_USER_ID;
        }
    }
}