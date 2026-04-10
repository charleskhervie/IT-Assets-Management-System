package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.intfc.CategoryDAO;
import dao.model.Category;
import dao.dao_util.DBUtil;
public class CategoryDAOImpl  implements CategoryDAO{
    
    @Override
    public void add(Category category) throws SQLException {
     String query = "insert into category (categoryName) values (?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getCategoryName());
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setCategoryId(keys.getInt(1));
                }
            }
        }
    }
     @Override
    public void update(Category category) throws SQLException {
        String query = "update category set categoryName = ? where categoryId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, category.getCategoryName());
            ps.setInt(2, category.getCategoryId());
            ps.executeUpdate();
        }
    }
    @Override
    public void delete(int categoryId) throws SQLException {
        String query = "delete from category where categoryId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, categoryId);
            ps.executeUpdate();
        }
    }
    @Override
    public List<Category> findAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "select * from category";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(
                    rs.getInt("categoryId"),
                    rs.getString("categoryName")
                ));
            }
        }
        return categories;
    }

    @Override
    public List<Category> findWithAttribute(String attribute, String value) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query;
        
        // Only checking for categoryName for the "LIKE" search
        if (attribute.equals("categoryName")) {
            query = "select * from category where " + attribute + " like ?";
        } else {
            query = "select * from category where " + attribute + " = ?";
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            if (attribute.equals("categoryName")) {
                ps.setString(1, "%" + value + "%");
            } else {
                ps.setString(1, value);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                        rs.getInt("categoryId"),
                        rs.getString("categoryName")
                    ));
                }
            }
        }
        return categories;
    }
}
