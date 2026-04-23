package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dao.intfc.EmployeeDAO;
import dao.model.Employee;
import dao.dao_util.DBUtil;
public class EmployeeDAOImpl implements EmployeeDAO {

    @Override
    public void add(Employee employee) throws SQLException {
        String query = "insert into employees(department_id, username, password, role, full_name) values (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, employee.getDepartmentId());
            ps.setString(2, employee.getUsername());
            ps.setString(3, employee.getPassword());
            ps.setString(4, employee.getRole());
            ps.setString(5, employee.getFullName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    employee.setEmpId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Employee employee) throws SQLException {
        String query = "update employees set department_id = ?, username = ?, password = ?, role = ?, full_name = ? where emp_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, employee.getDepartmentId());
            ps.setString(2, employee.getUsername());
            ps.setString(3, employee.getPassword());
            ps.setString(4, employee.getRole());
            ps.setString(5, employee.getFullName());
            ps.setInt(6, employee.getEmpId());
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("No employee found with ID: " + employee.getEmpId());
            }
        }
    }

    @Override
    public void delete(int empId) throws SQLException {
        String query = "delete from employees where emp_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, empId);
            ps.executeUpdate();
        }
    }

    @Override
    public Employee findById(int empId) throws SQLException {
        String query = "select * from employees where emp_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                        rs.getInt("emp_id"),
                        rs.getInt("department_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("full_name")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<Employee> findAll() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String query = "select * from employees";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Employee employee = new Employee(
                    rs.getInt("emp_id"),
                    rs.getInt("department_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("full_name")
                );
                employees.add(employee);
            }
        }
        return employees;
    }

    @Override
    public List<Employee> findWithAttribute(String attribute, String value) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String query;
        if (attribute.equals("full_name") || attribute.equals("role") || attribute.equals("username")) {
            query = "select * from employees where " + attribute + " like ?";
        } else {
            query = "select * from employees where " + attribute + " = ?";
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if (attribute.equals("full_name") || attribute.equals("role") || attribute.equals("username")) {
                ps.setString(1, "%" + value + "%");
            } else {
                ps.setString(1, value);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee employee = new Employee(
                        rs.getInt("emp_id"),
                        rs.getInt("department_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("full_name")
                    );
                    employees.add(employee);
                }
            }
        } catch (NumberFormatException e) {
            throw new SQLException("Numeric attribute requires a number: " + attribute, e);
        }
        return employees;
    }
    @Override
    public Employee findByUsername(String username) throws SQLException {
        String query = "select * from employees where username = ?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                        rs.getInt("emp_id"),
                        rs.getInt("department_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("full_name")
                    );
                }
            }
        }
        return null;
    }
}