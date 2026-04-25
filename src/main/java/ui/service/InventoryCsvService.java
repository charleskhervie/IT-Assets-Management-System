package ui.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;
import ui.util.InventoryExportUtil;

public class InventoryCsvService {

    private final InventoryExportUtil exportUtil = new InventoryExportUtil();
    private final InventoryImportUtil importUtil = new InventoryImportUtil();

    public ExportSummary exportToCsv(Path targetFile) throws IOException {
        List<Unit> units = exportUtil.getUnitsRaw();
        List<Transaction> transactions = exportUtil.getTransactionsRaw();
        List<Equipment> equipments = exportUtil.getEquipments();
        List<Employee> employees = exportUtil.getEmployees();
        List<Category> categories = exportUtil.getCategories();
        List<Department> departments = exportUtil.getDepartments();

        StringBuilder csv = new StringBuilder();

        csv.append("table_name,id_1,id_2,text_1,text_2,text_3,text_4,text_5,date_1,date_2\n");
        for (Department department : departments) {
            csv.append("departments,");
            csv.append(escape(department.getDepartmentId())).append(",");
            csv.append(",");
            csv.append(escape(department.getDepartmentName())).append(",");
            csv.append(escape(department.getLocation())).append(",");
            csv.append(",");
            csv.append(",");
            csv.append(",");
            csv.append(",");
            csv.append("\n");
        }

        for (Employee employee : employees) {
            csv.append("employees,");
            csv.append(escape(employee.getEmpId())).append(",");
            csv.append(escape(employee.getDepartmentId())).append(",");
            csv.append(escape(employee.getUsername())).append(",");
            csv.append(escape(employee.getPassword())).append(",");
            csv.append(escape(employee.getRole())).append(",");
            csv.append(escape(employee.getFullName())).append(",");
            csv.append(",");
            csv.append(",");
            csv.append("\n");
        }
        
        for (Category category : categories) {
            csv.append("categories,");
            csv.append(escape(category.getCategoryId())).append(",");
            csv.append(",");
            csv.append(escape(category.getCategoryName())).append(",");
            csv.append(",");
            csv.append(",");
            csv.append(",");
            csv.append(",");
            csv.append(",");
            csv.append("\n");
        }
        for (Equipment equipment : equipments) {
            csv.append("equipment,");
            csv.append(escape(equipment.getEquipmentId())).append(",");
            csv.append(escape(equipment.getCategoryId())).append(",");
            csv.append(escape(equipment.getEquipmentName())).append(",");
            csv.append(escape(equipment.getBrand())).append(",");
            csv.append(escape(equipment.getModel())).append(",");
            csv.append(escape(equipment.getSpecifications())).append(",");
            csv.append(",");
            csv.append(",");
            csv.append("\n");
        }

        for (Unit unit : units) {
            csv.append("units,");
            csv.append(escape(unit.getUnitId())).append(",");
            csv.append(escape(unit.getEquipmentId())).append(",");
            csv.append(escape(unit.getSerialNumber())).append(",");
            csv.append(escape(unit.getStatus())).append(",");
            csv.append(escape(unit.getAddedBy())).append(",");
            csv.append(escape(unit.getAssignedTo())).append(",");
            csv.append(",");
            csv.append(escape(unit.getCreatedAt())).append(",");
            csv.append("\n");
        }


        for (Transaction transaction : transactions) {
            csv.append("transactions,");
            csv.append(escape(transaction.getTransactionId())).append(",");
            csv.append(escape(transaction.getUnitId())).append(",");
            csv.append(escape(transaction.getBorrower())).append(",");
            csv.append(escape(transaction.getProcessedBy())).append(",");
            csv.append(escape(transaction.getStatus())).append(",");
            csv.append(escape(transaction.getRemarks())).append(",");
            csv.append(",");
            csv.append(escape(transaction.getBorrowedDate())).append(",");
            csv.append(escape(transaction.getReturnDate())).append("\n");
        }

        Files.writeString(targetFile, csv.toString(), StandardCharsets.UTF_8);

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

    public ImportSummary importFromCsv(Path sourceFile, boolean skipDuplicates) throws IOException {
        ImportValidationResult validation = validateImport(sourceFile, skipDuplicates);
        try {
            return importUtil.appendAll(sourceFile, validation.snapshot());
        } catch (Exception e) {
            throw new IOException("CSV import failed: " + e.getMessage(), e);
        }
    }

    public ImportValidationResult validateImport(Path sourceFile, boolean skipDuplicates) throws IOException {
        String text = Files.readString(sourceFile, StandardCharsets.UTF_8);
        List<List<String>> rows = parseCsvRows(text);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Invalid CSV import format: file is empty.");
        }

        List<String> header = rows.get(0);
        List<String> expectedHeader = List.of("table_name", "id_1", "id_2", "text_1", "text_2", "text_3", "text_4", "text_5", "date_1", "date_2");
        if (!header.equals(expectedHeader)) {
            throw new IllegalArgumentException("Invalid CSV import format: header must be " + expectedHeader + ".");
        }

        List<Department> departments = new ArrayList<>();
        List<Employee> employees = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        List<Equipment> equipment = new ArrayList<>();
        List<Unit> units = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        List<ImportIssue> issues = new ArrayList<>();
        int totalRecords = 0;

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (isBlankRow(row)) {
                continue;
            }
            totalRecords++;
            if (row.size() != 10) {
                issues.add(new ImportIssue("row " + (i + 1), "Invalid CSV import format: row " + (i + 1) + " must have exactly 10 columns.", true));
                continue;
            }

            try {
                String table = row.get(0);
                switch (table) {
                    case "departments" -> {
                        requireBlanks(row, i + 1, 2, 5, 6, 7, 8, 9);
                        departments.add(new Department(
                                parseRequiredInt(row.get(1), "id_1", i + 1),
                                parseRequiredText(row.get(3), "text_1", i + 1),
                                parseNullableText(row.get(4))));
                    }
                    case "employees" -> {
                        requireBlanks(row, i + 1, 7, 8, 9);
                        employees.add(new Employee(
                                parseRequiredInt(row.get(1), "id_1", i + 1),
                                parseRequiredInt(row.get(2), "id_2", i + 1),
                                parseRequiredText(row.get(3), "text_1", i + 1),
                                parseRequiredText(row.get(4), "text_2", i + 1),
                                parseRequiredText(row.get(5), "text_3", i + 1),
                                parseRequiredText(row.get(6), "text_4", i + 1)));
                    }
                    case "categories" -> {
                        requireBlanks(row, i + 1, 2, 4, 5, 6, 7, 8, 9);
                        categories.add(new Category(
                                parseRequiredInt(row.get(1), "id_1", i + 1),
                                parseRequiredText(row.get(3), "text_1", i + 1)));
                    }
                    case "equipment" -> {
                        requireBlanks(row, i + 1, 7, 8, 9);
                        equipment.add(new Equipment(
                                parseRequiredInt(row.get(1), "id_1", i + 1),
                                parseRequiredText(row.get(3), "text_1", i + 1),
                                parseNullableText(row.get(4)),
                                parseNullableText(row.get(5)),
                                parseNullableText(row.get(6)),
                                parseRequiredInt(row.get(2), "id_2", i + 1)));
                    }
                    case "units" -> {
                        requireBlanks(row, i + 1, 7, 9);
                        units.add(new Unit(
                                parseRequiredInt(row.get(1), "id_1", i + 1),
                                parseRequiredInt(row.get(2), "id_2", i + 1),
                                parseRequiredText(row.get(3), "text_1", i + 1),
                                parseRequiredText(row.get(4), "text_2", i + 1),
                                parseRequiredInt(row.get(5), "text_3", i + 1),
                                parseNullableDateTime(row.get(8), "date_1", i + 1),
                                parseNullableInt(row.get(6), "text_4", i + 1)));
                    }
                    case "transactions" -> {
                        requireBlanks(row, i + 1, 7);
                        transactions.add(new Transaction(
                                parseRequiredInt(row.get(1), "id_1", i + 1),
                                parseRequiredInt(row.get(2), "id_2", i + 1),
                                parseRequiredInt(row.get(3), "text_1", i + 1),
                                parseRequiredInt(row.get(4), "text_2", i + 1),
                                parseNullableDateTime(row.get(8), "date_1", i + 1),
                                parseNullableDateTime(row.get(9), "date_2", i + 1),
                                parseRequiredText(row.get(5), "text_3", i + 1),
                                parseNullableText(row.get(6))));
                    }
                    default -> throw new IllegalArgumentException("Invalid CSV import format: unsupported table_name '" + table + "' on row " + (i + 1) + ".");
                }
            } catch (IllegalArgumentException e) {
                issues.add(new ImportIssue("row " + (i + 1), e.getMessage(), true));
            }
        }

