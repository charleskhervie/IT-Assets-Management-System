package ui.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ui.util.NavigationUtil;

public class ImportExportScreen implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

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