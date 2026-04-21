package view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import view.util.AdminUtil;
import view.util.AlertUtil;
import view.util.ModalUtil;
import view.util.UnitFilter;
import view.util.UnitTableUtil;;

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
    @FXML private Button addEquipmentButton;
    @FXML private Button addCategoryButton;

    private static final String STATUS_ALL = "All";
    private static final String STATUS_AVAILABLE = "available";
    private static final String STATUS_CHECKED_OUT = "checked-out";
    private static final String STATUS_MAINTENANCE = "maintenance";

    private final unitHandler handler = new unitHandler();
    private final ObservableList<Unit> masterList = FXCollections.observableArrayList();
    private FilteredList<Unit> filteredList;
    private UnitDAO unitDAO;
    
    @FXML private void handleCheckOut(ActionEvent event) { 
        ModalUtil.openModal(event, "/view/Check-out.fxml", "Check-out"); 
    }
    @FXML private void handleCheckIn(ActionEvent event) {
        ModalUtil.openModal(event, "/view/Check-in.fxml", "Check-in"); 
    }
    @FXML private void handleAddUnit(ActionEvent event) {
        ModalUtil.openModal(event, "/view/addAsset.fxml", "Add Unit"); loadData(); 
    }
    @FXML private void handleAddEquipment(ActionEvent event) { 
        ModalUtil.openModal(event, "/view/AddEquipment.fxml", "Add Equipment"); 
    }
    @FXML private void handleAddCategory(ActionEvent event) { 
        ModalUtil.openModal(event, "/view/AddCategory.fxml", "Add Category"); 
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
        addUnitButton.setVisible(visible);
        addUnitButton.setManaged(visible);
        addEquipmentButton.setVisible(visible);
        addEquipmentButton.setManaged(visible);
        addCategoryButton.setVisible(visible);
        addCategoryButton.setManaged(visible);
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

    private void initTable() {
        UnitTableUtil.setupColumns(idColumn, serialColumn, equipmentColumn, addedByColumn, statusColumn, assignedToColumn);
        
        MenuItem editItem = new MenuItem("Edit Unit");
        MenuItem deleteItem = new MenuItem("Delete Selected");
        editItem.setOnAction(event -> handleEditSelected());
        deleteItem.setOnAction(event -> handleDeleteSelected());
        
        UnitTableUtil.setupContextMenu(unitsTable, editItem, deleteItem);

        filteredList = new FilteredList<>(masterList, unit -> true);
        unitsTable.setItems(filteredList);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/editAsset.fxml"));
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

}