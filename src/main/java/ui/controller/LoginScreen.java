package ui.controller;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import java.net.URL;
import ui.util.SessionManager;


import dao.dao_util.CredentialManager;
import dao.handler.EmployeeHandler;
import dao.impl.EmployeeDAOImpl;
import dao.intfc.EmployeeDAO;
import dao.model.Employee;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

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

    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleComboBox.getItems().addAll("Admin", "Employee");
        errorLabel.setVisible(false);
    }
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            errorLabel.setVisible(true);
            return;
        }
        if (role == null || role.isBlank()) {
            errorLabel.setText("Please select a role.");
            errorLabel.setVisible(true);
            return;
        }

        Employee matchedEmployee = authenticateEmployee(username, password, role);
        if (matchedEmployee == null) {
            errorLabel.setText("Invalid username or password.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            // Validate employee exists
             
            EmployeeDAO employeeDAO = new EmployeeDAOImpl();
            EmployeeHandler employeeHandler = new EmployeeHandler();
            Employee employee = employeeHandler.getEmployeeByUsername(employeeDAO, username);
            if (employee == null) {
                errorLabel.setText("Username not found.");
                errorLabel.setVisible(true);
                return;
            }

            // Store in session
            SessionManager.setLoggedInEmployee(employee);

            CredentialManager credentialManager = new CredentialManager();
            credentialManager.writeAppSession(matchedEmployee.getUsername(), normalizeAppMode(matchedEmployee.getRole()));

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException exception) {
            exception.printStackTrace();
            errorLabel.setText("Failed to continue login.");
            errorLabel.setVisible(true);
        } 
        /* 
        catch (SQLException exception) {
            exception.printStackTrace();
            errorLabel.setText("Database error during login.");
            errorLabel.setVisible(true);
        }
            */
    }

    private Employee authenticateEmployee(String username, String password, String selectedRole) {
        try {
            List<Employee> employees = employeeDAO.findWithAttribute("username", username);
            if (employees.isEmpty()) {
                return null;
            }

            Employee employee = employees.get(0);
            if (!password.equals(employee.getPassword())) {
                return null;
            }

            String appMode = normalizeAppMode(employee.getRole());
            if (!appMode.equalsIgnoreCase(selectedRole)) {
                return null;
            }

            return employee;
        } catch (Exception e) {
            errorLabel.setText("Login failed. Check database connection.");
            errorLabel.setVisible(true);
            return null;
        }
    }

    private String normalizeAppMode(String employeeRole) {
        return "Admin".equalsIgnoreCase(employeeRole) ? "Admin" : "Employee";
    }
    
}