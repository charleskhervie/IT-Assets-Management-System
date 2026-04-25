package ui.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;
import ui.util.InventoryExportUtil;
import ui.util.InventoryImportUtil;

public class InventoryJsonService {

    private final InventoryExportUtil exportUtil = new InventoryExportUtil();
    private final InventoryImportUtil importUtil = new InventoryImportUtil();

    public ExportSummary exportToJson(Path targetFile) throws IOException {
        List<Category> categories = exportUtil.getCategories();
        List<Department> departments = exportUtil.getDepartments();
        List<Employee> employees = exportUtil.getEmployees();
        List<Equipment> equipments = exportUtil.getEquipments();
        List<Unit> units = exportUtil.getUnitsRaw();
        List<Transaction> transactions = exportUtil.getTransactionsRaw();

        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"departments\": ").append(buildDepartmentsJson(departments)).append(",\n");
        json.append("  \"employees\": ").append(buildEmployeesJson(employees)).append(",\n");
        json.append("  \"categories\": ").append(buildCategoriesJson(categories)).append(",\n");
        json.append("  \"equipment\": ").append(buildEquipmentJson(equipments)).append(",\n");
        json.append("  \"units\": ").append(buildUnitsJson(units)).append(",\n");
        json.append("  \"transactions\": ").append(buildTransactionsJson(transactions)).append("\n");
        json.append("}\n");

        Files.writeString(targetFile, json.toString(), StandardCharsets.UTF_8);

        int total =
                categories.size()
                + departments.size()
                + employees.size()
                + equipments.size()
                + units.size()
                + transactions.size();

