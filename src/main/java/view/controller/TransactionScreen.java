package view.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import view.util.NavigationUtil;

public class TransactionScreen implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    @FXML private void handleDashboard(ActionEvent event){
        NavigationUtil.loadScene(event, "/view/Dashboard.fxml"); 
    }
    @FXML private void handleUnits(ActionEvent event){
        NavigationUtil.loadDashboardWithContent(event, "/view/unitsList.fxml");
    }    
    @FXML private void handleTransactions(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/view/Transaction.fxml"); 
    }
    @FXML private void handleEmployees(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/view/Employee.fxml"); 
    }
    @FXML private void handleReports(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/view/report.fxml"); 
    }
    @FXML private void handleImportExport(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/view/importExport.fxml"); 
    }
    @FXML private void handleExit(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/view/login.fxml"); 
    }
}