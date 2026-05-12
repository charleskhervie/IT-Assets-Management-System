package ui.controller;

import dao.handler.CategoryHandler;
import dao.impl.CategoryDAOImpl;
import dao.intfc.CategoryDAO;
import dao.model.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.util.AlertUtil;
/**
 * Controller for the Edit Category Modal Screen.
 * 
 * Provides a specialized interface for modifying inventory classifications. 
 * This controller focuses on maintaining the taxonomy of organizational assets 
 * by ensuring category labels remain accurate and non-redundant.
 * 
 * - Manages the state transfer from the parent screen to the modal by initializing 
 *   form fields with existing {@link Category} attributes.
 * - Implements UI-level validation to prevent the submission of null or empty 
 *   category names, maintaining data quality within the system.
 * - Bridges the presentation layer with the backend by delegating update 
 *   persistence to the {@link CategoryHandler} and {@link CategoryDAO}.
 * - Displays contextual feedback via {@link AlertUtil} in the event of database 
 *   constraints or execution errors.
 * - Coordinates modal dismissal by resolving the current {@link Stage} from the 
 *   triggering action event.
 */
public class EditCategoryModalScreen {

    @FXML private TextField categoryNameField;

    private final CategoryHandler handler = new CategoryHandler();
    private final CategoryDAO categoryDAO = new CategoryDAOImpl();
    private Category category;

    public void setCategory(Category category) {
        this.category = category;
        categoryNameField.setText(category.getCategoryName());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = categoryNameField.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showError("Validation Error", "Category name cannot be empty.");
            return;
        }
        category.setCategoryName(name);
        String error = handler.updateCategory(categoryDAO, category);
        if (error != null) {
            AlertUtil.showError("Save Failed", error);
            return;
        }
        close(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        close(event);
    }

    private void close(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}