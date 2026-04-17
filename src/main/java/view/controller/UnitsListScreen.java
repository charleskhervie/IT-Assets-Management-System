package view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;

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
    private Button checkInOutButton;

    @FXML
    private Button backButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusFilter.getItems().addAll("All", "available", "checked-out", "maintenance");
        statusFilter.setValue("All");
    }

    @FXML
    private void handleCheckInOut() {
        // TODO: implement check-out/check-in panel navigation
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load Dashboard.fxml", exception);
        }
    }
}
