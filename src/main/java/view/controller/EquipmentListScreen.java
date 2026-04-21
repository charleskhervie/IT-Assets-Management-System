package view.controller;

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
import view.util.AlertUtil;
import view.util.EquipmentFilter;
import view.util.EquipmentTableUtil;
import view.util.ModalUtil;
import view.util.NavigationUtil;

public class EquipmentListScreen implements Initializable {

    @FXML private TextField searchField1;
    @FXML private TableView<Equipment> unitsTable1;
    @FXML private TableColumn<Equipment, Integer> idColumn1;
    @FXML private TableColumn<Equipment, String> serialColumn1;
    @FXML private TableColumn<Equipment, String> equipmentColumn1;
    @FXML private TableColumn<Equipment, String> addedByColumn1;
    @FXML private TableColumn<Equipment, String> addedByColumn11;
    @FXML private TableColumn<Equipment, Integer> statusColumn1;
    @FXML private Button addEquipmentButton;
    @FXML private Button backButton;

    private final EquipmentHandler handler = new EquipmentHandler();
    private final ObservableList<Equipment> masterList = FXCollections.observableArrayList();
    private FilteredList<Equipment> filteredList;
    private EquipmentDAO equipmentDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDAO();
        initTable();
        initFilterListeners();
        loadData();
    }

    private void initDAO() {
        if (equipmentDAO == null) {
            equipmentDAO = new EquipmentDAOImpl();
        }
    }

    private void initTable() {
        EquipmentTableUtil.setupColumns(idColumn1, serialColumn1, equipmentColumn1,
                addedByColumn1, addedByColumn11, statusColumn1);

        MenuItem editItem = new MenuItem("Edit Equipment");
        MenuItem deleteItem = new MenuItem("Delete Selected");
        editItem.setOnAction(e -> handleEditSelected());
        deleteItem.setOnAction(e -> handleDeleteSelected());

        EquipmentTableUtil.setupContextMenu(unitsTable1, editItem, deleteItem);

        filteredList = new FilteredList<>(masterList, e -> true);
        unitsTable1.setItems(filteredList);
    }

    private void initFilterListeners() {
        searchField1.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField1.getText().toLowerCase().trim();
        filteredList.setPredicate(equipment -> EquipmentFilter.matches(equipment, keyword));
    }

    public void loadData() {
        masterList.setAll(handler.getEquipments(equipmentDAO));
    }

    @FXML
    private void handleAddEquipment(ActionEvent event) {
        ModalUtil.openModal(event, "/view/AddEquipment.fxml", "Add Equipment");
        loadData();
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        NavigationUtil.loadIntoDashboard(event, "/view/unitsList.fxml");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/editEquipment.fxml"));
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