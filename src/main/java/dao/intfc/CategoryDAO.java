package dao.intfc;


import java.sql.SQLException;
import java.util.List;

import dao.model.Category;

public interface CategoryDAO {
    void add(Category category) throws SQLException;
    void update(Category category) throws SQLException;
    void delete(int categoryId) throws SQLException;
    List<Category> findAll() throws SQLException;
    List<Category> findWithAttribute(String attribute, String value) throws SQLException;
    
}
