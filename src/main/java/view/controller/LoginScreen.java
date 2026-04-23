package view.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;

import java.net.URL;

import dao.impl.EmployeeDAOImpl;
import dao.model.Employee;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;

public class LoginScreen implements Initializable{

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private ComboBox<String> roleComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize your ComboBox values
        roleComboBox.getItems().addAll("Admin", "Employee");
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (selectedRole == null || selectedRole.isEmpty()) {
            showError("Please select a role.");
            return;
        }

        try {
            EmployeeDAOImpl employeeDAO = new EmployeeDAOImpl();
            Employee employee = employeeDAO.findByUsernameAndPassword(username, password);

            if (employee == null || !employee.getRole().equalsIgnoreCase(selectedRole)) {
                showError("Invalid credentials. Please try again.");
                return;
            }

            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("IT Assets Management System - Dashboard");
            stage.show();
        } catch (SQLException e) {
            showError("Unable to connect to the database. Please try again later.");
        } catch (IOException e) {
            showError("Failed to load dashboard.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}