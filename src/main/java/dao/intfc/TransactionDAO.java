package dao.intfc;

import java.sql.SQLException;
import java.util.List;

import dao.model.Transaction;

public interface TransactionDAO {
    void add(Transaction transaction) throws SQLException;
    void update(Transaction transaction) throws SQLException;
    void delete(int transactionId) throws SQLException;
    List<Transaction> findAll() throws SQLException;
    List<Transaction> findWithAttribute(String attribute, String value) throws SQLException;
    List<Transaction> findByEmployee(int empId) throws SQLException;
    void approveCheckout(int transactionId, int processedBy, String remarks) throws SQLException;
    void declineCheckout(int transactionId) throws SQLException;
    
} 