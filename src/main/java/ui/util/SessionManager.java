package ui.util;

import dao.model.Employee;

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