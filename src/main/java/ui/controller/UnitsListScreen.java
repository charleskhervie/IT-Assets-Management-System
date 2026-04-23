package ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import dao.handler.unitHandler;
import dao.impl.UnitDAOImpl;
import dao.intfc.UnitDAO;
import dao.model.Unit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.util.AdminUtil;
import ui.util.AlertUtil;
import ui.util.NavigationUtil;
import ui.util.UnitFilter;
import ui.util.UnitTableUtil;;

public class UnitsListScreen implements Initializable {

    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private TableView<Unit> unitsTable;
    @FXML private TableColumn<Unit, Integer> idColumn;
    @FXML private TableColumn<Unit, String> serialColumn;
    @FXML private TableColumn<Unit, Integer> equipmentColumn;
    @FXML private TableColumn<Unit, Integer> addedByColumn;
    @FXML private TableColumn<Unit, String> statusColumn;
    @FXML private TableColumn<Unit, Integer> assignedToColumn;
    @FXML private TableColumn<Unit, Void> actionsColumn;
    @FXML private Button checkOutButton;
    @FXML private Button checkInButton;
    @FXML private Button addUnitButton;
    @FXML private Button navEquipmentButton;
    @FXML private Button navCategoryButton;

    private static final String STATUS_ALL = "All";
    private static final String STATUS_AVAILABLE = "available";
    private static final String STATUS_CHECKED_OUT = "checked-out";
    private static final String STATUS_MAINTENANCE = "maintenance";

    private final unitHandler handler = new unitHandler();
    private final ObservableList<Unit> masterList = FXCollections.observableArrayList();
    private FilteredList<Unit> filteredList;
    private UnitDAO unitDAO;
    
    @FXML private void handleUnits(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/fxml/unitsList.fxml"); 
    }
    @FXML private void handleImportExport(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/importExport.fxml"); 
    }
    @FXML private void handleTransactions(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/fxml/Transaction.fxml"); 
    }
    @FXML private void handleEmployees(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Employee.fxml"); 
    }
    @FXML private void handleReports(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/report.fxml"); 
    }
    @FXML private void handleDashboard(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Dashboard.fxml"); 
    }
    @FXML private void handleBackToDashboard(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Dashboard.fxml"); 
    }
    @FXML private void handleNavEquipment(ActionEvent event) {
        NavigationUtil.loadIntoDashboard(event, "/fxml/equipmentList.fxml");
    }

    @FXML private void handleNavCategory(ActionEvent event) {
        NavigationUtil.loadIntoDashboard(event, "/fxml/categoryList.fxml");
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDAO();
        initStatusFilter();
        initTable();
        initFilterListeners();
        initButtonVisibility();
        loadData();
    }

     private void initFilterListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void initButtonVisibility() {
        boolean isAdmin = AdminUtil.isAdminMode();
        setUserButtonVisibility(!isAdmin);
        setAdminButtonVisibility(isAdmin);
    }

    private void setUserButtonVisibility(boolean visible) {
        if (checkOutButton != null) {
            checkOutButton.setVisible(visible);
            checkOutButton.setManaged(visible);
        }
        if (checkInButton != null) {
            checkInButton.setVisible(visible);
            checkInButton.setManaged(visible);
        }
    }

    private void setAdminButtonVisibility(boolean visible) {
        if (addUnitButton != null) {
            addUnitButton.setVisible(visible);
            addUnitButton.setManaged(visible);
        }
        if (navEquipmentButton != null) {
            navEquipmentButton.setVisible(true);
            navEquipmentButton.setManaged(true);
        }
        if (navCategoryButton != null) {
            navCategoryButton.setVisible(true);
            navCategoryButton.setManaged(true);
        }
    }    

    private void initDAO() {
        if (unitDAO == null){
            unitDAO = new UnitDAOImpl();
        }
    }

    private void initStatusFilter() {
        statusFilter.getItems().addAll(STATUS_ALL, STATUS_AVAILABLE, STATUS_CHECKED_OUT, STATUS_MAINTENANCE);
        statusFilter.setValue(STATUS_ALL);
    }

