package ui.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class TransactionScreen implements Initializable {

    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private TableView<?> transactionTable;
    @FXML private TableColumn<?, ?> idColumn;
    @FXML private TableColumn<?, ?> unitIdColumn;
    @FXML private TableColumn<?, ?> borrowedByColumn;
    @FXML private TableColumn<?, ?> processedByColumn;
    @FXML private TableColumn<?, ?> borrowDateColumn;
    @FXML private TableColumn<?, ?> returnDateColumn;
    @FXML private TableColumn<?, ?> statusColumn;
    @FXML private TableColumn<?, ?> remarksColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}