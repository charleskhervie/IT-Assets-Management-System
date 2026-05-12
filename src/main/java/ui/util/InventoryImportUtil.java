package ui.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import ui.service.ImportIssue;
import ui.service.ImportSummary;
import ui.service.InventorySnapshot;
/**
 * Utility class for Inventory Data Import.
 * 
 * Provides centralized logic for validating and migrating external data 
 * snapshots into the application's persistent database.
 * 
 * - Generates comprehensive {@link ImportSummary} and {@link ImportIssue} 
 *   reports to provide transparency on skipped, imported, or rejected records.
 * - Utilizes specialized DAOs and handlers to bridge external {@link InventorySnapshot} 
 *   data with the internal relational schema.
 */

public class InventoryImportUtil {

    //   Handlers and DAOs for reading existing data 
    private final CategoryHandler    categoryHandler    = new CategoryHandler();
    private final DepartmentHandler  departmentHandler  = new DepartmentHandler();
    private final EmployeeHandler    employeeHandler    = new EmployeeHandler();
    private final EquipmentHandler   equipmentHandler   = new EquipmentHandler();
    private final unitHandler        unitHandler        = new unitHandler();
    private final TransactionHandler transactionHandler = new TransactionHandler();

    private final CategoryDAO    categoryDAO    = new CategoryDAOImpl();
    private final DepartmentDAO  departmentDAO  = new DepartmentDAOImpl();
    private final EmployeeDAO    employeeDAO    = new EmployeeDAOImpl();
    private final EquipmentDAO   equipmentDAO   = new EquipmentDAOImpl();
    private final UnitDAO        unitDAO        = new UnitDAOImpl();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();

    
    private final InventoryImportWriter writer = new InventoryImportWriter();

    public List<ImportIssue> validate(InventorySnapshot snapshot) {
        ExistingData existing = loadExistingData();
        return findIssues(snapshot, existing);
    }

    /*
     - This imports the snapshot ,then validate first and if any issues are found
      nothing is wrriten and the issue count is reflected in the summary
     */
    public ImportSummary importAll(Path sourceFile, InventorySnapshot snapshot) {
        ExistingData existing = loadExistingData();
        List<ImportIssue> issues = findIssues(snapshot, existing);

        if (!issues.isEmpty()) {
            return new ImportSummary(sourceFile, 0, 0, issues.size());
        }

        return writeAll(sourceFile, snapshot, existing);
    }

    // — Load existing DB records via handlers/DAOs

    private ExistingData loadExistingData() {
        Map<String, Integer> categoriesByName = new HashMap<>();
        for (Category c : categoryHandler.getCategories(categoryDAO)) {
            categoriesByName.put(normalize(c.getCategoryName()), c.getCategoryId());
        }

        Map<String, Integer> departmentsByKey = new HashMap<>();
        for (Department d : departmentHandler.getDepartments(departmentDAO)) {
            departmentsByKey.put(deptKey(d.getDepartmentName(), d.getLocation()), d.getDepartmentId());
        }

        Map<String, Integer> employeesByUsername = new HashMap<>();
        for (Employee e : employeeHandler.getEmployees(employeeDAO)) {
            employeesByUsername.put(e.getUsername(), e.getEmpId());
        }

        Map<String, Integer> equipmentByKey = new HashMap<>();
        for (Equipment eq : equipmentHandler.getEquipments(equipmentDAO)) {
            equipmentByKey.put(equipmentKey(eq), eq.getEquipmentId());
        }

        Map<String, Integer> unitsBySerial = new HashMap<>();
        for (Unit u : unitHandler.getUnitsRaw(unitDAO)) {
            unitsBySerial.put(u.getSerialNumber(), u.getUnitId());
        }

        Set<String> transactionKeys = new HashSet<>();
        for (Transaction t : transactionHandler.getTransactionRaw(transactionDAO)) {
            transactionKeys.add(transactionKey(t));
        }

        return new ExistingData(
                categoriesByName, departmentsByKey, employeesByUsername,
                equipmentByKey, unitsBySerial, transactionKeys);
    }

    //  Validate: find all issues such as (duplicates + invalid data)