        InventorySnapshot snapshot = new InventorySnapshot(departments, employees, categories, equipment, units, transactions);
        try {
            issues.addAll(importUtil.previewAppendIssues(snapshot));
        } catch (Exception e) {
            throw new IOException("CSV import validation failed: " + e.getMessage(), e);
        }
        return new ImportValidationResult(snapshot, totalRecords, snapshot.sectionCounts(), issues);
    }

    private List<List<String>> parseCsvRows(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        currentCell.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentCell.append(c);
                }
                continue;
            }

            if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                currentRow.add(currentCell.toString());
                currentCell.setLength(0);
            } else if (c == '\r') {
                continue;
            } else if (c == '\n') {
                currentRow.add(currentCell.toString());
                rows.add(new ArrayList<>(currentRow));
                currentRow.clear();
                currentCell.setLength(0);
            } else {
                currentCell.append(c);
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException("Invalid CSV import format: unterminated quoted value.");
        }

        if (currentCell.length() > 0 || !currentRow.isEmpty()) {
            currentRow.add(currentCell.toString());
            rows.add(new ArrayList<>(currentRow));
        }

        return rows;
    }

    private boolean isBlankRow(List<String> row) {
        for (String value : row) {
            if (!value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private void requireBlanks(List<String> row, int rowNumber, int... indexes) {
        for (int index : indexes) {
            if (!row.get(index).isBlank()) {
                throw new IllegalArgumentException("Invalid CSV import format: column " + columnName(index) + " on row " + rowNumber + " must be blank.");
            }
        }
    }

    private String columnName(int index) {
        return switch (index) {
            case 0 -> "table_name";
            case 1 -> "id_1";
            case 2 -> "id_2";
            case 3 -> "text_1";
            case 4 -> "text_2";
            case 5 -> "text_3";
            case 6 -> "text_4";
            case 7 -> "text_5";
            case 8 -> "date_1";
            case 9 -> "date_2";
            default -> "column_" + index;
        };
    }

    private int parseRequiredInt(String value, String column, int rowNumber) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid CSV import format: " + column + " on row " + rowNumber + " is required.");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid CSV import format: " + column + " on row " + rowNumber + " must be an integer.", e);
        }
    }

    private Integer parseNullableInt(String value, String column, int rowNumber) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid CSV import format: " + column + " on row " + rowNumber + " must be an integer.", e);
        }
    }

    private String parseRequiredText(String value, String column, int rowNumber) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid CSV import format: " + column + " on row " + rowNumber + " is required.");
        }
        return value;
    }

    private String parseNullableText(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private LocalDateTime parseNullableDateTime(String value, String column, int rowNumber) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid CSV import format: " + column + " on row " + rowNumber + " must be an ISO date-time.", e);
        }
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);

        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }
}