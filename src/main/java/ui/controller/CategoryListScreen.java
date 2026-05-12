package ui.controller;

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
import ui.util.CategoryFilter;
import ui.util.CategoryTableUtil;
import ui.util.ModalUtil;
/**
 * Controller for the Category List Screen.
 * 
 * Manages the comprehensive display and administrative control of inventory 
 * categories. This controller implements a robust data-view architecture that 
 * balances user accessibility with administrative security.
 * 
 * - Implements a paginated {@link TableView} to efficiently navigate through 
 *   large classification sets while maintaining UI responsiveness.
 * - Enforces role-based access control by dynamically hiding or showing 
 *   administrative tools like the "Add Category" button and context menus 
 *   based on {@link AdminUtil#isAdminMode()}.
 * - Provides real-time data filtering using {@link CategoryFilter}, allowing 
 *   users to quickly isolate specific categories via the search interface.
 * - Handles complex CRUD operations, including bulk deletion with integrated 
 *   error reporting and transactional integrity verification via {@link CategoryHandler}.
 * - Facilitates modular record editing by launching the {@link EditCategoryModalScreen} 
 *   and refreshing the master data list upon completion of modal interactions.
 */
public class CategoryListScreen implements Initializable {

    @FXML private TextField searchField11;
    @FXML private TableView<Category> unitsTable11;
    @FXML private TableColumn<Category, Integer> idColumn11;
    @FXML private TableColumn<Category, String> serialColumn11;
    @FXML private Button addCategoryButton;
    @FXML private Button prevButton11;
    @FXML private Button nextButton11;
    @FXML private Label pageLabel11;

    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private List<Category> currentFilteredData = new ArrayList<>();
    private final CategoryHandler handler = new CategoryHandler();
    private final ObservableList<Category> masterList = FXCollections.observableArrayList();
    private CategoryDAO categoryDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDAO();
        initTable();
        initFilterListeners();
        loadData();
        if (addCategoryButton != null) {
            addCategoryButton.setVisible(AdminUtil.isAdminMode());
            addCategoryButton.setManaged(AdminUtil.isAdminMode());
        }
    }

    private void initDAO() {
        if (categoryDAO == null) {
            categoryDAO = new CategoryDAOImpl();
        }
    }

    private void initTable() {
        if (AdminUtil.isAdminMode()) {
            CategoryTableUtil.setupColumns(
                unitsTable11,
                idColumn11, serialColumn11,
                e -> handleEditSelected(),
                e -> handleDeleteSelected()
            );

            MenuItem editItem = new MenuItem("Edit Category");
            MenuItem deleteItem = new MenuItem("Delete Selected");
            editItem.setOnAction(e -> handleEditSelected());
            deleteItem.setOnAction(e -> handleDeleteSelected());
            CategoryTableUtil.setupContextMenu(unitsTable11, editItem, deleteItem);
        } else {
            CategoryTableUtil.setupColumns(
                unitsTable11,
                idColumn11, serialColumn11,
                null, null
            );
        }

        currentFilteredData = new ArrayList<>(masterList);
        updatePage();
    }

    private void initFilterListeners() {
        searchField11.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField11.getText().toLowerCase().trim();
        currentFilteredData = masterList.stream()
            .filter(category -> CategoryFilter.matches(category, keyword))
            .collect(java.util.stream.Collectors.toList());
        currentPage = 0;
        updatePage();
    }
    private void updatePage() {
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredData.size());
        int totalPages = (int) Math.ceil((double) currentFilteredData.size() / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        unitsTable11.setItems(FXCollections.observableArrayList(
            currentFilteredData.subList(fromIndex, toIndex)
        ));

        if (pageLabel11 != null)
            pageLabel11.setText("Page " + (currentPage + 1) + " of " + totalPages);
        if (prevButton11 != null)
            prevButton11.setDisable(currentPage == 0);
        if (nextButton11 != null)
            nextButton11.setDisable(currentPage >= totalPages - 1);
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
        masterList.setAll(handler.getCategories(categoryDAO));
        applyFilters();
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        ModalUtil.openModal(event, "/fxml/AddCategory.fxml", "Add Category");
        loadData();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editCategory.fxml"));
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