package ui.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;
import ui.util.InventoryExportUtil;
import ui.util.InventoryImportUtil;

public class InventorySqlService {

    private final InventoryExportUtil exportUtil = new InventoryExportUtil();
    private final InventoryImportUtil importUtil = new InventoryImportUtil();

    public ExportSummary exportToSql(Path targetFile) throws IOException {
        List<Category> categories = exportUtil.getCategories();
        List<Department> departments = exportUtil.getDepartments();
        List<Employee> employees = exportUtil.getEmployees();
        List<Equipment> equipments = exportUtil.getEquipments();
        List<Unit> units = exportUtil.getUnitsRaw();
        List<Transaction> transactions = exportUtil.getTransactionsRaw();

        StringBuilder sql = new StringBuilder();

        sql.append("-- ITAMS Export\n");
        sql.append("-- Generated at: ").append(LocalDateTime.now()).append("\n\n");

        appendSchema(sql);
        sql.append("\n");
        appendCategories(sql, categories);
        sql.append("\n");
        appendDepartments(sql, departments);
        sql.append("\n");
        appendEmployees(sql, employees);
        sql.append("\n");
        appendEquipment(sql, equipments);
        sql.append("\n");
        appendUnits(sql, units);
        sql.append("\n");
        appendTransactions(sql, transactions);

        sql.append("\nSET FOREIGN_KEY_CHECKS = 1;\n");

        Files.writeString(targetFile, sql.toString(), StandardCharsets.UTF_8);

        int total = categories.size() + departments.size() + employees.size()
                + equipments.size() + units.size() + transactions.size();

        return new ExportSummary(targetFile, total);
    }

    public ImportPreviewData previewImport(Path sourceFile, boolean skipDuplicates) throws IOException {
        ImportValidationResult validation = validateImport(sourceFile, skipDuplicates);
        return validation.toPreviewData();
    }

    public ImportSummary importFromSql(Path sourceFile, boolean skipDuplicates) throws IOException {
        ImportValidationResult validation = validateImport(sourceFile, skipDuplicates);
        try {
            return importUtil.importAll(sourceFile, validation.snapshot());
        } catch (Exception e) {
            throw new IOException("SQL import failed: " + e.getMessage(), e);
        }
    }

    public ImportValidationResult validateImport(Path sourceFile, boolean skipDuplicates) throws IOException {
        SqlImportPlan plan = parseSqlImportPlan(sourceFile);
        ImportValidationResult parsed = parseSnapshotFromPlan(plan);
        List<ImportIssue> issues = new ArrayList<>(parsed.issues());
        try {
            issues.addAll(importUtil.validate(parsed.snapshot()));
        } catch (Exception e) {
            throw new IOException("SQL import validation failed: " + e.getMessage(), e);
        }
        return new ImportValidationResult(parsed.snapshot(), parsed.totalRecords(), parsed.sectionCounts(), issues);
    }

    SqlImportPlan parseSqlImportPlan(Path sourceFile) throws IOException {
        String text = Files.readString(sourceFile, StandardCharsets.UTF_8);
        

        List<String> statements = splitSqlStatements(removeCommentLines(text));
        List<String> requiredPrefixes = List.of(
                "SET FOREIGN_KEY_CHECKS = 0",
                "CREATE DATABASE IF NOT EXISTS itams_db",
                "USE itams_db",
                "CREATE TABLE IF NOT EXISTS `departments`",
                "CREATE TABLE IF NOT EXISTS `employees`",
                "CREATE TABLE IF NOT EXISTS `categories`",
                "CREATE TABLE IF NOT EXISTS `equipment`",
                "CREATE TABLE IF NOT EXISTS `units`",
                "CREATE TABLE IF NOT EXISTS `transaction`");

        if (statements.size() < requiredPrefixes.size() + 1) {
            throw new IllegalArgumentException("Invalid SQL import format: incomplete export script.");
        }

        for (int i = 0; i < requiredPrefixes.size(); i++) {
            if (!statements.get(i).startsWith(requiredPrefixes.get(i))) {
                throw new IllegalArgumentException("Invalid SQL import format: expected statement starting with '" + requiredPrefixes.get(i) + "'.");
            }
        }

        if (!statements.get(statements.size() - 1).startsWith("SET FOREIGN_KEY_CHECKS = 1")) {
            throw new IllegalArgumentException("Invalid SQL import format: missing closing foreign key statement.");
        }

        List<String> createTableStatements = new ArrayList<>();
        for (int i = 3; i <= 8; i++) {
            createTableStatements.add(statements.get(i));
        }

        Map<String, Integer> sectionCounts = new LinkedHashMap<>();
        sectionCounts.put("categories", 0);
        sectionCounts.put("departments", 0);
        sectionCounts.put("employees", 0);
        sectionCounts.put("equipment", 0);
        sectionCounts.put("units", 0);
        sectionCounts.put("transactions", 0);

        List<String> insertStatements = new ArrayList<>();
        List<String> expectedInsertPrefixes = List.of(
                "INSERT INTO categories ",
                "INSERT INTO departments ",
                "INSERT INTO employees ",
                "INSERT INTO equipment ",
                "INSERT INTO units ",
                "INSERT INTO transaction ");

        int insertIndex = 0;
        for (int i = 9; i < statements.size() - 1; i++) {
            String statement = statements.get(i);
            if (insertIndex >= expectedInsertPrefixes.size()
                    || !statement.startsWith(expectedInsertPrefixes.get(insertIndex))) {
                throw new IllegalArgumentException("Invalid SQL import format: unexpected insert statement order.");
            }
            insertStatements.add(statement);
            sectionCounts.put(sectionNameForInsert(insertIndex), countTuples(statement));
            insertIndex++;
        }

        int totalRecords = 0;
        for (Integer count : sectionCounts.values()) {
            totalRecords += count;
        }

        return new SqlImportPlan(createTableStatements, insertStatements, sectionCounts, totalRecords);
    }

