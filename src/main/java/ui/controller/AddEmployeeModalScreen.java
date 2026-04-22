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

public class AddEmployeeModalScreen {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private ChoiceBox<String> departmentChoiceBox;

    private final EmployeeHandler handler = new EmployeeHandler();
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();

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

  
        Employee employee = new Employee(0, deptId, username, password, role, fullName);

        String error = handler.addEmployee(employeeDAO, employee);
        
        if (error == null) {
            
            close(event);
        } else {
           
            AlertUtil.showError("Save Failed", error);
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
}