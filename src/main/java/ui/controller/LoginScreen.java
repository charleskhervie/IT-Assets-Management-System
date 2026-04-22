package ui.controller;

import java.io.IOException;
import java.util.ResourceBundle;

import java.net.URL;

import dao.dao_util.CredentialManager;
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

        try {
            CredentialManager credentialManager = new CredentialManager();
            credentialManager.write(username, password, role);

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException exception) {
            exception.printStackTrace();
            errorLabel.setText("Failed to continue login");
            errorLabel.setVisible(true);
            System.out.println(exception);

        }

    }
    
}