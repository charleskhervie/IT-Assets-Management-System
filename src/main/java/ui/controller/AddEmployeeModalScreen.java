package ui.controller;

import dao.handler.EmployeeHandler;
import dao.impl.EmployeeDAOImpl;
import dao.intfc.EmployeeDAO;
import dao.model.Employee;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.util.AlertUtil;
/**
 * Controller for the Add Employee Modal Screen.
 * 
 * Manages the creation and modification of system user accounts. This controller 
 * handles the synchronization between user profile attributes and the organizational 
 * hierarchy.
 * 
 * - Populates selection components with predefined system roles and organizational 
 *   departments during initialization.
 * - Supports dual-mode functionality, serving as both a creation form for new 
 *   users and an editor for existing {@link Employee} records.
 * - Implements a mapping layer between descriptive department names and numerical 
 *   database identifiers using switch expressions.
 * - Conducts mandatory field validation to ensure the integrity of security 
 *   credentials and profile information.
 * - Interfaces with {@link EmployeeHandler} and {@link EmployeeDAO} to manage 
 *   data persistence and account updates.
 */
public class AddEmployeeModalScreen {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private ChoiceBox<String> departmentChoiceBox;

    private final EmployeeHandler handler = new EmployeeHandler();
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();
    private Employee existingEmployee;

    @FXML
    public void initialize() {
        
        roleChoiceBox.getItems().addAll("Admin", "Employee");

        
        departmentChoiceBox.getItems().addAll(
            "IT Services", 
            "Human Resources", 
            "Accounting", 
            "Marketing", 
            "Academic Affairs", 
            "Research and Development"
        );
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleChoiceBox.getValue();
        String deptName = departmentChoiceBox.getValue();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || role == null || deptName == null) {
            AlertUtil.showError("Validation Error", "All fields are required.");
            return;
        }

       
        int deptId = switch (deptName) {
            case "IT Services" -> 1;
            case "Human Resources" -> 2;
            case "Accounting" -> 3;
            case "Marketing" -> 4;
            case "Academic Affairs" -> 5;
            case "Research and Development" -> 6;
            default -> 0;
        };

  
        Employee employee = new Employee(
            existingEmployee != null ? existingEmployee.getEmpId() : 0,
            deptId,
            username,
            password,
            role,
            fullName
        );

        if (existingEmployee == null) {
            String error = handler.addEmployee(employeeDAO, employee);
            if (error == null) {
                close(event);
            } else {
                AlertUtil.showError("Save Failed", error);
            }
            return;
        }

        try {
            employeeDAO.update(employee);
            close(event);
        } catch (Exception e) {
            AlertUtil.showError("Update Failed", e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        close(event);
    }

    private void close(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void setEmployee(Employee employee) {
        this.existingEmployee = employee;

        fullNameField.setText(employee.getFullName());
        usernameField.setText(employee.getUsername());
        passwordField.setText(employee.getPassword());
        roleChoiceBox.setValue(employee.getRole());
        departmentChoiceBox.setValue(departmentNameFromId(employee.getDepartmentId()));
    }

    private String departmentNameFromId(int departmentId) {
        return switch (departmentId) {
            case 1 -> "IT Services";
            case 2 -> "Human Resources";
            case 3 -> "Accounting";
            case 4 -> "Marketing";
            case 5 -> "Academic Affairs";
            case 6 -> "Research and Development";
            default -> null;
        };
    }
}