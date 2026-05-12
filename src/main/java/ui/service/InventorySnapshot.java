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
/**
 * Data record for Inventory State Snapshots.
 * 
 * Serves as a unified container for a complete, point-in-time capture of 
 * all domain entities within the asset management system.
 * 
 * - Aggregates various entity collections, including Departments, Employees, 
 *   Equipment, Units, and Transactions, into a single immutable structure.
 * - Acts as the primary data exchange object between the import/export 
 *   services and the underlying persistence layer.
 * - Provides utility methods to calculate the aggregate record count and 
 *   generate a mapping of record distributions across different sections.
 */
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
