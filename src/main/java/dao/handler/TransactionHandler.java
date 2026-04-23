package dao.handler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import dao.intfc.TransactionDAO;
import dao.model.Employee;
import dao.model.Transaction;
import ui.util.SessionManager;

public class TransactionHandler {

    public List<Transaction> getTransactions(TransactionDAO dao) {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String addTransaction(TransactionDAO dao, Transaction transaction) {
        if (!isInputValid(transaction)) {
            return "Unit ID and employee ID must be greater than 0.";
        }
        try {
            dao.add(transaction);
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to add transaction: " + e.getMessage();
        }
    }

    public String updateTransaction(TransactionDAO dao, Transaction transaction) {
        if (!isInputValid(transaction)) {
            return "Unit ID and employee ID must be greater than 0.";
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

    private boolean isInputValid(Transaction transaction) {
        return transaction.getUnitId() > 0 && transaction.getBorrower() > 0;
    }

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
    
}