package ui.controller;

import dao.dao_util.CredentialManager;
import dao.dao_util.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SetupScreen {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Label statusLabel;
    @FXML private Button connectButton;

    private final CredentialManager credentialManager = new CredentialManager();

    @FXML
    public void initialize() {
        // Pre-fill defaults
        hostField.setText("localhost");
        portField.setText("3306");
    }

    @FXML
    private void handleConnect() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String user = userField.getText().trim();
        String pass = passField.getText();

        if (host.isEmpty() || port.isEmpty() || user.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        showStatus("Testing connection...", false);

        // Test connection with provided credentials
        if (DBUtil.isValid(user, pass, host, port)) {
            try {
                // Save credentials to app.env
                credentialManager.write(user, pass); 

                // Initialize DB (create tables + sample data if needed)
                DBUtil.initializeDatabase();

                // Go to login screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) connectButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } catch (Exception e) {
                showError("Setup failed: " + e.getMessage());
            }
        } else {
            showError("Connection failed. Check your credentials.");
        }
    }

    private void showError(String msg) {
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27AE60;");
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
    }
}