package view.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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
    private void handleBack() {
        // TODO: implement back navigation
    }
}
