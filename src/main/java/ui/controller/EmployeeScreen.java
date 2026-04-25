package ui.controller;

import dao.handler.EmployeeHandler;
import dao.impl.EmployeeDAOImpl;
import dao.intfc.EmployeeDAO;
import dao.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import ui.util.EmployeeTableUtil;
import ui.util.ModalUtil;
import ui.util.NavigationUtil;
import ui.util.AlertUtil;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class EmployeeScreen implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> usernameColumn;
    @FXML private TableColumn<Employee, String> fullNameColumn;
    @FXML private TableColumn<Employee, String> roleColumn;
    @FXML private TableColumn<Employee, Integer> departmentColumn;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private final EmployeeHandler handler = new EmployeeHandler();
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();
    private final ObservableList<Employee> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private java.util.List<Employee> currentFilteredData = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EmployeeTableUtil.setupColumns(idColumn, usernameColumn, fullNameColumn, roleColumn, departmentColumn);
        setupContextMenu();
        initFilterListeners();
        loadData();
    }

    private void initFilterListeners() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void applyFilters() {
        String keyword = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        currentFilteredData = masterList.stream()
            .filter(employee -> matchesKeyword(employee, keyword))
            .collect(java.util.stream.Collectors.toList());
        currentPage = 0;
        updatePage();
    }

    private boolean matchesKeyword(Employee employee, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return String.valueOf(employee.getEmpId()).contains(keyword)
            || containsKeyword(employee.getUsername(), keyword)
            || containsKeyword(employee.getFullName(), keyword)
            || containsKeyword(employee.getRole(), keyword)
            || String.valueOf(employee.getDepartmentId()).contains(keyword);
    }

    private boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void updatePage() {
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredData.size());
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        employeeTable.setItems(FXCollections.observableArrayList(
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

    public void loadData() {
        
        masterList.setAll(handler.getEmployees(employeeDAO));
        applyFilters();
        
    }
    @FXML
    private void handleAddEmployee(ActionEvent event) { 
        ModalUtil.openModal(event, "/fxml/addEmployee.fxml", "Add Employee"); 
        loadData(); 
    }

    @FXML
    private void handleEditEmployee(ActionEvent event) {
        handleEditSelected();
    }

    @FXML
    private void handleDeleteEmployee(ActionEvent event) {
        handleDeleteSelected();
    }

    private void setupContextMenu() {
        employeeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        MenuItem editItem = new MenuItem("Edit Employee");
        MenuItem deleteItem = new MenuItem("Delete Selected");

        editItem.setOnAction(event -> handleEditSelected());
        deleteItem.setOnAction(event -> handleDeleteSelected());

        employeeTable.setContextMenu(new ContextMenu(editItem, deleteItem));
    }

    private void handleEditSelected() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addEmployee.fxml"));
            Parent root = loader.load();

            AddEmployeeModalScreen controller = loader.getController();
            controller.setEmployee(selected);

            Stage modal = new Stage();
            modal.initOwner(employeeTable.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Edit Employee");
            modal.setScene(new Scene(root));
            modal.showAndWait();

            loadData();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load edit employee modal", e);
        }
    }

    private void handleDeleteSelected() {
        ObservableList<Employee> selected = employeeTable.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }

        boolean confirm = AlertUtil.showConfirmation(
            "Delete Employees",
            "Are you sure you want to delete " + selected.size() + " employee(s)?"
        );
        if (!confirm) {
            return;
        }

        for (Employee employee : new ArrayList<>(selected)) {
            try {
                employeeDAO.delete(employee.getEmpId());
            } catch (SQLException e) {
                AlertUtil.showError("Delete Failed", "Employee " + employee.getEmpId() + ": " + e.getMessage());
            }
        }

        loadData();
    }

    @FXML private void handleUnits(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/fxml/unitsList.fxml"); 
    }
    @FXML private void handleImportExport(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/importExport.fxml"); 
    }
    @FXML private void handleTransactions(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/fxml/Transaction.fxml"); 
    }
    @FXML private void handleEmployees(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Employee.fxml"); 
    }
    @FXML private void handleReports(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/report.fxml"); 
    }
    @FXML private void handleDashboard(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Dashboard.fxml"); 
    }
    @FXML private void handleBackToDashboard(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Dashboard.fxml"); 
    }
    @FXML private void handleExit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Exit");
        confirmAlert.setHeaderText("Exit to Login");
        confirmAlert.setContentText("Are you sure you want to exit and return to the login page?");

        Optional<ButtonType> response = confirmAlert.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) return;

        NavigationUtil.loadScene(event, "/fxml/login.fxml");
    }
}