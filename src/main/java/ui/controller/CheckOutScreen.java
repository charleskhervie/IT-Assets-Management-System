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

public class CheckOutScreen {

    @FXML private TextField serialNoField;
    @FXML private TextField borrowerField;

    private final TransactionHandler handler = new TransactionHandler();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private final UnitDAO unitDAO = new UnitDAOImpl();

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
            // Find unit by serial number
            List<Unit> units = unitDAO.findWithAttribute("serial_number", serialNo);
            if (units.isEmpty()) {
                AlertUtil.showError("Not Found", "No unit found with serial number: " + serialNo);
                return;
            }
            Unit unit = units.get(0);
            if (!"available".equalsIgnoreCase(unit.getStatus())) {
                AlertUtil.showError("Unavailable", "Unit is not available for checkout.");
                return;
            }

            String status = AdminUtil.isAdminMode() ? "checked-out" : "pending";
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