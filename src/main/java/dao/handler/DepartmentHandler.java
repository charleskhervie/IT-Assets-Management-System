package dao.handler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import dao.intfc.DepartmentDAO;
import dao.model.Department;

public class DepartmentHandler {
    public List<Department> getDepartments(DepartmentDAO dao) {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
