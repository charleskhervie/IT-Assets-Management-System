package ui.util;

import java.util.List;

import dao.handler.CategoryHandler;
import dao.handler.EmployeeHandler;
import dao.handler.EquipmentHandler;
import dao.handler.TransactionHandler;
import dao.handler.DepartmentHandler;
import dao.handler.unitHandler;
import dao.impl.CategoryDAOImpl;
import dao.impl.EmployeeDAOImpl;
import dao.impl.EquipmentDAOImpl;
import dao.impl.TransactionDAOImpl;
import dao.impl.DepartmentDAOImpl;
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
/**
 * Utility class for Inventory Data Export Aggregation.
 * 
 * Acts as a centralized data access facade that consolidates various domain 
 * entities to facilitate reporting and system-wide data extraction.
 * 
 * - Integrates multiple Data Access Objects (DAOs) and Handlers to provide 
 *   a unified entry point for retrieving raw inventory records.
 * - Supports comprehensive data retrieval across units, equipment, employees, 
 *   categories, departments, and transaction histories.
 */
public class InventoryExportUtil {

    private final unitHandler unitHandler = new unitHandler();
    private final EquipmentHandler equipmentHandler = new EquipmentHandler();
    private final EmployeeHandler employeeHandler = new EmployeeHandler();
    private final CategoryHandler categoryHandler = new CategoryHandler();
    private final TransactionHandler transactionHandler = new TransactionHandler();
    private final DepartmentHandler  departmentHandler = new DepartmentHandler();

    private final UnitDAO unitDAO = new UnitDAOImpl(); // Use Interface
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();
    private final CategoryDAO categoryDAO = new CategoryDAOImpl();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private final DepartmentDAO departmentDAO = new DepartmentDAOImpl();
    
    public List<Unit> getUnitsRaw() {
        return unitHandler.getUnitsRaw(unitDAO);
    }


    public List<Equipment> getEquipments() {
        return equipmentHandler.getEquipments(equipmentDAO);
    }

    public List<Employee> getEmployees() {
        return employeeHandler.getEmployees(employeeDAO);
    }

    public List<Category> getCategories() {
        return categoryHandler.getCategories(categoryDAO);
    }

    public List<Transaction> getTransactionsRaw() {
        return transactionHandler.getTransactionRaw(transactionDAO);
    }
    
    public List<Department> getDepartments() {
        return departmentHandler.getDepartments(departmentDAO);
    }
}