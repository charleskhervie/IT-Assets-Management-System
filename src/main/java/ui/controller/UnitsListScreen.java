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
import ui.util.UnitTableUtil;
import ui.service.TableExportService;
import javafx.stage.FileChooser;
import java.io.File;

/**
 * Controller for the Units List screen.
 *
 * Features:
 * Displays a paginated, filterable table of all units in the system.
 * Admin users see Edit and Delete action buttons per row, along with an Add Unit button.
 * Staff users see Check-out and Check-in buttons instead.
 *
 * Filtering is done client-side against a master list loaded from the database.
 */
public class UnitsListScreen implements Initializable {

    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private TableView<Unit> unitsTable;
    @FXML private TableColumn<Unit, Integer> idColumn;
    @FXML private TableColumn<Unit, String> serialColumn;
    @FXML private TableColumn<Unit, Integer> equipmentColumn;
    @FXML private TableColumn<Unit, String> categoryColumn;
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

    private static final int PAGE_SIZE = 16;
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

    /**
     * Shows or hides action buttons based on whether the current user is an admin or staff.
     * Admins see unit management buttons; staff see checkout/checkin buttons.
     */
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
        if (unitDAO == null) {
            unitDAO = new UnitDAOImpl();
        }
    }

    private void initStatusFilter() {
        statusFilter.getItems().addAll(STATUS_ALL, STATUS_AVAILABLE, STATUS_CHECKED_OUT, STATUS_MAINTENANCE);
        statusFilter.setValue(STATUS_ALL);
    }

    /**
     * Sets up table columns differently based on role.
     * Admins get inline Edit and Delete buttons per row via {@link UnitTableUtil#setupColumnsWithActions}.
     * Staff get a plain read-only table.
     */
    @SuppressWarnings("unchecked")
    private void initTable() {
        String editStyle = "-fx-background-color: #78A1BB; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;";
        String deleteStyle = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;";

        TableView<Object> table = (TableView<Object>) (TableView<?>) unitsTable;

        if (AdminUtil.isAdminMode()) {
            UnitTableUtil.setupColumnsWithActions(
                (TableView<Object>) (TableView<?>) unitsTable,
                "Edit", editStyle, e -> handleEditSelected(),
                "Delete", deleteStyle, e -> handleDeleteSelected(),
                e -> handleSetMaintenance()
            );
        } else {
            UnitTableUtil.setupColumns(table);
        }

        currentFilteredData = new ArrayList<>(masterList);
        updatePage();
    }

    public void loadData() {
        masterList.setAll(handler.getUnitsDisplay(unitDAO));
        applyFilters();
    }

    /**
     * Filters the master list by status and keyword, then resets to page 1.
     * Called whenever the search field or status dropdown changes.
     */
    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();

        currentFilteredData = masterList.stream()
            .filter(unit -> UnitFilter.matches(unit, status, keyword))
            .toList();

        currentPage = 0;
        updatePage();
    }

    /**
     * Slices {@code currentFilteredData} to the current page and updates
     * the table, page label, and prev/next button states.
     */
    private void updatePage() {
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredData.size());
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        unitsTable.getSelectionModel().clearSelection();
        unitsTable.getItems().clear();
        unitsTable.setItems(FXCollections.observableArrayList(
            currentFilteredData.subList(fromIndex, toIndex)
        ));
        unitsTable.refresh();

        if (pageLabel != null)
            pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (prevButton != null)
            prevButton.setDisable(currentPage == 0);
        if (nextButton != null)
            nextButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    private void handlePrev(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML
    private void handleNext(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePage();
        }
    }

    /** Guards against multi-selection before opening the edit modal. */
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

            loadData();
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not load the Add Unit screen.");
            e.printStackTrace();
        }
    }

    private void handleDeleteSelected() {
        List<Unit> selected = new ArrayList<>(unitsTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        boolean confirmed = AlertUtil.showConfirmation("Delete Units",
            "Are you sure you want to delete " + selected.size() + " unit(s)?");
        if (!confirmed) return;

        List<String> errors = deleteUnits(selected);
        if (!errors.isEmpty()) {
            AlertUtil.showError("Some Deletions Failed", String.join("\n", errors));
        }
        loadData();
    }

    /**
     * Performs a soft delete on each unit, collecting any errors.
     * A soft delete marks the unit as deleted without removing it from the database.
     *
     * @return list of error messages for any units that failed to delete
     */
    private List<String> deleteUnits(List<Unit> units) {
        List<String> errors = new ArrayList<>();
        for (Unit unit : units) {
            String error = handler.softDeleteUnit(unitDAO, unit.getUnitId());
            if (error != null) {
                errors.add("Unit " + unit.getUnitId() + ": " + error);
            }
        }
        return errors;
    }

    /**
     * Sets the status of selected unit(s) to 'Maintenance'.
     * Supports multi-selection.
     */
    private void handleSetMaintenance() {
        List<Unit> selected = new ArrayList<>(unitsTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        for (Unit unit : selected) {
            if (!unit.getStatus().equalsIgnoreCase("available")) {
                AlertUtil.showError(
                    "Set Maintenance Failed",
                    "One or more selected units are not Available.\n" +
                    "Please select only Available units to set as Maintenance."
                );
                return;
            }
        }

        boolean confirmed = AlertUtil.showConfirmation(
            "Set Maintenance",
            "Mark " + selected.size() + " unit(s) as 'Maintenance'?"
        );
        if (!confirmed) return;

        List<String> errors = new ArrayList<>();
        for (Unit unit : selected) {
            String error = handler.setUnitMaintenance(unitDAO, unit.getUnitId());
            if (error != null) {
                errors.add("Unit " + unit.getUnitId() + ": " + error);
            }
        }

        if (!errors.isEmpty()) {
            AlertUtil.showError("Some Updates Failed", String.join("\n", errors));
        }

        loadData();
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Exit");
        confirmAlert.setHeaderText("Exit to Login");
        confirmAlert.setContentText("Are you sure you want to exit and return to the login page?");

        Optional<ButtonType> response = confirmAlert.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) return;

        NavigationUtil.loadScene(event, "/fxml/login.fxml");
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        Unit selected = unitsTable.getSelectionModel().getSelectedItem();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Check-out.fxml"));
            Parent root = loader.load();
            CheckOutScreen controller = loader.getController();
            controller.setTargetUnit(selected);
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

    @FXML
    private void handleCheckIn(ActionEvent event) {
        Unit selected = unitsTable.getSelectionModel().getSelectedItem();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Check-in.fxml"));
            Parent root = loader.load();
            CheckInScreen controller = loader.getController();
            controller.setTargetUnit(selected);
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

    @FXML
    private void handleExportPdf(ActionEvent event) {
        if (currentFilteredData.isEmpty()) {
            AlertUtil.showError("Export Error", "No units to export. Please check your filters.");
            return;
        }

        try {
            String filterDesc = buildFilterDescription();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export Units as PDF");
            chooser.setInitialFileName("units-export.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = chooser.showSaveDialog(unitsTable.getScene().getWindow());
            if (file == null) return;

            TableExportService.exportUnitsToPdf(file.toPath(), currentFilteredData, filterDesc);
            AlertUtil.showInfo("Export Complete", "Units exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertUtil.showError("Export Failed", e.getMessage());
        }
    }

    /** Builds a human-readable description of active filters for the PDF export header. */
    private String buildFilterDescription() {
        StringBuilder desc = new StringBuilder();
        String status = statusFilter.getValue();
        String search = searchField.getText().trim();

        if (status != null && !status.equals(STATUS_ALL)) {
            desc.append("Status: ").append(status);
        }
        if (!search.isEmpty()) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("Search: ").append(search);
        }
        return desc.toString();
    }
}