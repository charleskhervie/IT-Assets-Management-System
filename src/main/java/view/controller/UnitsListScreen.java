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
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UnitsListScreen implements Initializable {

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<?> unitsTable;

    @FXML
    private TableColumn<?, ?> idColumn;

    @FXML
    private TableColumn<?, ?> serialColumn;

    @FXML
    private TableColumn<?, ?> equipmentColumn;

    @FXML
    private TableColumn<?, ?> categoryColumn;

    @FXML
    private TableColumn<?, ?> statusColumn;

    @FXML
    private TableColumn<?, ?> assignedToColumn;

    @FXML
    private TableColumn<?, ?> actionsColumn;

    @FXML
    private Button checkOutButton;

    @FXML
    private Button checkInButton;

    @FXML
    private Button addUnitButton;

    @FXML
    private Button addEquipmentButton;

    @FXML
    private Button addCategoryButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusFilter.getItems().addAll("All", "available", "checked-out", "maintenance");
        statusFilter.setValue("All");

        boolean adminMode = isAdminMode();
        checkOutButton.setVisible(!adminMode);
        checkOutButton.setManaged(!adminMode);
        checkInButton.setVisible(!adminMode);
        checkInButton.setManaged(!adminMode);
        addUnitButton.setVisible(adminMode);
        addUnitButton.setManaged(adminMode);
        addEquipmentButton.setVisible(adminMode);
        addEquipmentButton.setManaged(adminMode);
        addCategoryButton.setVisible(adminMode);
        addCategoryButton.setManaged(adminMode);
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        openModal(event, "/Check-out.fxml", "Check-out");
    }

    @FXML
    private void handleCheckIn(ActionEvent event) {
        openModal(event, "/Check-in.fxml", "Check-in");
    }

    @FXML
    private void handleAddUnit(ActionEvent event) {
        openModal(event, "/addAsset.fxml", "Add Unit");
    }

    @FXML
    private void handleAddEquipment(ActionEvent event) {
        openModal(event, "/AddEquipment.fxml", "Add Equipment");
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        openModal(event, "/AddCategory.fxml", "Add Category");
    }

    private void openModal(ActionEvent event, String resourcePath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();
            Stage owner = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage popup = new Stage();
            popup.initOwner(owner);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setResizable(false);
            popup.setTitle(title);
            popup.setScene(new Scene(root));
            popup.showAndWait();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load " + resourcePath, exception);
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

}
