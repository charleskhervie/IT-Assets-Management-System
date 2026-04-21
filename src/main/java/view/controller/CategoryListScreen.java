package view.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import dao.handler.CategoryHandler;
import dao.impl.CategoryDAOImpl;
import dao.intfc.CategoryDAO;
import dao.model.Category;
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
import view.util.CategoryFilter;
import view.util.CategoryTableUtil;
import view.util.ModalUtil;
import view.util.NavigationUtil;

public class CategoryListScreen implements Initializable {

    @FXML private TextField searchField11;
    @FXML private TableView<Category> unitsTable11;
    @FXML private TableColumn<Category, Integer> idColumn11;
    @FXML private TableColumn<Category, String> serialColumn11;
    @FXML private Button addCategoryButton;
    @FXML private Button backButton;

    private final CategoryHandler handler = new CategoryHandler();
    private final ObservableList<Category> masterList = FXCollections.observableArrayList();
    private FilteredList<Category> filteredList;
    private CategoryDAO categoryDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDAO();
        initTable();
        initFilterListeners();
        loadData();
    }

    private void initDAO() {
        if (categoryDAO == null) {
            categoryDAO = new CategoryDAOImpl();
        }
    }

    private void initTable() {
        CategoryTableUtil.setupColumns(idColumn11, serialColumn11);

        MenuItem editItem = new MenuItem("Edit Category");
        MenuItem deleteItem = new MenuItem("Delete Selected");
        editItem.setOnAction(e -> handleEditSelected());
        deleteItem.setOnAction(e -> handleDeleteSelected());

        CategoryTableUtil.setupContextMenu(unitsTable11, editItem, deleteItem);

        filteredList = new FilteredList<>(masterList, e -> true);
        unitsTable11.setItems(filteredList);
    }

    private void initFilterListeners() {
        searchField11.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField11.getText().toLowerCase().trim();
        filteredList.setPredicate(category -> CategoryFilter.matches(category, keyword));
    }

    public void loadData() {
        masterList.setAll(handler.getCategories(categoryDAO));
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        ModalUtil.openModal(event, "/view/AddCategory.fxml", "Add Category");
        loadData();
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        NavigationUtil.loadIntoDashboard(event, "/view/unitsList.fxml");
    }

    private void handleEditSelected() {
        List<Category> selected = unitsTable11.getSelectionModel().getSelectedItems();
        if (selected.size() > 1) {
            AlertUtil.showError("Edit Error", "Please select only one category to edit.");
            return;
        }
        Category category = unitsTable11.getSelectionModel().getSelectedItem();
        if (category == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/editCategory.fxml"));
            Parent root = loader.load();

            EditCategoryModalScreen controller = loader.getController();
            controller.setCategory(category);

            Stage modal = new Stage();
            modal.initOwner(unitsTable11.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle("Edit Category");
            modal.setScene(new Scene(root));
            modal.showAndWait();

            loadData();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load editCategory.fxml", e);
        }
    }

    private void handleDeleteSelected() {
        List<Category> selected = new ArrayList<>(unitsTable11.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        boolean confirmed = AlertUtil.showConfirmation("Delete Category",
                "Are you sure you want to delete " + selected.size() + " category(s)?");
        if (!confirmed) return;

        List<String> errors = deleteCategories(selected);
        if (!errors.isEmpty()) {
            AlertUtil.showError("Some Deletions Failed", String.join("\n", errors));
        }
        loadData();
    }

    private List<String> deleteCategories(List<Category> categories) {
        List<String> errors = new ArrayList<>();
        for (Category category : categories) {
            String error = handler.deleteCategory(categoryDAO, category.getCategoryId());
            if (error != null) {
                errors.add("Category " + category.getCategoryId() + ": " + error);
            }
        }
        return errors;
    }
}