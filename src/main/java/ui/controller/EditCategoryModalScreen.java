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