package view.controller;

import java.util.ResourceBundle;
import java.net.URL;
import java.sql.SQLException;

import dao.impl.EmployeeDAOImpl;
import dao.model.Employee;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleComboBox.getItems().addAll("Admin", "Employee");
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            EmployeeDAOImpl dao = new EmployeeDAOImpl();
            Employee employee = dao.authenticate(username, password);
            if (employee == null) {
                errorLabel.setText("Invalid username or password.");
                errorLabel.setVisible(true);
                return;
            }
            errorLabel.setVisible(false);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("IT Assets Management System - Dashboard");
            stage.show();
        } catch (SQLException e) {
            System.err.println("Login database error: " + e.getMessage());
            errorLabel.setText("A database error occurred. Please try again.");
            errorLabel.setVisible(true);
        } catch (Exception e) {
            errorLabel.setText("An error occurred. Please try again.");
            errorLabel.setVisible(true);
        }
    }
}
