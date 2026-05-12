package ui.util;

import dao.model.Category;
/**
 * Utility class for Filtering Category Data.
 * 
 * Provides centralized logic for evaluating search criteria against 
 * {@link Category} attributes to support dynamic UI filtering.
 */
public class CategoryFilter {

    private CategoryFilter() {}

    public static boolean matches(Category category, String keyword) {
        if (keyword.isEmpty()) return true;
        return containsKeyword(String.valueOf(category.getCategoryId()), keyword)
            || containsKeyword(category.getCategoryName(), keyword);
    }

    private static boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}