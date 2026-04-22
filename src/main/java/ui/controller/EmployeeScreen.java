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

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> usernameColumn;
    @FXML private TableColumn<Employee, String> fullNameColumn;
    @FXML private TableColumn<Employee, String> roleColumn;
    @FXML private TableColumn<Employee, Integer> departmentColumn;

    private final EmployeeHandler handler = new EmployeeHandler();
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();
    private final ObservableList<Employee> masterList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EmployeeTableUtil.setupColumns(idColumn, usernameColumn, fullNameColumn, roleColumn, departmentColumn);
        setupContextMenu();
        loadData();
    }
    public void loadData() {
        
        masterList.setAll(handler.getEmployees(employeeDAO));
        employeeTable.setItems(masterList);
        
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