    private ImportValidationResult parseSnapshotFromPlan(SqlImportPlan plan) {
        List<Category> categories = new ArrayList<>();
        List<Department> departments = new ArrayList<>();
        List<Employee> employees = new ArrayList<>();
        List<Equipment> equipment = new ArrayList<>();
        List<Unit> units = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        List<ImportIssue> issues = new ArrayList<>();

        for (String statement : plan.insertStatements()) {
            String normalized = statement.trim();
            if (normalized.startsWith("INSERT INTO categories ")) {
                int rowIndex = 0;
                for (List<String> row : parseInsertRows(normalized)) {
                    try {
                        ensureRowSize(normalized, row, 2);
                        categories.add(new Category(
                                parseRequiredInt(row.get(0), "category_id"),
                                parseNullableString(row.get(1))));
                    } catch (IllegalArgumentException e) {
                        issues.add(new ImportIssue("categories tuple " + rowIndex, e.getMessage(), true));
                    }
                    rowIndex++;
                }
            } else if (normalized.startsWith("INSERT INTO departments ")) {
                int rowIndex = 0;
                for (List<String> row : parseInsertRows(normalized)) {
                    try {
                        ensureRowSize(normalized, row, 3);
                        departments.add(new Department(
                                parseRequiredInt(row.get(0), "department_id"),
                                parseNullableString(row.get(1)),
                                parseNullableString(row.get(2))));
                    } catch (IllegalArgumentException e) {
                        issues.add(new ImportIssue("departments tuple " + rowIndex, e.getMessage(), true));
                    }
                    rowIndex++;
                }
            } else if (normalized.startsWith("INSERT INTO employees ")) {
                int rowIndex = 0;
                for (List<String> row : parseInsertRows(normalized)) {
                    try {
                        ensureRowSize(normalized, row, 6);
                        employees.add(new Employee(
                                parseRequiredInt(row.get(0), "emp_id"),
                                parseRequiredInt(row.get(1), "department_id"),
                                parseNullableString(row.get(2)),
                                parseNullableString(row.get(3)),
                                parseNullableString(row.get(4)),
                                parseNullableString(row.get(5))));
                    } catch (IllegalArgumentException e) {
                        issues.add(new ImportIssue("employees tuple " + rowIndex, e.getMessage(), true));
                    }
                    rowIndex++;
                }
            } else if (normalized.startsWith("INSERT INTO equipment ")) {
                int rowIndex = 0;
                for (List<String> row : parseInsertRows(normalized)) {
                    try {
                        ensureRowSize(normalized, row, 6);
                        equipment.add(new Equipment(
                                parseRequiredInt(row.get(0), "equipment_id"),
                                parseNullableString(row.get(1)),
                                parseNullableString(row.get(2)),
                                parseNullableString(row.get(3)),
                                parseNullableString(row.get(4)),
                                parseRequiredInt(row.get(5), "category_id")));
                    } catch (IllegalArgumentException e) {
                        issues.add(new ImportIssue("equipment tuple " + rowIndex, e.getMessage(), true));
                    }
                    rowIndex++;
                }
            } else if (normalized.startsWith("INSERT INTO units ")) {
                int rowIndex = 0;
                for (List<String> row : parseInsertRows(normalized)) {
                    try {
                        ensureRowSize(normalized, row, 7);
                        units.add(new Unit(
                                parseRequiredInt(row.get(0), "unit_id"),
                                parseRequiredInt(row.get(1), "equipment_id"),
                                parseNullableString(row.get(2)),
                                parseNullableString(row.get(3)),
                                parseRequiredInt(row.get(4), "added_by"),
                                parseNullableDateTime(row.get(5), "created_at"),
                                parseNullableInt(row.get(6), "assigned_to")));
                    } catch (IllegalArgumentException e) {
                        issues.add(new ImportIssue("units tuple " + rowIndex, e.getMessage(), true));
                    }
                    rowIndex++;
                }
            } else if (normalized.startsWith("INSERT INTO transaction ")) {
                int rowIndex = 0;
                for (List<String> row : parseInsertRows(normalized)) {
                    try {
                        ensureRowSize(normalized, row, 8);
                        transactions.add(new Transaction(
                                parseRequiredInt(row.get(0), "transaction_id"),
                                parseRequiredInt(row.get(1), "unit_id"),
                                parseNullableInt(row.get(2), "borrowed_by", 0),
                                parseNullableInt(row.get(3), "processed_by", 0),
                                parseNullableDateTime(row.get(4), "borrowed_date"),
                                parseNullableDateTime(row.get(5), "return_date"),
                                parseNullableString(row.get(6)),
                                parseNullableString(row.get(7))));
                    } catch (IllegalArgumentException e) {
                        issues.add(new ImportIssue("transactions tuple " + rowIndex, e.getMessage(), true));
                    }
                    rowIndex++;
                }
            }
        }

        InventorySnapshot snapshot = new InventorySnapshot(departments, employees, categories, equipment, units, transactions);
        return new ImportValidationResult(snapshot, plan.totalRecords(), plan.sectionCounts(), issues);
    }

