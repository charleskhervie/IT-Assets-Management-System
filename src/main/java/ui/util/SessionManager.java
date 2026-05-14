package ui.util;
import dao.model.Employee;

public class SessionManager {
    private static Employee loggedInEmployee;
    private static boolean adminMode = false; // ← add this

    private SessionManager() {}

    public static void setLoggedInEmployee(Employee employee) {
        loggedInEmployee = employee;
    }

    public static Employee getLoggedInEmployee() {
        return loggedInEmployee;
    }

    public static void setAdminMode(boolean isAdmin) { 
        adminMode = isAdmin;
    }

    public static boolean isAdminMode() { 
        return adminMode;
    }

    public static void clear() {
        loggedInEmployee = null;
        adminMode = false;
    }
}