        return new ExportSummary(targetFile, total);
    }

    public ImportPreviewData previewImport(Path sourceFile, boolean skipDuplicates) throws IOException {
        ImportValidationResult validation = validateImport(sourceFile, skipDuplicates);
        return validation.toPreviewData();
    }

    public ImportSummary importFromJson(Path sourceFile, boolean skipDuplicates) throws IOException {
        ImportValidationResult validation = validateImport(sourceFile, skipDuplicates);
        try {
            return importUtil.importAll(sourceFile, validation.snapshot());
        } catch (Exception e) {
            throw new IOException("JSON import failed: " + e.getMessage(), e);
        }
    }

    public ImportValidationResult validateImport(Path sourceFile, boolean skipDuplicates) throws IOException {
        String text = Files.readString(sourceFile, StandardCharsets.UTF_8);
        Object root = SimpleJsonParser.parse(text);
        if (!(root instanceof Map<?, ?> rootMapRaw)) {
            throw new IllegalArgumentException("Invalid JSON import format: expected a top-level object.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> rootMap = (Map<String, Object>) rootMapRaw;
        requireExactKeys(rootMap, "top-level JSON object",
                "departments", "employees", "categories", "equipment", "units", "transactions");

        List<Object> departmentRows = requireArray(rootMap, "departments");
        List<Object> employeeRows = requireArray(rootMap, "employees");
        List<Object> categoryRows = requireArray(rootMap, "categories");
        List<Object> equipmentRows = requireArray(rootMap, "equipment");
        List<Object> unitRows = requireArray(rootMap, "units");
        List<Object> transactionRows = requireArray(rootMap, "transactions");

        List<ImportIssue> issues = new ArrayList<>();
        List<Department> departments = parseDepartments(departmentRows, issues);
        List<Employee> employees = parseEmployees(employeeRows, issues);
        List<Category> categories = parseCategories(categoryRows, issues);
        List<Equipment> equipment = parseEquipment(equipmentRows, issues);
        List<Unit> units = parseUnits(unitRows, issues);
        List<Transaction> transactions = parseTransactions(transactionRows, issues);

        InventorySnapshot snapshot = new InventorySnapshot(departments, employees, categories, equipment, units, transactions);
        try {
            issues.addAll(importUtil.validate(snapshot));
        } catch (Exception e) {
            throw new IOException("JSON import validation failed: " + e.getMessage(), e);
        }

        Map<String, Integer> sectionCounts = new LinkedHashMap<>();
        sectionCounts.put("departments", departmentRows.size());
        sectionCounts.put("employees", employeeRows.size());
        sectionCounts.put("categories", categoryRows.size());
        sectionCounts.put("equipment", equipmentRows.size());
        sectionCounts.put("units", unitRows.size());
        sectionCounts.put("transactions", transactionRows.size());

        int totalRecords = departmentRows.size()
                + employeeRows.size()
                + categoryRows.size()
                + equipmentRows.size()
                + unitRows.size()
                + transactionRows.size();

        return new ImportValidationResult(snapshot, totalRecords, sectionCounts, issues);
    }

    private List<Department> parseDepartments(List<Object> values, List<ImportIssue> issues) {
        List<Department> departments = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            try {
                Map<String, Object> row = requireObject(values.get(i), "departments[" + i + "]");
                requireExactKeys(row, "departments[" + i + "]", "departmentId", "departmentName", "location");
                departments.add(new Department(
                        requireInt(row, "departmentId", "departments[" + i + "]"),
                        requireString(row, "departmentName", "departments[" + i + "]", false),
                        requireString(row, "location", "departments[" + i + "]", true)));
            } catch (IllegalArgumentException e) {
                issues.add(new ImportIssue("departments[" + i + "]", e.getMessage(), true));
            }
        }
        return departments;
    }

    private List<Employee> parseEmployees(List<Object> values, List<ImportIssue> issues) {
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            try {
                Map<String, Object> row = requireObject(values.get(i), "employees[" + i + "]");
                requireExactKeys(row, "employees[" + i + "]",
                        "empId", "departmentId", "username", "password", "role", "fullName");
                employees.add(new Employee(
                        requireInt(row, "empId", "employees[" + i + "]"),
                        requireInt(row, "departmentId", "employees[" + i + "]"),
                        requireString(row, "username", "employees[" + i + "]", false),
                        requireString(row, "password", "employees[" + i + "]", false),
                        requireString(row, "role", "employees[" + i + "]", false),
                        requireString(row, "fullName", "employees[" + i + "]", false)));
            } catch (IllegalArgumentException e) {
                issues.add(new ImportIssue("employees[" + i + "]", e.getMessage(), true));
            }
        }
        return employees;
    }

    private List<Category> parseCategories(List<Object> values, List<ImportIssue> issues) {
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            try {
                Map<String, Object> row = requireObject(values.get(i), "categories[" + i + "]");
                requireExactKeys(row, "categories[" + i + "]", "categoryId", "categoryName");
                categories.add(new Category(
                        requireInt(row, "categoryId", "categories[" + i + "]"),
                        requireString(row, "categoryName", "categories[" + i + "]", false)));
            } catch (IllegalArgumentException e) {
                issues.add(new ImportIssue("categories[" + i + "]", e.getMessage(), true));
            }
        }
        return categories;
    }

    private List<Equipment> parseEquipment(List<Object> values, List<ImportIssue> issues) {
        List<Equipment> equipment = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            try {
                Map<String, Object> row = requireObject(values.get(i), "equipment[" + i + "]");
                requireExactKeys(row, "equipment[" + i + "]",
                        "equipmentId", "equipmentName", "brand", "model", "specifications", "categoryId");
                equipment.add(new Equipment(
                        requireInt(row, "equipmentId", "equipment[" + i + "]"),
                        requireString(row, "equipmentName", "equipment[" + i + "]", false),
                        requireString(row, "brand", "equipment[" + i + "]", true),
                        requireString(row, "model", "equipment[" + i + "]", true),
                        requireString(row, "specifications", "equipment[" + i + "]", true),
                        requireInt(row, "categoryId", "equipment[" + i + "]")));
            } catch (IllegalArgumentException e) {
                issues.add(new ImportIssue("equipment[" + i + "]", e.getMessage(), true));
            }
        }
        return equipment;
    }

    private List<Unit> parseUnits(List<Object> values, List<ImportIssue> issues) {
        List<Unit> units = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            try {
                Map<String, Object> row = requireObject(values.get(i), "units[" + i + "]");
                requireExactKeys(row, "units[" + i + "]",
                        "unitId", "equipmentId", "serialNumber", "status", "addedBy", "createdAt", "assignedTo");
                units.add(new Unit(
                        requireInt(row, "unitId", "units[" + i + "]"),
                        requireInt(row, "equipmentId", "units[" + i + "]"),
                        requireString(row, "serialNumber", "units[" + i + "]", false),
                        requireString(row, "status", "units[" + i + "]", false),
                        requireInt(row, "addedBy", "units[" + i + "]"),
                        requireDateTime(row, "createdAt", "units[" + i + "]"),
                        requireNullableInt(row, "assignedTo", "units[" + i + "]")));
            } catch (IllegalArgumentException e) {
                issues.add(new ImportIssue("units[" + i + "]", e.getMessage(), true));
            }
        }
        return units;
    }

    private List<Transaction> parseTransactions(List<Object> values, List<ImportIssue> issues) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            try {
                Map<String, Object> row = requireObject(values.get(i), "transactions[" + i + "]");
                requireExactKeys(row, "transactions[" + i + "]",
                        "transactionId", "unitId", "borrowedBy", "processedBy", "borrowedDate", "returnDate", "remarks", "status");
                transactions.add(new Transaction(
                        requireInt(row, "transactionId", "transactions[" + i + "]"),
                        requireInt(row, "unitId", "transactions[" + i + "]"),
                        requireInt(row, "borrowedBy", "transactions[" + i + "]"),
                        requireInt(row, "processedBy", "transactions[" + i + "]"),
                        requireDateTime(row, "borrowedDate", "transactions[" + i + "]"),
                        requireDateTime(row, "returnDate", "transactions[" + i + "]"),
                        requireString(row, "status", "transactions[" + i + "]", false),
                        requireString(row, "remarks", "transactions[" + i + "]", true)));
            } catch (IllegalArgumentException e) {
                issues.add(new ImportIssue("transactions[" + i + "]", e.getMessage(), true));
            }
        }
        return transactions;
    }

    private List<Object> requireArray(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Invalid JSON import format: '" + key + "' must be an array.");
        }
        return new ArrayList<>(list);
    }

    private Map<String, Object> requireObject(Object value, String context) {
        if (!(value instanceof Map<?, ?> mapRaw)) {
            throw new IllegalArgumentException("Invalid JSON import format: " + context + " must be an object.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) mapRaw;
        return map;
    }

    private void requireExactKeys(Map<String, Object> map, String context, String... expectedKeys) {
        Set<String> expected = new LinkedHashSet<>(List.of(expectedKeys));
        Set<String> actual = new LinkedHashSet<>(map.keySet());
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException("Invalid JSON import format: " + context + " must contain exactly " + expected + ".");
        }
    }

    private int requireInt(Map<String, Object> map, String key, String context) {
        Object value = map.get(key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Invalid JSON import format: '" + key + "' in " + context + " must be a number.");
        }
        double doubleValue = number.doubleValue();
        int intValue = number.intValue();
        if (doubleValue != intValue) {
            throw new IllegalArgumentException("Invalid JSON import format: '" + key + "' in " + context + " must be an integer.");
        }
        return intValue;
    }

    private Integer requireNullableInt(Map<String, Object> map, String key, String context) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Invalid JSON import format: '" + key + "' in " + context + " must be a number or null.");
        }
        double doubleValue = number.doubleValue();
        int intValue = number.intValue();
        if (doubleValue != intValue) {
            throw new IllegalArgumentException("Invalid JSON import format: '" + key + "' in " + context + " must be an integer.");
        }
        return intValue;
    }

    private String requireString(Map<String, Object> map, String key, String context, boolean nullable) {
        Object value = map.get(key);
        if (value == null && nullable) {
            return null;
        }
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("Invalid JSON import format: '" + key + "' in " + context + " must be a string" + (nullable ? " or null." : "."));
        }
        return text;
    }

    private LocalDateTime requireDateTime(Map<String, Object> map, String key, String context) {
        String value = requireString(map, key, context, true);
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid JSON import format: '" + key + "' in " + context + " must be an ISO date-time.", e);
        }
    }

    private String buildDepartmentsJson(List<Department> departments) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < departments.size(); i++) {
            Department d = departments.get(i);

            json.append("    {\n");
            json.append("      \"departmentId\": ").append(d.getDepartmentId()).append(",\n");
            json.append("      \"departmentName\": ").append(quote(d.getDepartmentName())).append(",\n");
            json.append("      \"location\": ").append(quote(d.getLocation())).append("\n");
            json.append("    }");

            if (i < departments.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private String buildEmployeesJson(List<Employee> employees) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);

            json.append("    {\n");
            json.append("      \"empId\": ").append(e.getEmpId()).append(",\n");
            json.append("      \"departmentId\": ").append(e.getDepartmentId()).append(",\n");
            json.append("      \"username\": ").append(quote(e.getUsername())).append(",\n");
            json.append("      \"password\": ").append(quote(e.getPassword())).append(",\n");
            json.append("      \"role\": ").append(quote(e.getRole())).append(",\n");
            json.append("      \"fullName\": ").append(quote(e.getFullName())).append("\n");
            json.append("    }");

            if (i < employees.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }
    
    private String buildCategoriesJson(List<Category> categories) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);

            json.append("    {\n");
            json.append("      \"categoryId\": ").append(c.getCategoryId()).append(",\n");
            json.append("      \"categoryName\": ").append(quote(c.getCategoryName())).append("\n");
            json.append("    }");

            if (i < categories.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private String buildEquipmentJson(List<Equipment> equipments) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < equipments.size(); i++) {
            Equipment e = equipments.get(i);

            json.append("    {\n");
            json.append("      \"equipmentId\": ").append(e.getEquipmentId()).append(",\n");
            json.append("      \"equipmentName\": ").append(quote(e.getEquipmentName())).append(",\n");
            json.append("      \"brand\": ").append(quote(e.getBrand())).append(",\n");
            json.append("      \"model\": ").append(quote(e.getModel())).append(",\n");
            json.append("      \"specifications\": ").append(quote(e.getSpecifications())).append(",\n");
            json.append("      \"categoryId\": ").append(e.getCategoryId()).append("\n");
            json.append("    }");

            if (i < equipments.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private String buildUnitsJson(List<Unit> units) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < units.size(); i++) {
            Unit u = units.get(i);

            json.append("    {\n");
            json.append("      \"unitId\": ").append(u.getUnitId()).append(",\n");
            json.append("      \"equipmentId\": ").append(u.getEquipmentId()).append(",\n");
            json.append("      \"serialNumber\": ").append(quote(u.getSerialNumber())).append(",\n");
            json.append("      \"status\": ").append(quote(u.getStatus())).append(",\n");
            json.append("      \"addedBy\": ").append(u.getAddedBy()).append(",\n");
            json.append("      \"createdAt\": ").append(quote(u.getCreatedAt() == null ? null : u.getCreatedAt().toString())).append(",\n");
            json.append("      \"assignedTo\": ").append(u.getAssignedTo() == null ? "null" : u.getAssignedTo()).append("\n");
            json.append("    }");

            if (i < units.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private String buildTransactionsJson(List<Transaction> transactions) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);

            json.append("    {\n");
            json.append("      \"transactionId\": ").append(t.getTransactionId()).append(",\n");
            json.append("      \"unitId\": ").append(t.getUnitId()).append(",\n");
            json.append("      \"borrowedBy\": ").append(t.getBorrower()).append(",\n");
            json.append("      \"processedBy\": ").append(t.getProcessedBy()).append(",\n");
            json.append("      \"borrowedDate\": ").append(quote(t.getBorrowedDate() == null ? null : t.getBorrowedDate().toString())).append(",\n");
            json.append("      \"returnDate\": ").append(quote(t.getReturnDate() == null ? null : t.getReturnDate().toString())).append(",\n");
            json.append("      \"remarks\": ").append(quote(t.getRemarks())).append(",\n");
            json.append("      \"status\": ").append(quote(t.getStatus())).append("\n");
            json.append("    }");

            if (i < transactions.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }

        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}