    private String removeCommentLines(String text) {
        StringBuilder builder = new StringBuilder();
        String[] lines = text.split("\\R");
        for (String line : lines) {
            if (!line.trim().startsWith("--")) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    private List<String> splitSqlStatements(String text) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'') {
                current.append(c);
                if (inQuote && i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                    current.append(text.charAt(i + 1));
                    i++;
                } else {
                    inQuote = !inQuote;
                }
                continue;
            }

            if (c == ';' && !inQuote) {
                String statement = current.toString().trim();
                if (!statement.isBlank()) {
                    statements.add(statement);
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        String trailing = current.toString().trim();
        if (!trailing.isBlank()) {
            statements.add(trailing);
        }

        return statements;
    }

    private int countTuples(String insertStatement) {
        int valuesIndex = insertStatement.indexOf("VALUES");
        if (valuesIndex < 0) {
            throw new IllegalArgumentException("Invalid SQL import format: INSERT statement missing VALUES clause.");
        }

        String valuesSection = insertStatement.substring(valuesIndex + "VALUES".length());
        boolean inQuote = false;
        int depth = 0;
        int count = 0;

        for (int i = 0; i < valuesSection.length(); i++) {
            char c = valuesSection.charAt(i);
            if (c == '\'') {
                if (inQuote && i + 1 < valuesSection.length() && valuesSection.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    inQuote = !inQuote;
                }
            } else if (!inQuote) {
                if (c == '(') {
                    if (depth == 0) {
                        count++;
                    }
                    depth++;
                } else if (c == ')') {
                    depth--;
                    if (depth < 0) {
                        throw new IllegalArgumentException("Invalid SQL import format: malformed VALUES tuples.");
                    }
                }
            }
        }

        if (depth != 0 || inQuote) {
            throw new IllegalArgumentException("Invalid SQL import format: malformed VALUES tuples.");
        }

        return count;
    }

    private String sectionNameForInsert(int insertIndex) {
        return switch (insertIndex) {
            case 0 -> "categories";
            case 1 -> "departments";
            case 2 -> "employees";
            case 3 -> "equipment";
            case 4 -> "units";
            case 5 -> "transactions";
            default -> throw new IllegalArgumentException("Unexpected insert index: " + insertIndex);
        };
    }

    private List<List<String>> parseInsertRows(String insertStatement) {
        int valuesIndex = insertStatement.indexOf("VALUES");
        if (valuesIndex < 0) {
            throw new IllegalArgumentException("Invalid SQL import format: INSERT statement missing VALUES clause.");
        }

        String valuesSection = insertStatement.substring(valuesIndex + "VALUES".length()).trim();
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = null;
        StringBuilder currentValue = new StringBuilder();
        boolean inQuote = false;
        boolean insideRow = false;

        for (int i = 0; i < valuesSection.length(); i++) {
            char c = valuesSection.charAt(i);

            if (c == '\'') {
                if (inQuote && i + 1 < valuesSection.length() && valuesSection.charAt(i + 1) == '\'') {
                    currentValue.append('\'');
                    i++;
                } else {
                    inQuote = !inQuote;
                }
                continue;
            }

            if (!inQuote) {
                if (c == '(') {
                    if (insideRow) {
                        throw new IllegalArgumentException("Invalid SQL import format: nested tuple found.");
                    }
                    insideRow = true;
                    currentRow = new ArrayList<>();
                    currentValue.setLength(0);
                    continue;
                }
                if (c == ',') {
                    if (insideRow) {
                        currentRow.add(currentValue.toString().trim());
                        currentValue.setLength(0);
                    }
                    continue;
                }
                if (c == ')') {
                    if (!insideRow || currentRow == null) {
                        throw new IllegalArgumentException("Invalid SQL import format: malformed tuple ending.");
                    }
                    currentRow.add(currentValue.toString().trim());
                    rows.add(currentRow);
                    currentRow = null;
                    currentValue.setLength(0);
                    insideRow = false;
                    continue;
                }
                if (Character.isWhitespace(c) && !insideRow) {
                    continue;
                }
            }

            if (insideRow) {
                currentValue.append(c);
            }
        }

        if (inQuote || insideRow) {
            throw new IllegalArgumentException("Invalid SQL import format: unterminated INSERT tuple.");
        }

        return rows;
    }

    private void ensureRowSize(String statement, List<String> row, int expectedSize) {
        if (row.size() != expectedSize) {
            throw new IllegalArgumentException("Invalid SQL import format: expected " + expectedSize
                    + " values for statement '" + statement + "'.");
        }
    }

    private int parseRequiredInt(String token, String columnName) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid SQL import format: " + columnName + " must be an integer.", e);
        }
    }

