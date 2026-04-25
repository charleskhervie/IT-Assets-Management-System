package ui.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import dao.handler.TransactionHandler;
import dao.impl.TransactionDAOImpl;
import dao.intfc.TransactionDAO;
import dao.model.Employee;
import dao.model.Transaction;
import dao.model.Unit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.util.AlertUtil;
import ui.util.SessionManager;

public class CheckInScreen implements Initializable {

    @FXML private ComboBox<String> checkInComboBox;
    @FXML private TextField borrowerField;

    private final TransactionHandler handler = new TransactionHandler();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private List<Transaction> checkedOutTransactions;
    public void setTargetUnit(Unit unit) {
        if (unit == null || checkedOutTransactions == null) return;

        for (int i = 0; i < checkedOutTransactions.size(); i++) {
            if (checkedOutTransactions.get(i).getUnitId() == unit.getUnitId()) {
                checkInComboBox.getSelectionModel().select(i);
                break;
            }
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Employee emp = SessionManager.getLoggedInEmployee();
        if (emp == null) return;

        borrowerField.setText(emp.getFullName());

        checkedOutTransactions = handler.getCheckedOutByEmployee(transactionDAO, emp.getEmpId());

        if (checkedOutTransactions.isEmpty()) {
            checkInComboBox.getItems().add("No checked-out items");
            checkInComboBox.setValue("No checked-out items");
            checkInComboBox.setDisable(true);
        } else {
            checkInComboBox.getItems().clear();
            for (Transaction t : checkedOutTransactions) {
                checkInComboBox.getItems().add("Unit " + t.getUnitId() + " (Transaction #" + t.getTransactionId() + ")");
            }
        }
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        if (checkedOutTransactions == null || checkedOutTransactions.isEmpty()) {
            AlertUtil.showError("No Items", "You have no checked-out items to return.");
            return;
        }

        int selectedIndex = checkInComboBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            AlertUtil.showError("Selection Error", "Please select an item to check in.");
            return;
        }

        Transaction selected = checkedOutTransactions.get(selectedIndex);
        String error = handler.checkIn(transactionDAO, selected.getTransactionId());
        if (error != null) {
            AlertUtil.showError("Error", error);
            return;
        }

        closeStage(event);
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