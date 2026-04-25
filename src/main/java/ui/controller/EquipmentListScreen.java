package ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import dao.handler.EquipmentHandler;
import dao.impl.EquipmentDAOImpl;
import dao.intfc.EquipmentDAO;
import dao.model.Equipment;
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
import ui.util.EquipmentFilter;
import ui.util.EquipmentTableUtil;
import ui.util.ModalUtil;

public class EquipmentListScreen implements Initializable {

    @FXML private TextField searchField1;
    @FXML private TableView<Equipment> unitsTable1;
    @FXML private TableColumn<Equipment, Integer> idColumn1;
    @FXML private TableColumn<Equipment, String> serialColumn1;
    @FXML private TableColumn<Equipment, String> equipmentColumn1;
    @FXML private TableColumn<Equipment, String> addedByColumn1;
    @FXML private TableColumn<Equipment, String> addedByColumn11;
    @FXML private TableColumn<Equipment, Integer> statusColumn1;
    @FXML private Button prevButton1;
    @FXML private Button nextButton1;
    @FXML private Label pageLabel1;
    @FXML private Button addEquipmentButton;
    
    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private List<Equipment> currentFilteredData = new ArrayList<>();
    private final EquipmentHandler handler = new EquipmentHandler();
    private final ObservableList<Equipment> masterList = FXCollections.observableArrayList();
    private EquipmentDAO equipmentDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDAO();
        initTable();
        initFilterListeners();
        loadData();
        if (addEquipmentButton != null) {
            addEquipmentButton.setVisible(AdminUtil.isAdminMode());
            addEquipmentButton.setManaged(AdminUtil.isAdminMode());
        }
    }

    private void initDAO() {
        if (equipmentDAO == null) {
            equipmentDAO = new EquipmentDAOImpl();
        }
    }

    private void initTable() {
        if (AdminUtil.isAdminMode()) {
            EquipmentTableUtil.setupColumns(
                unitsTable1,
                idColumn1, serialColumn1, equipmentColumn1,
                addedByColumn1, addedByColumn11, statusColumn1,
                e -> handleEditSelected(),
                e -> handleDeleteSelected()
            );

            MenuItem editItem = new MenuItem("Edit Equipment");
            MenuItem deleteItem = new MenuItem("Delete Selected");
            editItem.setOnAction(e -> handleEditSelected());
            deleteItem.setOnAction(e -> handleDeleteSelected());
            EquipmentTableUtil.setupContextMenu(unitsTable1, editItem, deleteItem);
        } else {
            EquipmentTableUtil.setupColumns(
                unitsTable1,
                idColumn1, serialColumn1, equipmentColumn1,
                addedByColumn1, addedByColumn11, statusColumn1,
                null, null
            );
        }

        currentFilteredData = new ArrayList<>(masterList);
        updatePage();
    }

    private void initFilterListeners() {
        searchField1.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField1.getText().toLowerCase().trim();
        currentFilteredData = masterList.stream()
            .filter(equipment -> EquipmentFilter.matches(equipment, keyword))
            .collect(java.util.stream.Collectors.toList());
        currentPage = 0;
        updatePage();
    }

    private void updatePage() {
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredData.size());
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        unitsTable1.setItems(FXCollections.observableArrayList(
            currentFilteredData.subList(fromIndex, toIndex)
        ));

        if (pageLabel1 != null)
            pageLabel1.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (prevButton1 != null)
            prevButton1.setDisable(currentPage == 0);
        if (nextButton1 != null)
            nextButton1.setDisable(currentPage >= totalPages - 1);
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

    public void loadData() {
        masterList.setAll(handler.getEquipments(equipmentDAO));
        applyFilters();
    }

    @FXML
    private void handleAddEquipment(ActionEvent event) {
        ModalUtil.openModal(event, "/fxml/AddEquipment.fxml", "Add Equipment");
        loadData();
    }

    private void handleEditSelected() {
        List<Equipment> selected = unitsTable1.getSelectionModel().getSelectedItems();
        if (selected.size() > 1) {
            AlertUtil.showError("Edit Error", "Please select only one equipment to edit.");
            return;
        }
        Equipment equipment = unitsTable1.getSelectionModel().getSelectedItem();
        if (equipment == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editEquipment.fxml"));
            Parent root = loader.load();

            EditEquipmentModalScreen controller = loader.getController();
            controller.setEquipment(equipment);

            Stage modal = new Stage();
            modal.initOwner(unitsTable1.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Edit Equipment");
            modal.setScene(new Scene(root));
            modal.showAndWait();

            loadData();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load editEquipment.fxml", e);
        }
    }

    private void handleDeleteSelected() {
        List<Equipment> selected = new ArrayList<>(unitsTable1.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        boolean confirmed = AlertUtil.showConfirmation("Delete Equipment",
                "Are you sure you want to delete " + selected.size() + " equipment(s)?");
        if (!confirmed) return;

        List<String> errors = deleteEquipments(selected);
        if (!errors.isEmpty()) {
            AlertUtil.showError("Some Deletions Failed", String.join("\n", errors));
        }
        loadData();
    }

    private List<String> deleteEquipments(List<Equipment> equipments) {
        List<String> errors = new ArrayList<>();
        for (Equipment equipment : equipments) {
            String error = handler.deleteEquipment(equipmentDAO, equipment.getEquipmentId());
            if (error != null) {
                errors.add("Equipment " + equipment.getEquipmentId() + ": " + error);
            }
        }
        return errors;
    }
}