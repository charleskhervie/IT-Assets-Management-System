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
        String query = "insert into employee (department_id, user_name, password, role, full_name) values (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, employee.getDepartmentId());
            ps.setString(2, employee.getUserName());
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
        String query = "update employee set department_id = ?, user_name = ?, password = ?, role = ?, full_name = ? where emp_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, employee.getDepartmentId());
            ps.setString(2, employee.getUserName());
            ps.setString(3, employee.getPassword());
            ps.setString(4, employee.getRole());
            ps.setString(5, employee.getFullName());
            ps.setInt(6, employee.getEmpId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int empId) throws SQLException {
        String query = "delete from employee where emp_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, empId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Employee> findAll() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String query = "select * from employee";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Employee employee = new Employee(
                    rs.getInt("empID"),
                    rs.getInt("departmentId"),
                    rs.getString("userName"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("fullName")
                );
                employees.add(employee);
            }
        }
        return employees;
    }

    @Override
    public List<Employee> findWithAttribute(String attribute, String value) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String query = "select * from employee where " + attribute + " = ?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee employee = new Employee(
                        rs.getInt("empId"),
                        rs.getInt("departmentId"),
                        rs.getString("userName"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("fullName")
                    );
                    employees.add(employee);
                }
            }
        }
        return employees;
    }
}