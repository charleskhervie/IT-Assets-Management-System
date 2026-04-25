package ui.util;

import dao.handler.CategoryHandler;
import dao.handler.DepartmentHandler;
import dao.handler.EmployeeHandler;
import dao.handler.EquipmentHandler;
import dao.handler.TransactionHandler;
import dao.handler.unitHandler;
import dao.impl.CategoryDAOImpl;
import dao.impl.DepartmentDAOImpl;
import dao.impl.EmployeeDAOImpl;
import dao.impl.EquipmentDAOImpl;
import dao.impl.TransactionDAOImpl;
import dao.impl.UnitDAOImpl;
import dao.intfc.CategoryDAO;
import dao.intfc.DepartmentDAO;
import dao.intfc.EmployeeDAO;
import dao.intfc.EquipmentDAO;
import dao.intfc.TransactionDAO;
import dao.intfc.UnitDAO;
import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;

/*
    - This writes validated import records to the database using handlers and DAOs.
    - Called only after InventoryImportUtil has confirmed there are zero issues.
 */
public class InventoryImportWriter {

    private final CategoryHandler categoryHandler       = new CategoryHandler();
    private final DepartmentHandler departmentHandler   = new DepartmentHandler();
    private final EmployeeHandler employeeHandler       = new EmployeeHandler();
    private final EquipmentHandler equipmentHandler     = new EquipmentHandler();
    private final unitHandler unitHandler               = new unitHandler();
    private final TransactionHandler transactionHandler = new TransactionHandler();

    private final CategoryDAO categoryDAO       = new CategoryDAOImpl();
    private final DepartmentDAO departmentDAO   = new DepartmentDAOImpl();
    private final EmployeeDAO employeeDAO       = new EmployeeDAOImpl();
    private final EquipmentDAO equipmentDAO     = new EquipmentDAOImpl();
    private final UnitDAO unitDAO               = new UnitDAOImpl();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();

   
    public String addCategory(Category category) {
        return categoryHandler.addCategory(categoryDAO, category);
    }

    public String addDepartment(Department department) {
        try {
            departmentDAO.add(department);
            return null;
        } catch (Exception e) {
            return "Failed to add department: " + e.getMessage();
        }
    }

    public String addEmployee(Employee employee) {
        return employeeHandler.addEmployee(employeeDAO, employee);
    }

    public String addEquipment(Equipment equipment) {
        return equipmentHandler.addEquipment(equipmentDAO, equipment);
    }

    public String addUnit(Unit unit) {
        return unitHandler.addUnit(unitDAO, unit);
    }

    public String addTransaction(Transaction transaction) {
        return transactionHandler.addTransaction(transactionDAO, transaction);
    }
}