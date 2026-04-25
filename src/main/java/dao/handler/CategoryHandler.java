package dao.handler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import dao.intfc.CategoryDAO;
import dao.model.Category;

public class CategoryHandler {

    public List<Category> getCategories(CategoryDAO dao) {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String addCategory(CategoryDAO dao, Category category) {
        if (!isInputValid(category)) {
            return "Category name cannot be empty.";
        }
        try {
            dao.add(category);
            return null;
        } catch (SQLException e) {
            return "Failed to add category: " + e.getMessage();
        }
    }

    public String updateCategory(CategoryDAO dao, Category category) {
        if (!isInputValid(category)) {
            return "Category name cannot be empty.";
        }
        try {
            dao.update(category);
            return null;
        } catch (SQLException e) {
            return "Failed to update category: " + e.getMessage();
        }
    }

    public String deleteCategory(CategoryDAO dao, int categoryId) {
        if (categoryId <= 0) return "Invalid category ID.";
        try {
            dao.delete(categoryId);
            return null;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key")) {
                return "Cannot delete equipment. Delete units that reference this equipment first.";
            }
            return "Failed to delete category: " + e.getMessage();
        }
    }

    private boolean isInputValid(Category category) {
        return category.getCategoryName() != null
            && !category.getCategoryName().isBlank();
    }
}