   @SuppressWarnings("unchecked")
    private void initTable() {
        UnitTableUtil.setupColumns((TableView<Object>) (TableView<?>) unitsTable);

        if (AdminUtil.isAdminMode()) {
            MenuItem editItem = new MenuItem("Edit Unit");
            MenuItem deleteItem = new MenuItem("Delete Selected");
            editItem.setOnAction(e -> handleEditSelected());
            deleteItem.setOnAction(e -> handleDeleteSelected());
            UnitTableUtil.setupContextMenu((TableView<Object>) (TableView<?>) unitsTable, editItem, deleteItem);
        }

        filteredList = new FilteredList<>(masterList, u -> true);
        unitsTable.setItems((ObservableList) filteredList);
    }

    public void loadData() {
        masterList.setAll(handler.getUnits(unitDAO));
    }

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();
        filteredList.setPredicate(unit -> UnitFilter.matches(unit, status, keyword));
    }

    private void handleEditSelected() {
        List<Unit> selected = unitsTable.getSelectionModel().getSelectedItems();
        if (selected.size() > 1) {
            AlertUtil.showError("Edit Error", "Please select only one unit to edit.");
            return;
        }
        Unit unit = unitsTable.getSelectionModel().getSelectedItem();
        if (unit == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editAsset.fxml"));
            Parent root = loader.load();

            EditUnitScreen controller = loader.getController();
            controller.setUnit(unit);   

            Stage modal = new Stage();
            modal.initOwner(unitsTable.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Edit Unit");
            modal.setScene(new Scene(root));
            modal.showAndWait();

            loadData();  
        } catch (IOException e) {
            throw new RuntimeException("Failed to load editAsset.fxml", e);
        }
    }
    @FXML
    private void handleAddUnit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/addAsset.fxml"));
            Parent root = loader.load();

            Stage modal = new Stage();
            modal.initOwner(unitsTable.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Add New Unit");
            modal.setScene(new Scene(root));
            modal.showAndWait();

            loadData(); // Refresh the table after adding
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not load the Add Unit screen.");
            e.printStackTrace();
        }
    }

    private void handleDeleteSelected() {
        List<Unit> selected = new ArrayList<>(unitsTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()){
            return;
        }
        boolean alert = AlertUtil.showConfirmation("Delete Units", "Are you sure you want to delete " + selected.size() + " unit(s)?");
        if (!alert){
            return;
        }
        List<String> errors = deleteUnits(selected);
        if (!errors.isEmpty()){
            AlertUtil.showError("Some Deletions Failed", String.join("\n", errors));
        }
        loadData();
    }

    private List<String> deleteUnits(List<Unit> units) {
        List<String> errors = new ArrayList<>();
        for (Unit unit : units) {
            String error = handler.deleteUnit(unitDAO, unit.getUnitId());
            if (error != null){
                errors.add("Unit " + unit.getUnitId() + ": " + error);
            }
        }
        return errors;
    }
    @FXML private void handleExit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Exit");
        confirmAlert.setHeaderText("Exit to Login");
        confirmAlert.setContentText("Are you sure you want to exit and return to the login page?");

        Optional<ButtonType> response = confirmAlert.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) return;

        NavigationUtil.loadScene(event, "/fxml/login.fxml");
    }
    @FXML private void handleCheckOut(ActionEvent event) { 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Check-out.fxml"));
            Parent root = loader.load();
            Stage modal = new Stage();
            modal.initOwner(unitsTable.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Check Out");
            modal.setScene(new Scene(root));
            modal.showAndWait();
            loadData();
        } catch (IOException e) {
            AlertUtil.showError("Error", "Could not load Check-out screen.");
        }
    }

    @FXML private void handleCheckIn(ActionEvent event) { 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Check-in.fxml"));
            Parent root = loader.load();
            Stage modal = new Stage();
            modal.initOwner(unitsTable.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Check In");
            modal.setScene(new Scene(root));
            modal.showAndWait();
            loadData();
        } catch (IOException e) {
            AlertUtil.showError("Error", "Could not load Check-in screen.");
        }
    }

    

}