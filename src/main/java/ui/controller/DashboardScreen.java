package ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import dao.dao_util.CredentialManager;
import dao.handler.EmployeeHandler;
import dao.handler.EquipmentHandler;
import dao.handler.TransactionHandler;
import dao.handler.unitHandler;
import dao.impl.EmployeeDAOImpl;
import dao.impl.EquipmentDAOImpl;
import dao.impl.TransactionDAOImpl;
import dao.impl.UnitDAOImpl;
import ui.util.AdminUtil;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import ui.util.NavigationUtil;

import java.util.Optional;
import java.util.Properties;
/**
 * Controller for the Main Dashboard Screen.(important)
 * 
 * Serves as the central navigation hub and primary shell for the IT Asset Management 
 * System. It coordinates the display of high-level inventory metrics and manages 
 * the dynamic loading of sub-interfaces within a centralized viewing area.
 * 
 * - Orchestrates conditional UI rendering based on administrative privileges, 
 *   restricting access to sensitive modules via {@link AdminUtil#isAdminMode()}.
 * - Manages the primary navigation state, utilizing a reactive styling mechanism 
 *   to highlight the currently active module.
 * - Aggregates system-wide statistics by querying the handler layer to provide 
 *   real-time counts for units, equipment, employees, and transactions.
 * - Integrates with {@link CredentialManager} to securely display session data, 
 *   including the current user's identity and system role.
 * - Facilitates modular content swapping within the {@link BorderPane} using 
 *   {@link NavigationUtil} for a seamless single-page application experience.
 */
public class DashboardScreen implements Initializable {

    @FXML
    private BorderPane rootPane;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button importExportButton;
    @FXML
    private Button transactionsButton;
    @FXML
    private Button employeesButton;
    @FXML
    private Button unitsButton;
    @FXML
    private Button equipmentButton;
    @FXML
    private Button categoryButton;
    @FXML
    private Button reportsButton;
    @FXML 
    private Label unitCountLabel;
    @FXML 
    private Label equipmentCountLabel;
    @FXML 
    private Label employeeCountLabel;
    @FXML 
    private Label transactionCountLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label roleLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boolean adminMode = AdminUtil.isAdminMode();

        if (dashboardButton != null){ 
            dashboardButton.setVisible(adminMode);    
            dashboardButton.setManaged(adminMode); 
        }
        if (importExportButton != null) { 
            importExportButton.setVisible(adminMode); importExportButton.setManaged(adminMode); 
        }
        if (employeesButton != null){ 
            employeesButton.setVisible(adminMode);    
            employeesButton.setManaged(adminMode); 
        }
        if (reportsButton != null){ 
            reportsButton.setVisible(adminMode);      
            reportsButton.setManaged(adminMode); 
        }
        if (transactionsButton != null) {
            transactionsButton.setVisible(true);
            transactionsButton.setManaged(true);
            transactionsButton.setText(adminMode ? "Transactions" : "History");
        }
        if (adminMode) {
            setActiveButton(dashboardButton);
        } else {
            setActiveButton(unitsButton);
        }
        if (usernameLabel != null && roleLabel != null) {
            try {
                CredentialManager cm = new CredentialManager();
                if (cm.exists()) {
                    Properties props = cm.load();
                    String appUser = props.getProperty("app_user", "Unknown");
                    String appRole = props.getProperty("app_mode", "Unknown");
                    usernameLabel.setText(appUser);
                    roleLabel.setText(appRole);
                }
            } catch (IOException e) {
                usernameLabel.setText("Unknown");
            }
        }

        if (!adminMode) {
            NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/unitsList.fxml");
        }

        loadCounts();
    }

    @FXML private void handleUnits(ActionEvent event){ 
        setActiveButton(unitsButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/unitsList.fxml"); 
    }
    @FXML private void handleTransactions(ActionEvent event){ 
        setActiveButton(transactionsButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/Transaction.fxml"); 
    }
    @FXML private void handleEmployees(ActionEvent event){ 
        setActiveButton(employeesButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/Employee.fxml"); 
    }
    @FXML private void handleReports(ActionEvent event){ 
        setActiveButton(reportsButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/report.fxml"); 
    }
    @FXML private void handleNavEquipment(ActionEvent event) {
        setActiveButton(equipmentButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/equipmentList.fxml");
    }
    @FXML private void handleNavCategory(ActionEvent event) {
        setActiveButton(categoryButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/categoryList.fxml");
    }
    @FXML private void handleDashboard(ActionEvent event){ 
        setActiveButton(dashboardButton);
        NavigationUtil.loadScene(event, "/fxml/Dashboard.fxml"); 
    }
    @FXML private void handleImportExport(ActionEvent event){ 
        setActiveButton(importExportButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/importExport.fxml"); 
    }
    @FXML private void handleUnitsClick(MouseEvent event){    
        setActiveButton(unitsButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/unitsList.fxml"); 
    }
    @FXML private void handleEquipmentClick(MouseEvent event){    
        setActiveButton(equipmentButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/equipmentList.fxml"); 
    }
    @FXML private void handleEmployeesClick(MouseEvent event){    
        setActiveButton(employeesButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/Employee.fxml"); 
    }
    @FXML private void handleTransactionsClick(MouseEvent event){    
        setActiveButton(transactionsButton);
        NavigationUtil.loadIntoDashboardFromPane(rootPane, "/fxml/Transaction.fxml"); 
    }

    public void loadContent(String fxmlPath) {
        NavigationUtil.loadIntoDashboardFromPane(rootPane, fxmlPath);
    }

    private void loadCounts() {
        unitHandler handler = new unitHandler();
        EquipmentHandler eqHandler = new EquipmentHandler();
        EmployeeHandler empHandler = new EmployeeHandler();
        TransactionHandler txHandler = new TransactionHandler();

        if (unitCountLabel != null)
            unitCountLabel.setText(String.valueOf(handler.getUnitsRaw(new UnitDAOImpl()).size()));
        if (equipmentCountLabel != null)
            equipmentCountLabel.setText(String.valueOf(eqHandler.getEquipments(new EquipmentDAOImpl()).size()));
        if (employeeCountLabel != null)
            employeeCountLabel.setText(String.valueOf(empHandler.getEmployees(new EmployeeDAOImpl()).size()));
        if (transactionCountLabel != null)
            transactionCountLabel.setText(String.valueOf(txHandler.getTransactionRaw(new TransactionDAOImpl()).size()));
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
    private void setActiveButton(Button clickedButton) {
        Button[] navButtons = {
            dashboardButton, unitsButton, equipmentButton, categoryButton,
            transactionsButton, employeesButton, reportsButton, importExportButton
        };

        for (Button btn : navButtons) {
            if (btn != null) btn.getStyleClass().remove("active");
        }

        if (clickedButton != null) {
            clickedButton.getStyleClass().add("active");
        }
    }
}