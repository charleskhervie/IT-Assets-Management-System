package dao.intfc;

import java.sql.SQLException;
import java.util.List;

import dao.model.Employee;

public interface EmployeeDAO {
    void add(Employee employee) throws SQLException;
    void update(Employee employee) throws SQLException;
    void delete(int employeeId) throws SQLException;
    List<Employee> findAll() throws SQLException;
    List<Employee> findWithAttribute(String attribute, String value) throws SQLException;
    
} 
