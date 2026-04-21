package view.controller;

import java.net.URL;
import java.util.ResourceBundle;

import view.util.AdminUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import view.util.NavigationUtil;

import java.util.Optional;

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
    private Button reportsButton;
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

        if (!adminMode) {
            NavigationUtil.loadIntoDashboardFromPane(rootPane, "/view/unitsList.fxml");
        }
    }

    @FXML private void handleUnits(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/view/unitsList.fxml"); 
    }
    @FXML private void handleImportExport(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/view/importExport.fxml"); 
    }
    @FXML private void handleTransactions(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/view/Transaction.fxml"); 
    }
    @FXML private void handleEmployees(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/view/Employee.fxml"); 
    }
    @FXML private void handleReports(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/view/report.fxml"); 
    }
    @FXML private void handleDashboard(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/view/Dashboard.fxml"); 
    }
    @FXML private void handleBackToDashboard(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/view/Dashboard.fxml"); 
    }

    @FXML private void handleExit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Exit");
        confirmAlert.setHeaderText("Exit to Login");
        confirmAlert.setContentText("Are you sure you want to exit and return to the login page?");

        Optional<ButtonType> response = confirmAlert.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) return;

        NavigationUtil.loadScene(event, "/view/login.fxml");
    }
}