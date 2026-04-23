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

public class InventoryCsvService {

    private final InventoryExportUtil exportUtil = new InventoryExportUtil();

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