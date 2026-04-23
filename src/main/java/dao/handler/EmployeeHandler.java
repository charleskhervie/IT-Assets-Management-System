package dao.handler;

import dao.intfc.EmployeeDAO;
import dao.model.Employee;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class EmployeeHandler {
    
    public List<Employee> getEmployees(EmployeeDAO dao) {
        try {
            return dao.findAll();
        } catch (Exception e) {
            // Log the error so you can see it in the console
            System.out.println("Database error: " + e.getMessage());
            // Return an empty list so the table just shows "No content" instead of crashing
            return Collections.emptyList();
        }
    }
    public String addEmployee(EmployeeDAO dao, Employee employee) {
        try {
            dao.add(employee);
            return null; // Success
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database Error: " + e.getMessage();
        }
    }
    public Employee getEmployeeByUsername(EmployeeDAO dao, String username) {
        try {
            return dao.findByUsername(username);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return null;
        }
    }
     

   
}