    private List<ImportIssue> findIssues(InventorySnapshot snapshot, ExistingData existing) {
        List<ImportIssue> issues = new ArrayList<>();
        Map<Integer, Employee> importedEmployeesById = new HashMap<>();
        Map<Integer, Equipment> importedEquipmentById = new HashMap<>();

        for (Employee employee : snapshot.employees()) {
            importedEmployeesById.put(employee.getEmpId(), employee);
        }
        for (Equipment equipment : snapshot.equipment()) {
            importedEquipmentById.put(equipment.getEquipmentId(), equipment);
        }

        Map<String, Integer> categoriesByName    = new HashMap<>(existing.categoriesByName);
        Map<String, Integer> departmentsByKey    = new HashMap<>(existing.departmentsByKey);
        Map<String, Integer> employeesByUsername = new HashMap<>(existing.employeesByUsername);
        Map<String, Integer> equipmentByKey      = new HashMap<>(existing.equipmentByKey);
        Map<String, Integer> unitsBySerial       = new HashMap<>(existing.unitsBySerial);
        Set<String>          transactionKeys     = new HashSet<>(existing.transactionKeys);

        Map<Integer, Integer> categoryIdMap  = new HashMap<>();
        Map<Integer, Integer> departmentIdMap = new HashMap<>();
        Map<Integer, Integer> employeeIdMap  = new HashMap<>();
        Map<Integer, Integer> equipmentIdMap = new HashMap<>();
        Map<Integer, Integer> unitIdMap      = new HashMap<>();
        int syntheticId = -1;

        for (Category c : snapshot.categories()) {
            if (c.getCategoryName() == null || c.getCategoryName().isBlank()) {
                issues.add(new ImportIssue("category (unnamed)", "Category name is empty.", true));
                continue;
            }
            String key = normalize(c.getCategoryName());
            Integer resolvedCategoryId = categoriesByName.get(key);
            if (resolvedCategoryId != null) {
                categoryIdMap.put(c.getCategoryId(), resolvedCategoryId);
                continue;
            }
            categoryIdMap.put(c.getCategoryId(), syntheticId);
            categoriesByName.put(key, syntheticId--);
        }

        for (Department d : snapshot.departments()) {
            if (d.getDepartmentName() == null || d.getDepartmentName().isBlank()) {
                issues.add(new ImportIssue("department (unnamed)", "Department name is empty.", true));
                continue;
            }
            String key = deptKey(d.getDepartmentName(), d.getLocation());
            Integer resolvedDepartmentId = departmentsByKey.get(key);
            if (resolvedDepartmentId != null) {
                departmentIdMap.put(d.getDepartmentId(), resolvedDepartmentId);
                continue;
            }
            departmentIdMap.put(d.getDepartmentId(), syntheticId);
            departmentsByKey.put(key, syntheticId--);
        }

        for (Employee e : snapshot.employees()) {
            if (!departmentIdMap.containsKey(e.getDepartmentId())) {
                issues.add(new ImportIssue(
                        "employee " + e.getUsername(),
                        "Missing department reference.", true));
                continue;
            }
            if (e.getUsername() == null || e.getUsername().isBlank()) {
                issues.add(new ImportIssue("employee (unnamed)", "Username is empty.", true));
                continue;
            }
            if (employeesByUsername.containsKey(e.getUsername())) {
                issues.add(new ImportIssue(
                        "employee " + e.getUsername(),
                        "Duplicate: username already exists.", true));
                continue;
            }
            employeeIdMap.put(e.getEmpId(), syntheticId);
            employeesByUsername.put(e.getUsername(), syntheticId--);
        }

        for (Equipment eq : snapshot.equipment()) {
            Integer mappedCategoryId = categoryIdMap.get(eq.getCategoryId());
            if (mappedCategoryId == null) {
                issues.add(new ImportIssue(
                        "equipment " + eq.getEquipmentName(),
                        "Missing category reference.", true));
                continue;
            }
            if (eq.getEquipmentName() == null || eq.getEquipmentName().isBlank()) {
                issues.add(new ImportIssue("equipment (unnamed)", "Equipment name is empty.", true));
                continue;
            }
            // Resolve to the mapped category ID so the key matches what the DB would store
            Equipment resolved = new Equipment(
                    eq.getEquipmentId(), eq.getEquipmentName(),
                    eq.getBrand(), eq.getModel(), eq.getSpecifications(), mappedCategoryId);
            String key = equipmentKey(resolved);
            Integer resolvedEquipmentId = equipmentByKey.get(key);
            if (resolvedEquipmentId != null) {
                equipmentIdMap.put(eq.getEquipmentId(), resolvedEquipmentId);
                continue;
            }
            equipmentIdMap.put(eq.getEquipmentId(), syntheticId);
            equipmentByKey.put(key, syntheticId--);
        }

        for (Unit u : snapshot.units()) {
            boolean hasIssue = false;
            boolean hasDirectUnitIssue = false;
            if (u.getSerialNumber() == null || u.getSerialNumber().isBlank()) {
                issues.add(new ImportIssue("unit (no serial)", "Serial number is empty.", true));
                hasIssue = true;
                hasDirectUnitIssue = true;
            } else if (unitsBySerial.containsKey(u.getSerialNumber())) {
                issues.add(new ImportIssue(
                        "unit " + u.getSerialNumber(),
                        "Duplicate: serial number already exists.", true));
                hasIssue = true;
                hasDirectUnitIssue = true;
            }

            if (!hasDirectUnitIssue && !equipmentIdMap.containsKey(u.getEquipmentId())) {
                issues.add(new ImportIssue(
                        "unit " + unitLabel(u),
                        describeEquipmentReference(u.getEquipmentId(), importedEquipmentById), true));
                hasIssue = true;
            }

            if (!hasDirectUnitIssue && !employeeIdMap.containsKey(u.getAddedBy())) {
                issues.add(new ImportIssue(
                        "unit " + unitLabel(u),
                        describeEmployeeReference(u.getAddedBy(), "added-by employee", importedEmployeesById), true));
                hasIssue = true;
            }

            if (!hasDirectUnitIssue && u.getAssignedTo() != null && !employeeIdMap.containsKey(u.getAssignedTo())) {
                issues.add(new ImportIssue(
                        "unit " + unitLabel(u),
                        describeEmployeeReference(u.getAssignedTo(), "assigned employee", importedEmployeesById), true));
                hasIssue = true;
            }

            if (hasIssue) {
                continue;
            }
            unitIdMap.put(u.getUnitId(), syntheticId);
            unitsBySerial.put(u.getSerialNumber(), syntheticId--);
        }

        for (Transaction t : snapshot.transactions()) {
            boolean unitMissing      = !unitIdMap.containsKey(t.getUnitId());
            boolean borrowerMissing  = t.getBorrower() > 0    && !employeeIdMap.containsKey(t.getBorrower());
            boolean processedMissing = t.getProcessedBy() > 0 && !employeeIdMap.containsKey(t.getProcessedBy());
            if (unitMissing || borrowerMissing || processedMissing) {
                issues.add(new ImportIssue(
                        "transaction " + t.getTransactionId(),
                        "Missing unit or employee reference.", true));
                continue;
            }
            if (t.getUnitId() <= 0 || t.getBorrower() <= 0) {
                issues.add(new ImportIssue(
                        "transaction " + t.getTransactionId(),
                        "Unit ID and borrower must be valid.", true));
                continue;
            }
            String key = transactionKey(t);
            if (transactionKeys.contains(key)) {
                issues.add(new ImportIssue(
                        "transaction " + t.getTransactionId(),
                        "Duplicate: transaction already exists.", true));
                continue;
            }
            transactionKeys.add(key);
        }

        return issues;
    }

