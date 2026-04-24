package ui.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;

public record InventorySnapshot(
        List<Department> departments,
        List<Employee> employees,
        List<Category> categories,
        List<Equipment> equipment,
        List<Unit> units,
        List<Transaction> transactions) {

    public int totalRecords() {
        return departments.size()
                + employees.size()
                + categories.size()
                + equipment.size()
                + units.size()
                + transactions.size();
    }

    public Map<String, Integer> sectionCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("departments", departments.size());
        counts.put("employees", employees.size());
        counts.put("categories", categories.size());
        counts.put("equipment", equipment.size());
        counts.put("units", units.size());
        counts.put("transactions", transactions.size());
        return counts;
    }
}