    private Integer parseNullableInt(String token, String columnName) {
        return parseNullableInt(token, columnName, null);
    }

    private Integer parseNullableInt(String token, String columnName, Integer defaultValue) {
        if (token == null || token.equalsIgnoreCase("NULL") || token.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid SQL import format: " + columnName + " must be an integer or NULL.", e);
        }
    }

    private String parseNullableString(String token) {
        if (token == null || token.equalsIgnoreCase("NULL")) {
            return null;
        }
        return token;
    }

    private LocalDateTime parseNullableDateTime(String token, String columnName) {
        if (token == null || token.equalsIgnoreCase("NULL") || token.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(token.replace(' ', 'T'));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid SQL import format: " + columnName + " must be a valid date-time.", e);
        }
    }

    record SqlImportPlan(
            List<String> createTableStatements,
            List<String> insertStatements,
            Map<String, Integer> sectionCounts,
            int totalRecords) {
    }

    private void appendSchema(StringBuilder sql) {
        sql.append("-- ============================================================\n");
        sql.append("-- SCHEMA\n");
        sql.append("-- ============================================================\n\n");

        sql.append("SET FOREIGN_KEY_CHECKS = 0;\n\n");

        sql.append("CREATE DATABASE IF NOT EXISTS itams_db;\n");
        sql.append("USE itams_db;\n\n");

        sql.append("CREATE TABLE IF NOT EXISTS `departments` (\n");
        sql.append("  `department_id` int NOT NULL AUTO_INCREMENT,\n");
        sql.append("  `department_name` varchar(100) NOT NULL,\n");
        sql.append("  `location` varchar(100) DEFAULT NULL,\n");
        sql.append("  PRIMARY KEY (`department_id`)\n");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n\n");

        sql.append("CREATE TABLE IF NOT EXISTS `employees` (\n");
        sql.append("  `emp_id` int NOT NULL AUTO_INCREMENT,\n");
        sql.append("  `department_id` int DEFAULT NULL,\n");
        sql.append("  `username` varchar(50) NOT NULL,\n");
        sql.append("  `password` varchar(255) NOT NULL,\n");
        sql.append("  `role` varchar(20) NOT NULL,\n");
        sql.append("  `full_name` varchar(100) NOT NULL,\n");
        sql.append("  PRIMARY KEY (`emp_id`),\n");
        sql.append("  UNIQUE KEY `username` (`username`),\n");
        sql.append("  KEY `department_id` (`department_id`),\n");
        sql.append("  CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`)\n");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n\n");

        sql.append("CREATE TABLE IF NOT EXISTS `categories` (\n");
        sql.append("  `category_id` int NOT NULL AUTO_INCREMENT,\n");
        sql.append("  `category_name` varchar(50) NOT NULL,\n");
        sql.append("  PRIMARY KEY (`category_id`)\n");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n\n");

        sql.append("CREATE TABLE IF NOT EXISTS `equipment` (\n");
        sql.append("  `equipment_id` int NOT NULL AUTO_INCREMENT,\n");
        sql.append("  `equipment_name` varchar(100) NOT NULL,\n");
        sql.append("  `brand` varchar(50) DEFAULT NULL,\n");
        sql.append("  `model` varchar(50) DEFAULT NULL,\n");
        sql.append("  `specifications` text,\n");
        sql.append("  `category_id` int DEFAULT NULL,\n");
        sql.append("  PRIMARY KEY (`equipment_id`),\n");
        sql.append("  KEY `category_id` (`category_id`),\n");
        sql.append("  CONSTRAINT `equipment_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)\n");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n\n");

        sql.append("CREATE TABLE IF NOT EXISTS `units` (\n");
        sql.append("  `unit_id` int NOT NULL AUTO_INCREMENT,\n");
        sql.append("  `equipment_id` int DEFAULT NULL,\n");
        sql.append("  `serial_number` varchar(100) NOT NULL,\n");
        sql.append("  `status` varchar(20) DEFAULT 'available',\n");
        sql.append("  `added_by` int DEFAULT NULL,\n");
        sql.append("  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,\n");
        sql.append("  `assigned_to` int DEFAULT NULL,\n");
        sql.append("  PRIMARY KEY (`unit_id`),\n");
        sql.append("  UNIQUE KEY `serial_number` (`serial_number`),\n");
        sql.append("  KEY `equipment_id` (`equipment_id`),\n");
        sql.append("  KEY `added_by` (`added_by`),\n");
        sql.append("  KEY `assigned_to` (`assigned_to`),\n");
        sql.append("  CONSTRAINT `units_ibfk_1` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`equipment_id`),\n");
        sql.append("  CONSTRAINT `units_ibfk_2` FOREIGN KEY (`added_by`) REFERENCES `employees` (`emp_id`),\n");
        sql.append("  CONSTRAINT `units_ibfk_3` FOREIGN KEY (`assigned_to`) REFERENCES `employees` (`emp_id`)\n");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n\n");

        sql.append("CREATE TABLE IF NOT EXISTS `transaction` (\n");
        sql.append("  `transaction_id` int NOT NULL AUTO_INCREMENT,\n");
        sql.append("  `unit_id` int DEFAULT NULL,\n");
        sql.append("  `borrowed_by` int DEFAULT NULL,\n");
        sql.append("  `processed_by` int DEFAULT NULL,\n");
        sql.append("  `borrowed_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,\n");
        sql.append("  `return_date` timestamp NULL DEFAULT NULL,\n");
        sql.append("  `remarks` text,\n");
        sql.append("  `status` varchar(20) DEFAULT 'checked out',\n");
        sql.append("  PRIMARY KEY (`transaction_id`),\n");
        sql.append("  KEY `unit_id` (`unit_id`),\n");
        sql.append("  KEY `borrowed_by` (`borrowed_by`),\n");
        sql.append("  KEY `processed_by` (`processed_by`),\n");
        sql.append("  CONSTRAINT `transaction_ibfk_1` FOREIGN KEY (`unit_id`) REFERENCES `units` (`unit_id`),\n");
        sql.append("  CONSTRAINT `transaction_ibfk_2` FOREIGN KEY (`borrowed_by`) REFERENCES `employees` (`emp_id`),\n");
        sql.append("  CONSTRAINT `transaction_ibfk_3` FOREIGN KEY (`processed_by`) REFERENCES `employees` (`emp_id`)\n");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n\n");

        sql.append("-- ============================================================\n");
        sql.append("-- DATA\n");
        sql.append("-- ============================================================\n");
    }

