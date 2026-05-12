package ui.util;

import dao.model.Employee;
/**
 * Utility class for Filtering Employee Data.
 * 
 * Provides centralized logic for evaluating search criteria against 
 * {@link Employee} attributes to support dynamic UI filtering.
 */
public class EmployeeFilter {

    private EmployeeFilter() {}

    public static boolean matches(Employee employee, String keyword) {
        if (keyword == null || keyword.isEmpty()) return true;
        return containsKeyword(String.valueOf(employee.getEmpId()), keyword)
            || containsKeyword(employee.getUsername(), keyword)
            || containsKeyword(employee.getFullName(), keyword)
            || containsKeyword(employee.getRole(), keyword)
            || containsKeyword(String.valueOf(employee.getDepartmentId()), keyword)
            || containsKeyword(employee.getDepartmentName(), keyword);
    }

    private static boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}