package ui.controller;

import java.time.LocalDateTime;
import java.util.List;

import dao.handler.TransactionHandler;
import dao.impl.TransactionDAOImpl;
import dao.impl.UnitDAOImpl;
import dao.intfc.TransactionDAO;
import dao.intfc.UnitDAO;
import dao.model.Employee;
import dao.model.Transaction;
import dao.model.Unit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.util.AdminUtil;
import ui.util.AlertUtil;
import ui.util.SessionManager;
/**
 * Controller for the Check Out Screen.
 * 
 * Manages the workflow for requesting and processing hardware asset deployments. 
 * This controller handles the transition of unit states from "available" to 
 * "checked-out" or "pending" based on the user's authorization level.
 * 
 * - Automates field population by retrieving hardware details from the target 
 *   {@link Unit} and borrower information from the current {@link SessionManager}.
 * - Implements conditional business logic that differentiates between direct 
 *   admin checkouts and employee requests requiring administrative approval.
 * - Validates unit availability and serial number existence via {@link UnitDAO} 
 *   to prevent transaction conflicts or data errors.
 * - Records hardware deployments by generating new {@link Transaction} entries 
 *   through the {@link TransactionHandler} service layer.
 * - Provides immediate user feedback on the status of the request while ensuring 
 *   modal lifecycle management via controlled stage closures.
 */
public class CheckOutScreen {

    @FXML private TextField serialNoField;
    @FXML private TextField borrowerField;

    private final TransactionHandler handler = new TransactionHandler();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private final UnitDAO unitDAO = new UnitDAOImpl();
    public void setTargetUnit(Unit unit) {
        if (unit != null) {
            serialNoField.setText(unit.getSerialNumber());
            serialNoField.setEditable(false);
        }
        // Auto-fill borrower from session
        Employee loggedIn = SessionManager.getLoggedInEmployee();
        if (loggedIn != null) {
            borrowerField.setText(loggedIn.getFullName());
            borrowerField.setEditable(false);
        }
    }
    public void initialize() {
        Employee loggedIn = SessionManager.getLoggedInEmployee();
        if (loggedIn != null) {
            borrowerField.setText(loggedIn.getFullName());
            borrowerField.setEditable(false);
        }
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        String serialNo = serialNoField.getText().trim();

        if (serialNo.isEmpty()) {
            AlertUtil.showError("Validation Error", "Serial number cannot be empty.");
            return;
        }

        Employee loggedIn = SessionManager.getLoggedInEmployee();
        if (loggedIn == null) {
            AlertUtil.showError("Session Error", "No logged in user found.");
            return;
        }

        try {
            Unit unit = unitDAO.findBySerialExact(serialNo);
            if (unit == null) {
                AlertUtil.showError("Not Found", "No unit found with serial number: " + serialNo);
                return;
            }
            if (!"available".equalsIgnoreCase(unit.getStatus())) {
                AlertUtil.showError("Unavailable", "Unit is not available for checkout.");
                return;
            }

            String status = AdminUtil.isAdminMode() ? "Checked-out" : "Pending";
            int processedBy = AdminUtil.isAdminMode() ? loggedIn.getEmpId() : 0;

            Transaction transaction = new Transaction(
                0,
                unit.getUnitId(),
                loggedIn.getEmpId(),
                processedBy,
                LocalDateTime.now(),
                null,
                status,
                null
            );

            String error = handler.addTransaction(transactionDAO, transaction);
            if (error != null) {
                AlertUtil.showError("Error", error);
                return;
            }
            closeStage(event);
            AlertUtil.showInfo("Check out Requested", "Your check out request has been submitted and is pending admin approval.");
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to process checkout: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeStage(event);
    }

    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}