package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import dao.intfc.TransactionDAO;
import dao.model.Transaction;
import dao.dao_util.DBUtil;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public void add(Transaction transaction) throws SQLException {
        String query = "insert into transaction (unit_id, borrowed_by, processed_by, borrowed_date, return_date, status, remarks) values (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getUnitId());
            if (transaction.getBorrower() > 0) {
                ps.setInt(2, transaction.getBorrower());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            if (transaction.getProcessedBy() > 0) {
                ps.setInt(3, transaction.getProcessedBy());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setTimestamp(4, transaction.getBorrowedDate() != null ? Timestamp.valueOf(transaction.getBorrowedDate()) : null);
            ps.setTimestamp(5, transaction.getReturnDate() != null ? Timestamp.valueOf(transaction.getReturnDate()) : null);
            ps.setString(6, transaction.getStatus());
            ps.setString(7, transaction.getRemarks());
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
        String query = "update transaction set unit_id = ?, borrowed_by = ?, processed_by = ?, borrowed_date = ?, return_date = ?, remarks = ? where transaction_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, transaction.getUnitId());
            ps.setInt(2, transaction.getBorrower());
            ps.setInt(3, transaction.getProcessedBy());
            ps.setTimestamp(4, transaction.getBorrowedDate() != null ? Timestamp.valueOf(transaction.getBorrowedDate()) : null);
            ps.setTimestamp(5, transaction.getReturnDate() != null ? Timestamp.valueOf(transaction.getReturnDate()) : null);
            ps.setString(6, transaction.getRemarks());
            ps.setInt(7, transaction.getTransactionId());
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

    //old find all to display data directly taken from data
    /* 
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
    */
   @Override
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = """
            select t.transaction_id, t.unit_id, t.borrowed_by, t.processed_by,
                t.borrowed_date, t.return_date, t.status, t.remarks,
                coalesce(e.equipment_name, 'Unknown') as equipment_name,
                coalesce(borrower.full_name, 'Unknown') as borrowed_by_name,
                coalesce(processor.full_name, '-') as processed_by_name
            from transaction t
            left join units u on t.unit_id = u.unit_id
            left join equipment e on u.equipment_id = e.equipment_id
            left join employees borrower on t.borrowed_by = borrower.emp_id
            left join employees processor on t.processed_by = processor.emp_id
        """;
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
        Transaction t = new Transaction(
            rs.getInt("transaction_id"),
            rs.getInt("unit_id"),
            rs.getInt("borrowed_by"),
            rs.getInt("processed_by"),
            rs.getObject("borrowed_date", LocalDateTime.class),
            rs.getObject("return_date", LocalDateTime.class),
            rs.getString("status"),
            rs.getString("remarks")
        );
        t.setEquipmentName(rs.getString("equipment_name"));
        t.setBorrowedByName(rs.getString("borrowed_by_name"));
        t.setProcessedByName(rs.getString("processed_by_name"));
        return t;
    }

    @Override
    public void approveCheckout(int transactionId, int processedBy, String remarks) throws SQLException {
        String updateTransaction = "update transaction set status = 'checked-out', processed_by = ?, remarks = ? where transaction_id = ?";
        String updateUnit = "update units set status = 'checked-out' where unit_id = (select unit_id from transaction where transaction_id = ?)";
        try (Connection conn = DBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(updateTransaction)) {
                if (processedBy > 0) {
                    ps.setInt(1, processedBy);
                } else {
                    ps.setNull(1, java.sql.Types.INTEGER);
                }
                ps.setString(2, remarks);
                ps.setInt(3, transactionId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateUnit)) {
                ps.setInt(1, transactionId);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void declineCheckout(int transactionId) throws SQLException {
        String updateTransaction = "update transaction set status = 'declined' where transaction_id = ?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateTransaction)) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        }
    }
    @Override
    public List<Transaction> findByEmployee(int empId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = """
            select t.transaction_id, t.unit_id, t.borrowed_by, t.processed_by,
                t.borrowed_date, t.return_date, t.status, t.remarks,
                coalesce(e.equipment_name, 'Unknown') as equipment_name,
                coalesce(borrower.full_name, 'Unknown') as borrowed_by_name,
                coalesce(processor.full_name, '-') as processed_by_name
            from transaction t
            left join units u on t.unit_id = u.unit_id
            left join equipment e on u.equipment_id = e.equipment_id
            left join employees borrower on t.borrowed_by = borrower.emp_id
            left join employees processor on t.processed_by = processor.emp_id
            where t.borrowed_by = ?
        """;
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        }
        return transactions;
    }
    @Override
    public List<Transaction> findCheckedOutByEmployee(int empId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "select * from transaction where borrowed_by = ? and status = 'checked-out'";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        }
        return transactions;
    }
    @Override 
    public void checkIn(int transactionId) throws SQLException {
        String updateTransaction = "update transaction set status = 'returned', return_date = ? where transaction_id = ?";
        String updateUnit = "update units set status = 'available' where unit_id = (select unit_id from transaction where transaction_id = ?)";
        try (Connection conn = DBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(updateTransaction)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(2, transactionId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateUnit)) {
                ps.setInt(1, transactionId);
                ps.executeUpdate();
            }
        }
    }
   
}