    // Write: called only when findIssues() returns empty----

    private ImportSummary writeAll(Path sourceFile, InventorySnapshot snapshot, ExistingData existing) {
        Map<String, Integer> categoriesByName = new HashMap<>(existing.categoriesByName);
        Map<String, Integer> departmentsByKey = new HashMap<>(existing.departmentsByKey);
        Map<String, Integer> equipmentByKey = new HashMap<>(existing.equipmentByKey);
        Map<Integer, Integer> categoryIdMap  = new HashMap<>();
        Map<Integer, Integer> departmentIdMap = new HashMap<>();
        Map<Integer, Integer> employeeIdMap  = new HashMap<>();
        Map<Integer, Integer> equipmentIdMap = new HashMap<>();
        Map<Integer, Integer> unitIdMap      = new HashMap<>();
        int importedCount = 0;
        int skippedCount = 0;
        int issueCount = 0;

        for (Category c : snapshot.categories()) {
            int oldId = c.getCategoryId();
            Integer existingCategoryId = categoriesByName.get(normalize(c.getCategoryName()));
            if (existingCategoryId != null) {
                categoryIdMap.put(oldId, existingCategoryId);
                skippedCount++;
                continue;
            }
            String error = writer.addCategory(c);           // DAO sets c.categoryId to the new DB ID and  same with other attributes
            if (error != null) { issueCount++; continue; }
            categoryIdMap.put(oldId, c.getCategoryId());
            categoriesByName.put(normalize(c.getCategoryName()), c.getCategoryId());
            importedCount++;
        }

        for (Department d : snapshot.departments()) {
            int oldId = d.getDepartmentId();
            Integer existingDepartmentId = departmentsByKey.get(deptKey(d.getDepartmentName(), d.getLocation()));
            if (existingDepartmentId != null) {
                departmentIdMap.put(oldId, existingDepartmentId);
                skippedCount++;
                continue;
            }
            String error = writer.addDepartment(d);       
            if (error != null) { issueCount++; continue; }
            departmentIdMap.put(oldId, d.getDepartmentId());
            departmentsByKey.put(deptKey(d.getDepartmentName(), d.getLocation()), d.getDepartmentId());
            importedCount++;
        }

        for (Employee e : snapshot.employees()) {
            Integer newDeptId = departmentIdMap.get(e.getDepartmentId());
            if (newDeptId == null) { issueCount++; continue; }
            e.setDepartmentId(newDeptId);
            int oldId = e.getEmpId();
            String error = writer.addEmployee(e);           
            if (error != null) { issueCount++; continue; }
            employeeIdMap.put(oldId, e.getEmpId());
            importedCount++;
        }

        for (Equipment eq : snapshot.equipment()) {
            Integer newCatId = categoryIdMap.get(eq.getCategoryId());
            if (newCatId == null) { issueCount++; continue; }
            int oldId = eq.getEquipmentId();
            eq.setCategoryId(newCatId);
            Integer existingEquipmentId = equipmentByKey.get(equipmentKey(eq));
            if (existingEquipmentId != null) {
                equipmentIdMap.put(oldId, existingEquipmentId);
                skippedCount++;
                continue;
            }
            String error = writer.addEquipment(eq);         
            if (error != null) { issueCount++; continue; }
            equipmentIdMap.put(oldId, eq.getEquipmentId());
            equipmentByKey.put(equipmentKey(eq), eq.getEquipmentId());
            importedCount++;
        }

        for (Unit u : snapshot.units()) {
            Integer newEqId       = equipmentIdMap.get(u.getEquipmentId());
            Integer newAddedBy    = employeeIdMap.get(u.getAddedBy());
            Integer newAssignedTo = u.getAssignedTo() == null ? null : employeeIdMap.get(u.getAssignedTo());
            if (newEqId == null || newAddedBy == null) { issueCount++; continue; }
            u.setEquipmentId(newEqId);
            u.setAddedBy(newAddedBy);
            u.setAssignedTo(newAssignedTo);
            int oldId = u.getUnitId();
            String error = writer.addUnit(u);               
            if (error != null) { issueCount++; continue; }
            unitIdMap.put(oldId, u.getUnitId());
            importedCount++;
        }

        for (Transaction t : snapshot.transactions()) {
            Integer newUnitId      = unitIdMap.get(t.getUnitId());
            Integer newBorrower    = t.getBorrower() > 0    ? employeeIdMap.get(t.getBorrower()) : null;
            Integer newProcessedBy = t.getProcessedBy() > 0 ? employeeIdMap.get(t.getProcessedBy()) : null;
            if (newUnitId == null) { issueCount++; continue; }
            t.setUnitId(newUnitId);
            if (newBorrower    != null) t.setBorrower(newBorrower);
            if (newProcessedBy != null) t.setProcessedBy(newProcessedBy);
            String error = writer.addTransaction(t);
            if (error != null) { issueCount++; continue; }
            importedCount++;
        }

        return new ImportSummary(sourceFile, importedCount, skippedCount, issueCount);
    }

