package ui.util;

import dao.model.Employee;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class EmployeeTableUtil {
    public static void setupColumns(
        TableColumn<Employee, Integer> idCol,
        TableColumn<Employee, String> userCol,
        TableColumn<Employee, String> nameCol,
        TableColumn<Employee, String> roleCol,
        TableColumn<Employee, Integer> deptCol
    ) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        deptCol.setCellValueFactory(new PropertyValueFactory<>("departmentId"));
    }
}