package ui.controller;

import dao.dao_util.CredentialManager;
import dao.dao_util.DatabaseSetup;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class DatabaseSetupDialogController {
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    
    private CredentialManager credentialManager;
    private Runnable onSuccess;
    
    @FXML
    public void initialize() {
        credentialManager = new CredentialManager();
    }
    
    @FXML
    private void handleTest() {
        if (validateInputs()) {
            statusLabel.setText("Testing connection...");
            statusLabel.setTextFill(Color.web("#FF9800"));
            
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            String user = usernameField.getText();
            String password = passwordField.getText();
            
            if (DatabaseSetup.testConnection(host, port, user, password)) {
                statusLabel.setText("✓ Connection successful!");
                statusLabel.setTextFill(Color.web("#4CAF50"));
            } else {
                statusLabel.setText("✗ Connection failed. Please check your credentials.");
                statusLabel.setTextFill(Color.web("#F44336"));
            }
        }
    }
    
    @FXML
    private void handleSetup() {
        System.out.println("🔵 DEBUG: handleSetup called");
        
        if (validateInputs()) {
            System.out.println("🔵 DEBUG: Input validation passed");
            statusLabel.setText("Setting up database...");
            statusLabel.setTextFill(Color.web("#FF9800"));
            
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            String user = usernameField.getText();
            String password = passwordField.getText();
            
            System.out.println("🔵 DEBUG: Testing connection to " + host + ":" + port);
            if (!DatabaseSetup.testConnection(host, port, user, password)) {
                statusLabel.setText("✗ Cannot connect to MySQL. Please verify credentials.");
                statusLabel.setTextFill(Color.web("#F44336"));
                System.out.println("❌ DEBUG: Connection test failed");
                return;
            }
            System.out.println("✅ DEBUG: Connection test passed");
            
            System.out.println("🔵 DEBUG: Creating database");
            if (!DatabaseSetup.createDatabase(host, port, user, password)) {
                statusLabel.setText("✗ Failed to create database. Please check permissions.");
                statusLabel.setTextFill(Color.web("#F44336"));
                System.out.println("❌ DEBUG: Database creation failed");
                return;
            }
            System.out.println("✅ DEBUG: Database created");
            
            try {
                System.out.println("🔵 DEBUG: Writing credentials...");
                credentialManager.write(host, port, user, password, "admin");
                System.out.println("✅ DEBUG: Credentials written successfully");
                statusLabel.setText("✓ Configuration saved successfully!");
                statusLabel.setTextFill(Color.web("#4CAF50"));
                
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    closeDialog();
                });
            } catch (IOException e) {
                System.out.println("❌ DEBUG: IOException when writing credentials: " + e.getMessage());
                e.printStackTrace();
                statusLabel.setText("✗ Error saving configuration: " + e.getMessage());
                statusLabel.setTextFill(Color.web("#F44336"));
            }
        } else {
            System.out.println("❌ DEBUG: Input validation failed");
        }
    }
    
    private boolean validateInputs() {
        if (hostField.getText().trim().isEmpty()) {
            statusLabel.setText("✗ Host cannot be empty");
            statusLabel.setTextFill(Color.web("#F44336"));
            return false;
        }
        
        try {
            int port = Integer.parseInt(portField.getText());
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Port out of range");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("✗ Invalid port number (1-65535)");
            statusLabel.setTextFill(Color.web("#F44336"));
            return false;
        }
        
        if (usernameField.getText().trim().isEmpty()) {
            statusLabel.setText("✗ Username cannot be empty");
            statusLabel.setTextFill(Color.web("#F44336"));
            return false;
        }
        
        return true;
    }
    
    private void closeDialog() {
        Stage stage = (Stage) hostField.getScene().getWindow();
        stage.close();
        if (onSuccess != null) {
            onSuccess.run();
        }
    }
    
    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }
}
