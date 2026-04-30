package ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import dao.handler.TransactionHandler;
import dao.impl.TransactionDAOImpl;
import dao.intfc.TransactionDAO;
import dao.model.Employee;
import dao.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import ui.service.TableExportService;
import javafx.stage.FileChooser;
import java.io.File;

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
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private List<Transaction> currentFilteredData = new ArrayList<>();

    private static final String STATUS_ALL = "All";
    private static final String STATUS_CHECKED_OUT = "checked-out";
    private static final String STATUS_RETURNED = "Returned";
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_DECLINED = "declined";

    private final TransactionHandler handler = new TransactionHandler();
    private final ObservableList<Transaction> masterList = FXCollections.observableArrayList();
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

        currentFilteredData = new ArrayList<>(masterList);
        updatePage();
    }

    private void initDAO() {
        if (transactionDAO == null) {
            transactionDAO = new TransactionDAOImpl();
        }
    }

    private void initStatusFilter() {
        statusFilter.getItems().addAll(STATUS_ALL, STATUS_CHECKED_OUT, STATUS_RETURNED.toLowerCase(), STATUS_PENDING.toLowerCase(), STATUS_DECLINED);
        statusFilter.setValue(STATUS_ALL);
    }

    private void initFilterListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();
        currentFilteredData = masterList.stream()
            .filter(t -> TransactionFilter.matches(t, status, keyword))
            .collect(java.util.stream.Collectors.toList());
        currentPage = 0;
        updatePage();
    }

    private void updatePage() {
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredData.size());
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        transactionTable.setItems(FXCollections.observableArrayList(
            currentFilteredData.subList(fromIndex, toIndex)
        ));

        if (pageLabel != null)
            pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (prevButton != null)
            prevButton.setDisable(currentPage == 0);
        if (nextButton != null)
            nextButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML private void handlePrev(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML private void handleNext(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePage();
        }
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
            masterList.setAll(handler.getTransactionDisplay(transactionDAO));
        } else {
            Employee emp = SessionManager.getLoggedInEmployee();
            if (emp != null) {
                masterList.setAll(handler.getTransactionsByEmployee(transactionDAO, emp.getEmpId()));
            } else {
                masterList.clear();
            }
        }
        applyFilters();
    }

    @FXML private void handleExportPdf(ActionEvent event) {
        if (currentFilteredData.isEmpty()) {
            AlertUtil.showError("Export Error", "No transactions to export. Please check your filters.");
            return;
        }

        try {
            String filterDesc = buildFilterDescription();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export Transactions as PDF");
            chooser.setInitialFileName("transactions-export.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = chooser.showSaveDialog(transactionTable.getScene().getWindow());
            if (file == null) {
                return;
            }

            TableExportService.exportTransactionsToPdf(file.toPath(), currentFilteredData, filterDesc);
            AlertUtil.showInfo("Export Complete", "Transactions exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertUtil.showError("Export Failed", e.getMessage());
        }
    }

    private String buildFilterDescription() {
        StringBuilder desc = new StringBuilder();
        String status = statusFilter.getValue();
        String search = searchField.getText().trim();

        if (status != null && !status.equals(STATUS_ALL)) {
            desc.append("Status: ").append(status);
        }
        if (!search.isEmpty()) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("Search: ").append(search);
        }
        return desc.toString();
    }
}