package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.intfc.DepartmentDAO;
import dao.model.Department;
import dao.dao_util.DBUtil;


public class DepartmentDAOImpl implements DepartmentDAO {
    
    @Override
    public void add(Department department) throws SQLException {
        String query = "insert into department ( departmentName, location) values (?, ?)";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, department.getDepartmentName());
            ps.setString(2, department.getLocation());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    department.setDepartmentId(keys.getInt(1));
                }
            }
        }
    }

     @Override
    public void update (Department department) throws SQLException{
        String query = "update department set departmentName = ? , location = ? where departmentId = ?";
        try (Connection conn =  DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)){
            ps.setString(1,department.getDepartmentName());
            ps.setString(2,department.getLocation());
            ps.setInt(3, department.getDepartmentId());
            ps.executeUpdate();
            }   
    }
     @Override
    public void delete(int departmentId) throws SQLException{
        String query = "delete from department where departmentId = ?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1,departmentId);
            ps.executeUpdate();
            }
    }
     @Override
    public List <Department> findAll() throws SQLException{
        List <Department> departments = new ArrayList<>();
        String query = "select * from department";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)){
            ResultSet rs = ps.executeQuery();

                while (rs.next()){
                    Department department = new Department(
                        rs.getInt("departmentId"),
                        rs.getString("departmentName"),
                        rs.getString("location")
                    );
                    departments.add(department);
                }
            }
        return departments;
    }
   public List<Department> findWithAttribute(String attribute, String value) throws SQLException{
        List <Department> departments = new ArrayList<>();
        String query;

        if (attribute.equals("departmentName")|| attribute.equals("location")){
            query = "select * from department where " + attribute + " like ?";
        }else {
            query = "select * from department where " + attribute + " = ?";
        }
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)){
            if (attribute.equals("departmentName") || attribute.equals("location")) {
                ps.setString(1,"%" + value + "%");
            }else{
                ps.setString(1, value);
            }
        
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()) {
                    Department department = new Department(
                        rs.getInt("departmentId"),
                        rs.getString("departmentName"),
                        rs.getString("location")
                    );
                    departments.add(department);
                }
            }
        }
        return departments;

   }



}
