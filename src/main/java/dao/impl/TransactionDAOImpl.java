package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dao.intfc.TransactionDAO;
import dao.model.Transaction;
import dao.dao_util.DBUtil;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public void add(Transaction transaction) throws SQLException {
        String query = "insert into transaction (unit_id, emp_id, processed_by, transaction_type, transaction_date, return_date, status, remarks) values (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getUnitId());
            ps.setInt(2, transaction.getEmpId());
            ps.setInt(3, transaction.getProcessedBy());
            ps.setString(4, transaction.getTransactionType());
            ps.setString(5, transaction.getTransactionDate());
            ps.setString(6, transaction.getReturnDate());
            ps.setString(7, transaction.getStatus());
            ps.setString(8, transaction.getRemarks());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setTransactionId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Transaction transaction) throws SQLException {
        String query = "update transaction set unit_id = ?, emp_id = ?, processed_by = ?, transaction_type = ?, transaction_date = ?, return_date = ?, status = ?, remarks = ? where transaction_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, transaction.getUnitId());
            ps.setInt(2, transaction.getEmpId());
            ps.setInt(3, transaction.getProcessedBy());
            ps.setString(4, transaction.getTransactionType());
            ps.setString(5, transaction.getTransactionDate());
            ps.setString(6, transaction.getReturnDate());
            ps.setString(7, transaction.getStatus());
            ps.setString(8, transaction.getRemarks());
            ps.setInt(9, transaction.getTransactionId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int transactionId) throws SQLException {
        String query = "delete from transaction where transaction_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "select * from transaction";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> findWithAttribute(String attribute, String value) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "select * from transaction where " + attribute + " = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        }
        return transactions;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("transaction_id"),
            rs.getInt("unit_id"),
            rs.getInt("emp_id"),
            rs.getInt("processed_by"),
            rs.getString("transaction_type"),
            rs.getString("transaction_date"),
            rs.getString("return_date"),
            rs.getString("status"),
            rs.getString("remarks")
        );
    }
}