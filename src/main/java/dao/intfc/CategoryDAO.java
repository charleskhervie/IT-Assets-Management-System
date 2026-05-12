package dao.intfc;


import java.sql.SQLException;
import java.util.List;

import dao.model.Category;
/**
 * Data Access Object interface for Unit operations.
 * Defines the contract for all database interactions involving the category table.
 */
public interface CategoryDAO {
    void add(Category category) throws SQLException;
    void update(Category category) throws SQLException;
    void delete(int categoryId) throws SQLException;
    List<Category> findAll() throws SQLException;
    List<Category> findWithAttribute(String attribute, String value) throws SQLException;
    
}
