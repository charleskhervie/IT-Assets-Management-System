package ui.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;
import ui.util.InventoryExportUtil;

public class InventoryJsonService {

    private final InventoryExportUtil exportUtil = new InventoryExportUtil();

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