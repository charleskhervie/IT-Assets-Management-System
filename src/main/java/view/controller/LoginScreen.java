package view.controller;

import java.io.IOException;
import java.util.ResourceBundle;

import java.net.URL;

import dao.dao_util.CredentialManager;
import itams.auth.SessionContext;
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
        roleComboBox.setValue("Employee");
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || selectedRole == null || selectedRole.isBlank()) {
            errorLabel.setText("Please fill in all fields.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            CredentialManager credentialManager = new CredentialManager();
            credentialManager.write(username, password, selectedRole);

            SessionContext.setSession(username, selectedRole);

            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException exception) {
            errorLabel.setText("Unable to continue login.");
            errorLabel.setVisible(true);
        }
    }
}