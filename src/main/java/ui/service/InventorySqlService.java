package ui.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;
import ui.util.InventoryExportUtil;

public class InventorySqlService {

    private final InventoryExportUtil exportUtil = new InventoryExportUtil();

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