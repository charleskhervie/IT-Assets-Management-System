package ui.controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import dao.handler.TransactionHandler;
import dao.impl.TransactionDAOImpl;
import dao.intfc.TransactionDAO;
import dao.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

    private void initDAO() {
        if (transactionDAO == null) {
            transactionDAO = new TransactionDAOImpl();
        }
    }

    private void initStatusFilter() {
        statusFilter.getItems().addAll(STATUS_ALL, STATUS_BORROWED, STATUS_RETURNED);
        statusFilter.setValue(STATUS_ALL);
    }

    private void initTable() {
        TransactionTableUtil.setupColumns(idColumn, unitIdColumn, borrowedByColumn,
                processedByColumn, borrowDateColumn, returnDateColumn, statusColumn, remarksColumn);
        filteredList = new FilteredList<>(masterList, t -> true);
        transactionTable.setItems(filteredList);
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

    public void loadData() {
        masterList.setAll(handler.getTransactions(transactionDAO));
    }
}