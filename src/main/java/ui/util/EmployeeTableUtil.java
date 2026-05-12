package ui.util;

import dao.model.Employee;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
/**
 * Utility class for Employee Table Configuration.
 * 
 * Provides centralized logic for initializing and customizing {@link TableColumn} 
 * components within an {@link Employee} data view.
 * 
 */
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
        deptCol.setCellFactory(col -> buildDepartmentCell());
    }

    private static TableCell<Employee, Integer> buildDepartmentCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                Employee emp = (Employee) getTableRow().getItem();
                String name = emp != null ? emp.getDepartmentName() : null;
                setText(name != null ? name : String.valueOf(item));
            }
        };
    }
}