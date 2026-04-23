package dao.intfc;

import java.sql.SQLException;
import java.util.List;

import dao.model.Department;



public interface DepartmentDAO {
    void add(Department department) throws SQLException;
    void update(Department department) throws SQLException;
    void delete(int departmentId)throws SQLException;
    List<Department> findAll() throws SQLException;
    List<Department> findWithAttribute(String attribute, String value) throws SQLException;

}