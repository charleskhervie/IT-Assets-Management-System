package ui.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import dao.handler.TransactionHandler;
import dao.impl.TransactionDAOImpl;
import dao.intfc.TransactionDAO;
import dao.model.Employee;
import dao.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.util.AdminUtil;
import ui.util.AlertUtil;
import ui.util.SessionManager;
import ui.util.TransactionFilter;
import ui.util.TransactionTableUtil;

public class TransactionScreen implements Initializable {

    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, Integer> unitIdColumn;
    @FXML private TableColumn<Transaction, Integer> borrowedByColumn;
    @FXML private TableColumn<Transaction, Integer> processedByColumn;
    @FXML private TableColumn<Transaction, LocalDateTime> borrowDateColumn;
    @FXML private TableColumn<Transaction, LocalDateTime> returnDateColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;
    @FXML private TableColumn<Transaction, String> remarksColumn;
    @FXML private TableColumn<Transaction, Void> actionsColumn;

    private static final String STATUS_ALL = "All";
    private static final String STATUS_BORROWED = "Borrowed";
    private static final String STATUS_RETURNED = "Returned";
    private static final String STATUS_PENDING = "Pending";

    private final TransactionHandler handler = new TransactionHandler();
    private final ObservableList<Transaction> masterList = FXCollections.observableArrayList();
    private FilteredList<Transaction> filteredList;
    private TransactionDAO transactionDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDAO();
        initStatusFilter();
        initTable();
        initFilterListeners();
        loadData();
    }
    @SuppressWarnings("unchecked")
    private void initTable() {
        TableView<Object> table = (TableView<Object>) (TableView<?>) transactionTable;
        
        TransactionTableUtil.setupColumns(table);
        
        if (AdminUtil.isAdminMode()) {
            TableColumn<Object, Void> actionsColumn = new TableColumn<>("Actions");
            TransactionTableUtil.setupActionsColumn(
                    actionsColumn,
                    t -> handleApprove((Transaction) t),
                    t -> handleDecline((Transaction) t),
                    true
            );
            table.getColumns().add(actionsColumn);
        }

        MenuItem viewItem = new MenuItem("View Transaction");
        viewItem.setOnAction(e -> handleViewSelected());
        TransactionTableUtil.setupContextMenu(table, this::handleViewSelected, viewItem);

        filteredList = new FilteredList<>(masterList, t -> true);
        table.setItems((ObservableList) filteredList);
    }

    private void initDAO() {
        if (transactionDAO == null) {
            transactionDAO = new TransactionDAOImpl();
        }
    }

    private void initStatusFilter() {
        statusFilter.getItems().addAll(STATUS_ALL, STATUS_BORROWED, STATUS_RETURNED, STATUS_PENDING);
        statusFilter.setValue(STATUS_ALL);
    }

    private void initFilterListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();
        filteredList.setPredicate(t -> TransactionFilter.matches(t, status, keyword));
    }
    

    private void handleViewSelected() {
        Transaction transaction = transactionTable.getSelectionModel().getSelectedItem();
        if (transaction == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/viewTransaction.fxml"));
            Parent root = loader.load();

            ViewTransactionModalScreen controller = loader.getController();
            controller.setTransaction(transaction);

            Stage modal = new Stage();
            modal.initOwner(transactionTable.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("View Transaction");
            modal.setScene(new Scene(root));
            modal.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load viewTransaction.fxml", e);
        }
    }
    private void handleApprove(Transaction transaction) {
        boolean confirmed = AlertUtil.showConfirmation("Approve",
                "Approve checkout for unit " + transaction.getUnitId() + "?");
        if (!confirmed) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addRemarks.fxml"));
            Parent root = loader.load();

            AddRemarksModal controller = loader.getController();

            Stage modal = new Stage();
            modal.initOwner(transactionTable.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Add Remarks");
            modal.setScene(new Scene(root));
            modal.showAndWait();

            if (!controller.isSaved()) return;

            String error = handler.approveCheckout(transactionDAO, transaction.getTransactionId(), controller.getRemarks());
            if (error != null) {
                AlertUtil.showError("Error", error);
            }
            loadData();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load addRemarks.fxml", e);
        }
    }

    private void handleDecline(Transaction transaction) {
        boolean confirmed = AlertUtil.showConfirmation("Decline",
                "Decline checkout for unit " + transaction.getUnitId() + "?");
        if (!confirmed) return;

        String error = handler.deleteTransaction(transactionDAO, transaction.getTransactionId());
        if (error != null) {
            AlertUtil.showError("Error", error);
        }
        loadData();
    }

    public void loadData() {
        boolean isAdmin = AdminUtil.isAdminMode();
        if (isAdmin) {
            masterList.setAll(handler.getTransactions(transactionDAO));
        } else {
            Employee emp = SessionManager.getLoggedInEmployee();
            if (emp != null) {
                masterList.setAll(handler.getTransactionsByEmployee(transactionDAO, emp.getEmpId()));
            }
        }
    }
}