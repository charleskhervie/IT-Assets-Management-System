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
    @FXML private Button checkOutButton;
    @FXML private Button checkInButton;
    @FXML private Button addUnitButton;
    @FXML private Button navEquipmentButton;
    @FXML private Button navCategoryButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private List<Unit> currentFilteredData = new ArrayList<>();

    private static final String STATUS_ALL = "All";
    private static final String STATUS_AVAILABLE = "Available";
    private static final String STATUS_CHECKED_OUT = "Checked-out";
    private static final String STATUS_MAINTENANCE = "Maintenance";

    private final unitHandler handler = new unitHandler();
    private final ObservableList<Unit> masterList = FXCollections.observableArrayList();
    private UnitDAO unitDAO;
    
   
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
        String editStyle = "-fx-background-color: #78A1BB; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;";
        String deleteStyle = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;";
        String checkOutStyle = "-fx-background-color: #78A1BB; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;";
        String checkInStyle = "-fx-background-color: #283044; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;";

        if (AdminUtil.isAdminMode()) {
            UnitTableUtil.setupColumnsWithActions(
                (TableView<Object>) (TableView<?>) unitsTable,
                "Edit", editStyle, e -> handleEditSelected(),
                "Delete", deleteStyle, e -> handleDeleteSelected()
            );
        } else {
            UnitTableUtil.setupColumnsWithActions(
                (TableView<Object>) (TableView<?>) unitsTable,
                "Check Out", checkOutStyle, e -> handleCheckOutInline(),
                "Check In", checkInStyle, e -> handleCheckInInline()
            );
        }

        currentFilteredData = new ArrayList<>(masterList);
        updatePage();
    }
    private void handleCheckOutInline() {
        
        Unit selectedUnit = unitsTable.getSelectionModel().getSelectedItem();
        
        if (selectedUnit == null) {
            AlertUtil.showError("Selection Error", "Please select a unit to check out.");
            return;
        }
        if (selectedUnit == null || "checked-out".equalsIgnoreCase(selectedUnit.getStatus()) 
                                || "maintenance".equalsIgnoreCase(selectedUnit.getStatus())) {
            return; 
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Check-out.fxml"));
            Parent root = loader.load();

            CheckOutScreen controller = loader.getController();
            controller.setTargetUnit(selectedUnit);

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

    private void handleCheckInInline() {
        Unit selectedUnit = unitsTable.getSelectionModel().getSelectedItem();
        if (selectedUnit == null) {
            AlertUtil.showError("Selection Error", "Please select a unit to check in.");
            return;
        }
        if (selectedUnit == null || "available".equalsIgnoreCase(selectedUnit.getStatus())) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Check-in.fxml"));
            Parent root = loader.load();

            CheckInScreen controller = loader.getController();
            controller.setTargetUnit(selectedUnit);

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
    public void loadData() {
        masterList.setAll(handler.getUnitsDisplay(unitDAO));
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();
        currentFilteredData = masterList.stream()
            .filter(unit -> UnitFilter.matches(unit, status, keyword))
            .collect(java.util.stream.Collectors.toList());
        currentPage = 0;
        updatePage();
    }

    private void updatePage() {
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredData.size());
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        unitsTable.setItems(FXCollections.observableArrayList(
            currentFilteredData.subList(fromIndex, toIndex)
        ));

        if (pageLabel != null)
            pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (prevButton != null)
            prevButton.setDisable(currentPage == 0);
        if (nextButton != null)
            nextButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML private void handlePrev(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML private void handleNext(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePage();
        }
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

            loadData(); // refresh the table after adding
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not load the Add Unit screen.");
            e.printStackTrace();
        }
    }

    private void handleDeleteSelected() {
        List<Unit> selected = new ArrayList<>(unitsTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return; // ← was this returning early?

        boolean alert = AlertUtil.showConfirmation("Delete Units", 
            "Are you sure you want to delete " + selected.size() + " unit(s)?");
        if (!alert) return;

        List<String> errors = deleteUnits(selected);
        if (!errors.isEmpty()) {
            AlertUtil.showError("Some Deletions Failed", String.join("\n", errors));
        }
        loadData();
    }

    private List<String> deleteUnits(List<Unit> units) {
        List<String> errors = new ArrayList<>();
        for (Unit unit : units) {
            String error = handler.softDeleteUnit(unitDAO, unit.getUnitId());
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