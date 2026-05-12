package dao.handler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import dao.intfc.TransactionDAO;
import dao.model.Employee;
import dao.model.Transaction;
import ui.util.SessionManager;
/**
 * handler class for managing the lifecycle of asset transactions.
 * coordinates between the user interface and the {@link TransactionDAO} 
 * to handle checkouts, returns, and admin approvals.
 */
public class TransactionHandler {

    public List<Transaction> getTransactionDisplay(TransactionDAO dao) {
        try {
            return dao.findAllDisplay();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Transaction> getTransactionRaw(TransactionDAO dao) {
        try {
            return dao.findAllRaw();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String addTransaction(TransactionDAO dao, Transaction transaction) {
        if (!isInputValid(transaction)) {
            return "Invalid Unit ID.";
        }
        try {
            dao.add(transaction);
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to add transaction: " + e.getMessage();
        }
    }

    /**
     * assigns a specific unit to an employee without the standard 
     * approval workflow, typically used for direct administrative assignments.
     */
    public String assignUnit(TransactionDAO dao, Transaction transaction) {
        if (!isInputValid(transaction)) {
            return "Invalid Unit ID and Employee ID.";
        }

        try {
            dao.assignUnit(transaction.getUnitId(), transaction.getBorrower());
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to assign unit: " + e.getMessage();
        }
    }

    public String updateTransaction(TransactionDAO dao, Transaction transaction) {
        if (!isInputValid(transaction)) {
            return "Invalid Unit ID and Employee ID .";
        }
        try {
            dao.update(transaction);
            return null;
        } catch (SQLException e) {
            return "Failed to update transaction: " + e.getMessage();
        }
    }

    public String deleteTransaction(TransactionDAO dao, int transactionId) {
        if (transactionId <= 0) return "Invalid transaction ID.";
        try {
            dao.delete(transactionId);
            return null;
        } catch (SQLException e) {
            return "Failed to delete transaction: " + e.getMessage();
        }
    }

    // ensures both the unit and the borrower exist before proceeding
    private boolean isInputValid(Transaction transaction) {
        return transaction.getUnitId() > 0 && transaction.getBorrower() > 0;
    }

    /**
     * finalizes a checkout request. 
     * retrieves the admin ID from the active session to record who processed the request.
     */
    public String approveCheckout(TransactionDAO dao, int transactionId, String remarks) {
        Employee admin = SessionManager.getLoggedInEmployee();
        int processedBy = admin != null ? admin.getEmpId() : 0;
        try {
            dao.approveCheckout(transactionId, processedBy, remarks);
            return null;
        } catch (SQLException e) {
            return "Failed to approve: " + e.getMessage();
        }
    }

    public String declineCheckout(TransactionDAO dao, int transactionId) {
        try {
            dao.declineCheckout(transactionId);
            return null;
        } catch (SQLException e) {
            return "Failed to decline: " + e.getMessage();
        }
    }

    public List<Transaction> getTransactionsByEmployee(TransactionDAO dao, int empId) {
        try {
            return dao.findByEmployee(empId);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * retrieves only the units currently in the possession of a specific employee.
     */
    public List<Transaction> getCheckedOutByEmployee(TransactionDAO dao, int empId) {
        try {
            return dao.findCheckedOutByEmployee(empId);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String checkIn(TransactionDAO dao, int transactionId) {
        try {
            dao.checkIn(transactionId);
            return null;
        } catch (SQLException e) {
            return "Failed to check in: " + e.getMessage();
        }
    }

    /**
     * initiates the return process by marking a transaction as 'Pending Return'.
     * this status prevents further use until an admin approves the return.
     */
    public String pendingReturn(TransactionDAO dao, int transactionId) {
        try {
            dao.pendingReturn(transactionId);
            return null;
        } catch (SQLException e) {
            return "Failed to submit return request: " + e.getMessage();
        }
    }

    public String approveReturn(TransactionDAO dao, int transactionId, String remarks) {
        Employee admin = SessionManager.getLoggedInEmployee();
        int processedBy = admin != null ? admin.getEmpId() : 0;
        try {
            dao.approveReturn(transactionId, processedBy, remarks);
            return null;
        } catch (SQLException e) {
            return "Failed to approve return: " + e.getMessage();
        }
    }

    /**
     * cancels a pending return request and moves the unit back to 'Checked-out' status.
     * useful if a return was initiated in error.
     */
    public String revertToCheckedOut(TransactionDAO dao, int transactionId) {
        try {
            dao.revertToCheckedOut(transactionId);
            return null;
        } catch (SQLException e) {
            return "Failed to revert status: " + e.getMessage();
        }
    }
    
}