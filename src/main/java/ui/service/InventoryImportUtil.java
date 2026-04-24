package ui.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dao.dao_util.DBUtil;
import dao.handler.CategoryHandler;
import dao.handler.DepartmentHandler;
import dao.handler.EmployeeHandler;
import dao.handler.EquipmentHandler;
import dao.handler.TransactionHandler;
import dao.handler.unitHandler;
import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;

public class InventoryImportUtil {

    // Handlers are used for validation logic only.
    // Actual DB writes stay in this class to preserve the single-transaction guarantee.
    private final CategoryHandler categoryHandler = new CategoryHandler();
    private final DepartmentHandler departmentHandler = new DepartmentHandler();
    private final EmployeeHandler employeeHandler = new EmployeeHandler();
    private final EquipmentHandler equipmentHandler = new EquipmentHandler();
    private final unitHandler unitHandlerInst = new unitHandler();
    private final TransactionHandler transactionHandler = new TransactionHandler();

    public ImportSummary appendAll(java.nio.file.Path sourceFile, InventorySnapshot snapshot)
            throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                AppendStats stats = appendSnapshot(conn, snapshot);
                conn.commit();
                return new ImportSummary(sourceFile, stats.importedCount, stats.skippedCount, stats.issueCount);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public List<ImportIssue> previewAppendIssues(InventorySnapshot snapshot)
            throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            return analyzeAppendIssues(conn, snapshot);
        }
    }

    private AppendStats appendSnapshot(Connection conn, InventorySnapshot snapshot) throws SQLException {
        Map<Integer, Integer> categoryIdMap = new HashMap<>();
        Map<Integer, Integer> departmentIdMap = new HashMap<>();
        Map<Integer, Integer> employeeIdMap = new HashMap<>();
        Map<Integer, Integer> equipmentIdMap = new HashMap<>();
        Map<Integer, Integer> unitIdMap = new HashMap<>();

        Map<String, Integer> categoriesByName = loadCategoriesByName(conn);
        Map<String, Integer> departmentsByKey = loadDepartmentsByKey(conn);
        Map<String, Integer> employeesByUsername = loadEmployeesByUsername(conn);
        Map<String, Integer> equipmentByKey = loadEquipmentByKey(conn);
        Map<String, Integer> unitsBySerial = loadUnitsBySerial(conn);
        Set<String> transactionKeys = loadTransactionKeys(conn);
        AppendStats stats = new AppendStats();

        for (Category category : snapshot.categories()) {
            String key = categoryKey(category.getCategoryName());
            Integer existingId = categoriesByName.get(key);
            if (existingId != null) {
                // Duplicate — map to existing ID and silently skip
                categoryIdMap.put(category.getCategoryId(), existingId);
                stats.skippedCount++;
                continue;
            }
            if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            int newId = insertCategory(conn, category);
            categoryIdMap.put(category.getCategoryId(), newId);
            categoriesByName.put(key, newId);
            stats.importedCount++;
        }

        for (Department department : snapshot.departments()) {
            String key = departmentKey(department.getDepartmentName(), department.getLocation());
            Integer existingId = departmentsByKey.get(key);
            if (existingId != null) {
                departmentIdMap.put(department.getDepartmentId(), existingId);
                stats.skippedCount++;
                continue;
            }
            if (!isDepartmentValid(department)) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            int newId = insertDepartment(conn, department);
            departmentIdMap.put(department.getDepartmentId(), newId);
            departmentsByKey.put(key, newId);
            stats.importedCount++;
        }

        for (Employee employee : snapshot.employees()) {
            Integer mappedDepartmentId = departmentIdMap.get(employee.getDepartmentId());
            if (mappedDepartmentId == null) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            Integer existingId = employeesByUsername.get(employee.getUsername());
            if (existingId != null) {
                employeeIdMap.put(employee.getEmpId(), existingId);
                stats.skippedCount++;
                continue;
            }
            if (!isEmployeeValid(employee)) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            int newId = insertEmployee(conn, employee, mappedDepartmentId);
            employeeIdMap.put(employee.getEmpId(), newId);
            employeesByUsername.put(employee.getUsername(), newId);
            stats.importedCount++;
        }

        for (Equipment equipment : snapshot.equipment()) {
            Integer mappedCategoryId = categoryIdMap.get(equipment.getCategoryId());
            if (mappedCategoryId == null) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            String key = equipmentKey(
                    equipment.getEquipmentName(),
                    equipment.getBrand(),
                    equipment.getModel(),
                    equipment.getSpecifications(),
                    mappedCategoryId);
            Integer existingId = equipmentByKey.get(key);
            if (existingId != null) {
                equipmentIdMap.put(equipment.getEquipmentId(), existingId);
                stats.skippedCount++;
                continue;
            }
            if (!isEquipmentValid(equipment)) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            int newId = insertEquipment(conn, equipment, mappedCategoryId);
            equipmentIdMap.put(equipment.getEquipmentId(), newId);
            equipmentByKey.put(key, newId);
            stats.importedCount++;
        }

        for (Unit unit : snapshot.units()) {
            Integer mappedEquipmentId = equipmentIdMap.get(unit.getEquipmentId());
            Integer mappedAddedBy = employeeIdMap.get(unit.getAddedBy());
            Integer mappedAssignedTo = unit.getAssignedTo() == null
                    ? null
                    : employeeIdMap.get(unit.getAssignedTo());
            if (mappedEquipmentId == null || mappedAddedBy == null
                    || (unit.getAssignedTo() != null && mappedAssignedTo == null)) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            Integer existingId = unitsBySerial.get(unit.getSerialNumber());
            if (existingId != null) {
                unitIdMap.put(unit.getUnitId(), existingId);
                stats.skippedCount++;
                continue;
            }
            if (!isUnitValid(unit)) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            int newId = insertUnit(conn, unit, mappedEquipmentId, mappedAddedBy, mappedAssignedTo);
            unitIdMap.put(unit.getUnitId(), newId);
            unitsBySerial.put(unit.getSerialNumber(), newId);
            stats.importedCount++;
        }

        for (Transaction transaction : snapshot.transactions()) {
            Integer mappedUnitId = unitIdMap.get(transaction.getUnitId());
            Integer mappedBorrower = transaction.getBorrower() > 0
                    ? employeeIdMap.get(transaction.getBorrower())
                    : null;
            Integer mappedProcessedBy = transaction.getProcessedBy() > 0
                    ? employeeIdMap.get(transaction.getProcessedBy())
                    : null;
            if (mappedUnitId == null
                    || (transaction.getBorrower() > 0 && mappedBorrower == null)
                    || (transaction.getProcessedBy() > 0 && mappedProcessedBy == null)) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            String key = transactionKey(
                    mappedUnitId,
                    mappedBorrower,
                    mappedProcessedBy,
                    transaction.getBorrowedDate(),
                    transaction.getReturnDate(),
                    transaction.getStatus(),
                    transaction.getRemarks());
            if (transactionKeys.contains(key)) {
                stats.skippedCount++;
                continue;
            }
            if (!isTransactionValid(transaction)) {
                stats.skippedCount++;
                stats.issueCount++;
                continue;
            }
            insertTransaction(conn, transaction, mappedUnitId, mappedBorrower, mappedProcessedBy);
            transactionKeys.add(key);
            stats.importedCount++;
        }

        return stats;
    }

    // -------------------------------------------------------------------------
    // Handler-based validation helpers
    // These mirror the isInputValid checks in each handler so validation runs
    // inside the transaction without needing a live DAO call.
    // -------------------------------------------------------------------------

    private boolean isDepartmentValid(Department department) {
        return department.getDepartmentName() != null
                && !department.getDepartmentName().isBlank();
    }

    private boolean isEmployeeValid(Employee employee) {
        return employee.getUsername() != null
                && !employee.getUsername().isBlank();
    }

    private boolean isEquipmentValid(Equipment equipment) {
        return equipment.getEquipmentName() != null
                && !equipment.getEquipmentName().isBlank()
                && equipment.getCategoryId() > 0;
    }

    private boolean isUnitValid(Unit unit) {
        return unit.getEquipmentId() > 0
                && unit.getSerialNumber() != null
                && !unit.getSerialNumber().isBlank();
    }

    private boolean isTransactionValid(Transaction transaction) {
        return transaction.getUnitId() > 0 && transaction.getBorrower() > 0;
    }

    // -------------------------------------------------------------------------
    // Preview / analysis (read-only, no writes)
    // -------------------------------------------------------------------------

    private List<ImportIssue> analyzeAppendIssues(Connection conn, InventorySnapshot snapshot)
            throws SQLException {
        List<ImportIssue> issues = new ArrayList<>();
        Map<Integer, Integer> categoryIdMap = new HashMap<>();
        Map<Integer, Integer> departmentIdMap = new HashMap<>();
        Map<Integer, Integer> employeeIdMap = new HashMap<>();
        Map<Integer, Integer> equipmentIdMap = new HashMap<>();
        Map<Integer, Integer> unitIdMap = new HashMap<>();
        int syntheticId = -1;

        Map<String, Integer> categoriesByName = loadCategoriesByName(conn);
        Map<String, Integer> departmentsByKey = loadDepartmentsByKey(conn);
        Map<String, Integer> employeesByUsername = loadEmployeesByUsername(conn);
        Map<String, Integer> equipmentByKey = loadEquipmentByKey(conn);
        Map<String, Integer> unitsBySerial = loadUnitsBySerial(conn);
        Set<String> transactionKeys = loadTransactionKeys(conn);

        for (Category category : snapshot.categories()) {
            String key = categoryKey(category.getCategoryName());
            Integer existingId = categoriesByName.get(key);
            if (existingId != null) {
                categoryIdMap.put(category.getCategoryId(), existingId);
                continue;
            }
            if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
                issues.add(new ImportIssue("category (unnamed)", "Category name is empty.", true));
                continue;
            }
            categoryIdMap.put(category.getCategoryId(), syntheticId);
            categoriesByName.put(key, syntheticId);
            syntheticId--;
        }

        for (Department department : snapshot.departments()) {
            String key = departmentKey(department.getDepartmentName(), department.getLocation());
            Integer existingId = departmentsByKey.get(key);
            if (existingId != null) {
                departmentIdMap.put(department.getDepartmentId(), existingId);
                continue;
            }
            if (!isDepartmentValid(department)) {
                issues.add(new ImportIssue("department (unnamed)", "Department name is empty.", true));
                continue;
            }
            departmentIdMap.put(department.getDepartmentId(), syntheticId);
            departmentsByKey.put(key, syntheticId);
            syntheticId--;
        }

        for (Employee employee : snapshot.employees()) {
            Integer mappedDepartmentId = departmentIdMap.get(employee.getDepartmentId());
            if (mappedDepartmentId == null) {
                issues.add(new ImportIssue(
                        "employee " + employee.getUsername(),
                        "Missing department reference for employee.", true));
                continue;
            }
            Integer existingId = employeesByUsername.get(employee.getUsername());
            if (existingId != null) {
                employeeIdMap.put(employee.getEmpId(), existingId);
                issues.add(new ImportIssue(
                        "employee " + normalize(employee.getUsername()),
                        "Duplicate username already exists.", true));
                continue;
            }
            if (!isEmployeeValid(employee)) {
                issues.add(new ImportIssue("employee (unnamed)", "Employee username is empty.", true));
                continue;
            }
            employeeIdMap.put(employee.getEmpId(), syntheticId);
            employeesByUsername.put(employee.getUsername(), syntheticId);
            syntheticId--;
        }

        for (Equipment equipment : snapshot.equipment()) {
            Integer mappedCategoryId = categoryIdMap.get(equipment.getCategoryId());
            if (mappedCategoryId == null) {
                issues.add(new ImportIssue(
                        "equipment " + equipment.getEquipmentName(),
                        "Missing category reference for equipment.", true));
                continue;
            }
            String key = equipmentKey(
                    equipment.getEquipmentName(),
                    equipment.getBrand(),
                    equipment.getModel(),
                    equipment.getSpecifications(),
                    mappedCategoryId);
            Integer existingId = equipmentByKey.get(key);
            if (existingId != null) {
                equipmentIdMap.put(equipment.getEquipmentId(), existingId);
                issues.add(new ImportIssue(
                        "equipment " + normalize(equipment.getEquipmentName()),
                        "Duplicate equipment already exists.", true));
                continue;
            }
            if (!isEquipmentValid(equipment)) {
                issues.add(new ImportIssue(
                        "equipment (unnamed)",
                        "Equipment name is empty or category ID is invalid.", true));
                continue;
            }
            equipmentIdMap.put(equipment.getEquipmentId(), syntheticId);
            equipmentByKey.put(key, syntheticId);
            syntheticId--;
        }

        for (Unit unit : snapshot.units()) {
            Integer mappedEquipmentId = equipmentIdMap.get(unit.getEquipmentId());
            Integer mappedAddedBy = employeeIdMap.get(unit.getAddedBy());
            Integer mappedAssignedTo = unit.getAssignedTo() == null
                    ? null
                    : employeeIdMap.get(unit.getAssignedTo());
            if (mappedEquipmentId == null || mappedAddedBy == null
                    || (unit.getAssignedTo() != null && mappedAssignedTo == null)) {
                issues.add(new ImportIssue(
                        "unit " + unit.getSerialNumber(),
                        "Missing reference data for unit.", true));
                continue;
            }
            Integer existingId = unitsBySerial.get(unit.getSerialNumber());
            if (existingId != null) {
                unitIdMap.put(unit.getUnitId(), existingId);
                issues.add(new ImportIssue(
                        "unit " + normalize(unit.getSerialNumber()),
                        "Duplicate serial number already exists.", true));
                continue;
            }
            if (!isUnitValid(unit)) {
                issues.add(new ImportIssue(
                        "unit (no serial)",
                        "Unit serial number is empty or equipment ID is invalid.", true));
                continue;
            }
            unitIdMap.put(unit.getUnitId(), syntheticId);
            unitsBySerial.put(unit.getSerialNumber(), syntheticId);
            syntheticId--;
        }

        for (Transaction transaction : snapshot.transactions()) {
            Integer mappedUnitId = unitIdMap.get(transaction.getUnitId());
            Integer mappedBorrower = transaction.getBorrower() > 0
                    ? employeeIdMap.get(transaction.getBorrower())
                    : null;
            Integer mappedProcessedBy = transaction.getProcessedBy() > 0
                    ? employeeIdMap.get(transaction.getProcessedBy())
                    : null;
            if (mappedUnitId == null
                    || (transaction.getBorrower() > 0 && mappedBorrower == null)
                    || (transaction.getProcessedBy() > 0 && mappedProcessedBy == null)) {
                issues.add(new ImportIssue(
                        "transaction " + transaction.getTransactionId(),
                        "Missing reference data for transaction.", true));
                continue;
            }
            String key = transactionKey(
                    mappedUnitId,
                    mappedBorrower,
                    mappedProcessedBy,
                    transaction.getBorrowedDate(),
                    transaction.getReturnDate(),
                    transaction.getStatus(),
                    transaction.getRemarks());
            if (transactionKeys.contains(key)) {
                issues.add(new ImportIssue(
                        "transaction " + transaction.getTransactionId(),
                        "Duplicate transaction already exists.", true));
                continue;
            }
            if (!isTransactionValid(transaction)) {
                issues.add(new ImportIssue(
                        "transaction " + transaction.getTransactionId(),
                        "Transaction unit ID or borrower is invalid.", true));
                continue;
            }
            transactionKeys.add(key);
        }

        return issues;
    }

    // -------------------------------------------------------------------------
    // DB load helpers
    // -------------------------------------------------------------------------

    private Map<String, Integer> loadCategoriesByName(Connection conn) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT category_id, category_name FROM categories");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(categoryKey(rs.getString("category_name")), rs.getInt("category_id"));
            }
        }
        return result;
    }

    private Map<String, Integer> loadDepartmentsByKey(Connection conn) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT department_id, department_name, location FROM departments");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(
                        departmentKey(rs.getString("department_name"), rs.getString("location")),
                        rs.getInt("department_id"));
            }
        }
        return result;
    }

    private Map<String, Integer> loadEmployeesByUsername(Connection conn) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT emp_id, username FROM employees");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("username"), rs.getInt("emp_id"));
            }
        }
        return result;
    }

    private Map<String, Integer> loadEquipmentByKey(Connection conn) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        String sql = "SELECT equipment_id, equipment_name, brand, model, specifications, category_id FROM equipment";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(
                        equipmentKey(
                                rs.getString("equipment_name"),
                                rs.getString("brand"),
                                rs.getString("model"),
                                rs.getString("specifications"),
                                rs.getInt("category_id")),
                        rs.getInt("equipment_id"));
            }
        }
        return result;
    }

    private Map<String, Integer> loadUnitsBySerial(Connection conn) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT unit_id, serial_number FROM units");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("serial_number"), rs.getInt("unit_id"));
            }
        }
        return result;
    }

    private Set<String> loadTransactionKeys(Connection conn) throws SQLException {
        Set<String> result = new HashSet<>();
        String sql = "SELECT unit_id, borrowed_by, processed_by, borrowed_date, return_date, status, remarks FROM transaction";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(transactionKey(
                        rs.getInt("unit_id"),
                        rs.getObject("borrowed_by", Integer.class),
                        rs.getObject("processed_by", Integer.class),
                        rs.getObject("borrowed_date", LocalDateTime.class),
                        rs.getObject("return_date", LocalDateTime.class),
                        rs.getString("status"),
                        rs.getString("remarks")));
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Raw SQL insert helpers (kept here to preserve single-transaction atomicity)
    // -------------------------------------------------------------------------

    private int insertCategory(Connection conn, Category category) throws SQLException {
        String sql = "INSERT INTO categories (category_name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getCategoryName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Import failed: category key generation returned no ID.");
    }

    private int insertDepartment(Connection conn, Department department) throws SQLException {
        String sql = "INSERT INTO departments (department_name, location) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, department.getDepartmentName());
            ps.setString(2, department.getLocation());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Import failed: department key generation returned no ID.");
    }

    private int insertEmployee(Connection conn, Employee employee, int mappedDepartmentId) throws SQLException {
        String sql = "INSERT INTO employees (department_id, username, password, role, full_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, mappedDepartmentId);
            ps.setString(2, employee.getUsername());
            ps.setString(3, employee.getPassword());
            ps.setString(4, employee.getRole());
            ps.setString(5, employee.getFullName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Import failed: employee key generation returned no ID.");
    }

    private int insertEquipment(Connection conn, Equipment equipment, int mappedCategoryId) throws SQLException {
        String sql = "INSERT INTO equipment (equipment_name, brand, model, specifications, category_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, equipment.getEquipmentName());
            ps.setString(2, equipment.getBrand());
            ps.setString(3, equipment.getModel());
            ps.setString(4, equipment.getSpecifications());
            ps.setInt(5, mappedCategoryId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Import failed: equipment key generation returned no ID.");
    }

    private int insertUnit(Connection conn, Unit unit, int mappedEquipmentId, int mappedAddedBy,
            Integer mappedAssignedTo) throws SQLException {
        String sql = "INSERT INTO units (equipment_id, serial_number, status, added_by, created_at, assigned_to) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, mappedEquipmentId);
            ps.setString(2, unit.getSerialNumber());
            ps.setString(3, unit.getStatus());
            ps.setInt(4, mappedAddedBy);
            ps.setTimestamp(5, unit.getCreatedAt() == null ? null : Timestamp.valueOf(unit.getCreatedAt()));
            if (mappedAssignedTo == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, mappedAssignedTo);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Import failed: unit key generation returned no ID.");
    }

    private void insertTransaction(Connection conn, Transaction transaction, int mappedUnitId,
            Integer mappedBorrower, Integer mappedProcessedBy) throws SQLException {
        String sql = "INSERT INTO transaction (unit_id, borrowed_by, processed_by, borrowed_date, return_date, status, remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mappedUnitId);
            if (mappedBorrower == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, mappedBorrower);
            }
            if (mappedProcessedBy == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, mappedProcessedBy);
            }
            ps.setTimestamp(4, transaction.getBorrowedDate() == null
                    ? null : Timestamp.valueOf(transaction.getBorrowedDate()));
            ps.setTimestamp(5, transaction.getReturnDate() == null
                    ? null : Timestamp.valueOf(transaction.getReturnDate()));
            ps.setString(6, transaction.getStatus());
            ps.setString(7, transaction.getRemarks());
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Key helpers
    // -------------------------------------------------------------------------

    private String categoryKey(String categoryName) {
        return normalize(categoryName);
    }

    private String departmentKey(String departmentName, String location) {
        return normalize(departmentName) + "|" + normalize(location);
    }

    private String equipmentKey(String equipmentName, String brand, String model,
            String specifications, int categoryId) {
        return normalize(equipmentName)
                + "|" + normalize(brand)
                + "|" + normalize(model)
                + "|" + normalize(specifications)
                + "|" + categoryId;
    }

    private String transactionKey(int unitId, Integer borrowedBy, Integer processedBy,
            LocalDateTime borrowedDate, LocalDateTime returnDate, String status, String remarks) {
        return unitId
                + "|" + Objects.toString(borrowedBy, "null")
                + "|" + Objects.toString(processedBy, "null")
                + "|" + Objects.toString(borrowedDate, "null")
                + "|" + Objects.toString(returnDate, "null")
                + "|" + normalize(status)
                + "|" + normalize(remarks);
    }

    private String normalize(String value) {
        return value == null ? "null" : value.trim();
    }

    private static final class AppendStats {
        private int importedCount;
        private int skippedCount;
        private int issueCount;
    }
}