    // Key helpers ------------------------------

    private String deptKey(String name, String location) {
        return normalize(name) + "|" + normalize(location);
    }

    private String equipmentKey(Equipment eq) {
        return normalize(eq.getEquipmentName())
                + "|" + normalize(eq.getBrand())
                + "|" + normalize(eq.getModel())
                + "|" + normalize(eq.getSpecifications())
                + "|" + eq.getCategoryId();
    }

    private String transactionKey(Transaction t) {
        return t.getUnitId()
                + "|" + t.getBorrower()
                + "|" + t.getProcessedBy()
                + "|" + Objects.toString(t.getBorrowedDate(), "null")
                + "|" + Objects.toString(t.getReturnDate(), "null")
                + "|" + normalize(t.getStatus())
                + "|" + normalize(t.getRemarks());
    }

    private String normalize(String value) {
        return value == null ? "null" : value.trim();
    }

    private String unitLabel(Unit unit) {
        return unit.getSerialNumber() == null || unit.getSerialNumber().isBlank()
                ? "(no serial)"
                : unit.getSerialNumber();
    }

    private String describeEquipmentReference(int equipmentId, Map<Integer, Equipment> importedEquipmentById) {
        Equipment equipment = importedEquipmentById.get(equipmentId);
        if (equipment != null) {
            return "References equipment '" + safeName(equipment.getEquipmentName())
                    + "', which is invalid or rejected in this import.";
        }
        return "Missing equipment reference.";
    }

    private String describeEmployeeReference(int employeeId, String role, Map<Integer, Employee> importedEmployeesById) {
        Employee employee = importedEmployeesById.get(employeeId);
        if (employee != null) {
            return "References " + role + " '" + safeName(employee.getUsername())
                    + "', which is invalid or rejected in this import.";
        }
        return "Missing " + role + " reference.";
    }

    private String safeName(String value) {
        return value == null || value.isBlank() ? "(unnamed)" : value;
    }

    // Internal data holder for existing DB state
  

    private record ExistingData(
            Map<String, Integer> categoriesByName,
            Map<String, Integer> departmentsByKey,
            Map<String, Integer> employeesByUsername,
            Map<String, Integer> equipmentByKey,
            Map<String, Integer> unitsBySerial,
            Set<String> transactionKeys) {
    }
}
