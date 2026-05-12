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
 * Controller for the Add Category Modal Screen.
 * 
 * Facilitates the addition of new asset classifications within the system. 
 * This controller provides a focused interface for defining top-level 
 * categories used to organize equipment and units.
 * 
 * - Captures and sanitizes category nomenclature through a single-entry 
 *   text interface.
 * - Implements validation logic to prevent the creation of empty or 
 *   whitespace-only category records.
 * - Bridges the UI and persistence layers by delegating the creation 
 *   of {@link Category} objects to {@link CategoryHandler} and {@link CategoryDAO}.
 */
public class AddCategoryModalScreen {

    @FXML private TextField categoryNameField;

    private final CategoryHandler handler = new CategoryHandler();
    private final CategoryDAO categoryDAO = new CategoryDAOImpl();

    @FXML
    private void handleSave(ActionEvent event) {
        String name = categoryNameField.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showError("Validation Error", "Category name cannot be empty.");
            return;
        }
        Category category = new Category(0, name);
        String error = handler.addCategory(categoryDAO, category);
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