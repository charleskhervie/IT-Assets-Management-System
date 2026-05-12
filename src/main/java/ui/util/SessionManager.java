package ui.util;

import dao.model.Employee;
/**
 * Utility class for Session Management.
 * 
 * Provides a centralized, thread-local mechanism for tracking the 
 * currently authenticated user throughout the application lifecycle.
 * 
 */
public class SessionManager {

    private static Employee loggedInEmployee;

    private SessionManager() {}

    public static void setLoggedInEmployee(Employee employee) {
        loggedInEmployee = employee;
    }

    public static Employee getLoggedInEmployee() {
        return loggedInEmployee;
    }

    public static void clear() {
        loggedInEmployee = null;
    }
}