    private void appendCategories(StringBuilder sql, List<Category> categories) {
        if (categories.isEmpty()) return;
        sql.append("INSERT INTO categories (category_id, category_name) VALUES\n");
        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            sql.append("  (")
               .append(c.getCategoryId()).append(", ")
               .append(sqlString(c.getCategoryName()))
               .append(")");
            sql.append(i < categories.size() - 1 ? ",\n" : ";\n");
        }
    }

    private void appendDepartments(StringBuilder sql, List<Department> departments) {
        if (departments.isEmpty()) return;
        sql.append("INSERT INTO departments (department_id, department_name, location) VALUES\n");
        for (int i = 0; i < departments.size(); i++) {
            Department d = departments.get(i);
            sql.append("  (")
               .append(d.getDepartmentId()).append(", ")
               .append(sqlString(d.getDepartmentName())).append(", ")
               .append(sqlString(d.getLocation()))
               .append(")");
            sql.append(i < departments.size() - 1 ? ",\n" : ";\n");
        }
    }

    private void appendEmployees(StringBuilder sql, List<Employee> employees) {
        if (employees.isEmpty()) return;
        sql.append("INSERT INTO employees (emp_id, department_id, username, password, role, full_name) VALUES\n");
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            sql.append("  (")
               .append(e.getEmpId()).append(", ")
               .append(e.getDepartmentId()).append(", ")
               .append(sqlString(e.getUsername())).append(", ")
               .append(sqlString(e.getPassword())).append(", ")
               .append(sqlString(e.getRole())).append(", ")
               .append(sqlString(e.getFullName()))
               .append(")");
            sql.append(i < employees.size() - 1 ? ",\n" : ";\n");
        }
    }

    private void appendEquipment(StringBuilder sql, List<Equipment> equipments) {
        if (equipments.isEmpty()) return;
        sql.append("INSERT INTO equipment (equipment_id, equipment_name, brand, model, specifications, category_id) VALUES\n");
        for (int i = 0; i < equipments.size(); i++) {
            Equipment e = equipments.get(i);
            sql.append("  (")
               .append(e.getEquipmentId()).append(", ")
               .append(sqlString(e.getEquipmentName())).append(", ")
               .append(sqlString(e.getBrand())).append(", ")
               .append(sqlString(e.getModel())).append(", ")
               .append(sqlString(e.getSpecifications())).append(", ")
               .append(e.getCategoryId())
               .append(")");
            sql.append(i < equipments.size() - 1 ? ",\n" : ";\n");
        }
    }

    private void appendUnits(StringBuilder sql, List<Unit> units) {
        if (units.isEmpty()) return;
        sql.append("INSERT INTO units (unit_id, equipment_id, serial_number, status, added_by, created_at, assigned_to) VALUES\n");
        for (int i = 0; i < units.size(); i++) {
            Unit u = units.get(i);
            sql.append("  (")
               .append(u.getUnitId()).append(", ")
               .append(u.getEquipmentId()).append(", ")
               .append(sqlString(u.getSerialNumber())).append(", ")
               .append(sqlString(u.getStatus())).append(", ")
               .append(u.getAddedBy()).append(", ")
               .append(sqlString(u.getCreatedAt() == null ? null : u.getCreatedAt().toString())).append(", ")
               .append(u.getAssignedTo() == null ? "NULL" : u.getAssignedTo())
               .append(")");
            sql.append(i < units.size() - 1 ? ",\n" : ";\n");
        }
    }

    private void appendTransactions(StringBuilder sql, List<Transaction> transactions) {
        if (transactions.isEmpty()) return;
        sql.append("INSERT INTO transaction (transaction_id, unit_id, borrowed_by, processed_by, borrowed_date, return_date, status, remarks) VALUES\n");
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            sql.append("  (")
               .append(t.getTransactionId()).append(", ")
               .append(t.getUnitId()).append(", ")
               .append(t.getBorrower() > 0 ? t.getBorrower() : "NULL").append(", ")
               .append(t.getProcessedBy() > 0 ? t.getProcessedBy() : "NULL").append(", ")
               .append(sqlString(t.getBorrowedDate() == null ? null : t.getBorrowedDate().toString())).append(", ")
               .append(sqlString(t.getReturnDate() == null ? null : t.getReturnDate().toString())).append(", ")
               .append(sqlString(t.getStatus())).append(", ")
               .append(sqlString(t.getRemarks()))
               .append(")");
            sql.append(i < transactions.size() - 1 ? ",\n" : ";\n");
        }
    }

    private String sqlString(String value) {
        if (value == null) return "NULL";
        return "'" + value.replace("'", "''") + "'";
    }
}