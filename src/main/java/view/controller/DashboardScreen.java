package view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import dao.dao_util.CredentialManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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
        boolean adminMode = isAdminMode();

        if (dashboardButton != null) {
            dashboardButton.setVisible(adminMode);
            dashboardButton.setManaged(adminMode);
        }

        if (importExportButton != null) {
            importExportButton.setVisible(adminMode);
            importExportButton.setManaged(adminMode);
        }
        if (transactionsButton != null) {
            transactionsButton.setVisible(true);
            transactionsButton.setManaged(true);
            transactionsButton.setText(adminMode ? "Transactions" : "History");
        }
        if (employeesButton != null) {
            employeesButton.setVisible(adminMode);
            employeesButton.setManaged(adminMode);
        }
        if (reportsButton != null) {
            reportsButton.setVisible(adminMode);
            reportsButton.setManaged(adminMode);
        }

        if (!adminMode) {
            setCenterContentFromResource("/unitsList.fxml", "Failed to load unitsList.fxml");
        }
    }

    @FXML
    private void handleImportExport(ActionEvent event) {
        try {
            Parent loadedRoot = FXMLLoader.load(getClass().getResource("/importExport.fxml"));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Parent currentRoot = currentScene.getRoot();

            if (currentRoot instanceof BorderPane dashboardPane) {
                if (loadedRoot instanceof BorderPane importExportPane) {
                    dashboardPane.setCenter(importExportPane.getCenter());
                } else {
                    dashboardPane.setCenter(loadedRoot);
                }
                return;
            }

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(loadedRoot));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load importExport.fxml", exception);
        }
    }

    private boolean isAdminMode() {
        CredentialManager credentialManager = new CredentialManager();
        if (!credentialManager.exists()) {
            return true;
        }

        try {
            Properties properties = credentialManager.load();
            String mode = properties.getProperty("app_mode", "Admin");
            return "Admin".equalsIgnoreCase(mode);
        } catch (IOException exception) {
            return true;
        }
    }

    @FXML
    private void handleUnits(ActionEvent event) {
        replaceCenterContent(event, "/unitsList.fxml", "Failed to load unitsList.fxml");
    }

    @FXML
    private void handleTransactions(ActionEvent event) {
        try {
            Parent loadedRoot = FXMLLoader.load(getClass().getResource("/Transaction.fxml"));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Parent currentRoot = currentScene.getRoot();

            if (currentRoot instanceof BorderPane dashboardPane) {
                if (loadedRoot instanceof BorderPane transactionPane) {
                    dashboardPane.setCenter(transactionPane.getCenter());
                } else {
                    dashboardPane.setCenter(loadedRoot);
                }
                return;
            }

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(loadedRoot));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load Transaction.fxml", exception);
        }
    }

    @FXML
    private void handleEmployees(ActionEvent event) {
        try {
            Parent loadedRoot = FXMLLoader.load(getClass().getResource("/Employee.fxml"));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Parent currentRoot = currentScene.getRoot();

            if (currentRoot instanceof BorderPane dashboardPane) {
                if (loadedRoot instanceof BorderPane employeePane) {
                    dashboardPane.setCenter(employeePane.getCenter());
                } else {
                    dashboardPane.setCenter(loadedRoot);
                }
                return;
            }

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(loadedRoot));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load Employee.fxml", exception);
        }
    }

    @FXML
    private void handleReports(ActionEvent event) {
        try {
            Parent loadedRoot = FXMLLoader.load(getClass().getResource("/report.fxml"));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Parent currentRoot = currentScene.getRoot();

            if (currentRoot instanceof BorderPane dashboardPane) {
                if (loadedRoot instanceof BorderPane reportsPane) {
                    dashboardPane.setCenter(reportsPane.getCenter());
                } else {
                    dashboardPane.setCenter(loadedRoot);
                }
                return;
            }

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(loadedRoot));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load report.fxml", exception);
        }
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load Dashboard.fxml", exception);
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        handleBackToDashboard(event);
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Exit");
        confirmAlert.setHeaderText("Exit to Login");
        confirmAlert.setContentText("Are you sure you want to exit and return to the login page?");

        Optional<ButtonType> response = confirmAlert.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) {
            return;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load login.fxml", exception);
        }
    }

    private void replaceCenterContent(ActionEvent event, String resourcePath, String errorMessage) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource(resourcePath));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Parent rootNode = currentScene.getRoot();

            if (rootNode instanceof BorderPane borderPane) {
                borderPane.setCenter(content);
                return;
            }

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(content));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException(errorMessage, exception);
        }
    }

    private void setCenterContentFromResource(String resourcePath, String errorMessage) {
        try {
            Parent loadedRoot = FXMLLoader.load(getClass().getResource(resourcePath));
            if (rootPane == null) {
                return;
            }

            // Employee entry should render the full units panel (top/center/bottom), not center-only.
            rootPane.setCenter(loadedRoot);
        } catch (IOException exception) {
            throw new RuntimeException(errorMessage, exception);
        }
    }
}