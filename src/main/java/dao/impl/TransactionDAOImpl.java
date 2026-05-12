package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dao.dao_util.DBUtil;
import dao.intfc.TransactionDAO;
import dao.model.Transaction;

/**
 * Implementation of the {@link TransactionDAO} interface.
 * Manages the lifecycle of asset transactions including checkout, return, and approvals.
 */
public class TransactionDAOImpl implements TransactionDAO {

    /**
     * Helper to convert SQL Timestamp to Java LocalDateTime safely.
     */
    private LocalDateTime readDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    /**
     * Helper to handle nullable Integer columns in PreparedStatements.
     */
    private void setNullableInt(PreparedStatement ps, int index, int value) throws SQLException {
        if (value > 0) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, java.sql.Types.INTEGER);
        }
    }

    @Override
    public void add(Transaction transaction) throws SQLException {
        String query = "insert into transaction (unit_id, borrowed_by, processed_by, borrowed_date, return_date, status, remarks) values (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getUnitId());
            setNullableInt(ps, 2, transaction.getBorrower());
            setNullableInt(ps, 3, transaction.getProcessedBy());
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

    @Override
    public List<Transaction> findAllRaw() throws SQLException {
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
    public List<Transaction> findAllDisplay() throws SQLException {
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
                transactions.add(mapRowDisplay(rs));
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

    @Override
    public List<Transaction> findByEmployee(int empId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = """
            select t.*, e.equipment_name, borrower.full_name as borrowed_by_name, processor.full_name as processed_by_name
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
                    transactions.add(mapRowDisplay(rs));
                }
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> findCheckedOutByEmployee(int empId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "select * from transaction where borrowed_by = ? and status = 'Checked-out'";
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
    public void assignUnit(int unitId, int empId) throws SQLException {
        String query = "update units set assigned_to = ? where unit_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, empId);
            ps.setInt(2, unitId);
            ps.executeUpdate();
        }
    }
    /**
     * Facilitates a quick return by calling approveReturn with default values.
     * Often used for administrative overrides or simplified return flows.
     *
     * @param transactionId the unique identifier of the transaction to check in
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void checkIn(int transactionId) throws SQLException {
        approveReturn(transactionId, 0, "Self-returned");
    }

    /**
     * Updates the transaction status to 'Pending Return'.
     * This indicates the borrower has initiated the return, but it hasn't been verified.
     *
     * @param transactionId the unique identifier of the transaction
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void pendingReturn(int transactionId) throws SQLException {
        String query = "update transaction set status = 'Pending Return' where transaction_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        }
    }
    /**
     * Reverts a 'Pending Return' status back to 'Checked-out'.
     * Useful if a return request is canceled or denied by an administrator.
     *
     * @param transactionId the unique identifier of the transaction
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void revertToCheckedOut(int transactionId) throws SQLException {
        String query = "update transaction set status = 'Checked-out' where transaction_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        }
    }
    /**
     * Finalizes the return process using a database transaction.
     * Updates the transaction record with a return timestamp and remarks,
     * and simultaneously marks the associated unit as 'Available' and unassigned.
     *
     * @param transactionId the unique identifier of the transaction
     * @param processedBy   the ID of the employee who approved the return
     * @param remarks       additional notes regarding the condition or process
     * @throws SQLException if a database access error occurs; triggers a rollback if the operation fails
     */
    @Override
    public void approveReturn(int transactionId, int processedBy, String remarks) throws SQLException {
        String updateTransaction = "update transaction set status = 'Returned', return_date = ?, processed_by = ?, remarks = ? where transaction_id = ?";
        String updateUnit = "update units set status = 'Available', assigned_to = NULL where unit_id = (select unit_id from transaction where transaction_id = ?)";
        String declineOthers = """
            update transaction set status = 'Declined' 
            where unit_id in (select unit_id from (select unit_id from transaction where transaction_id = ?) as derived) 
            and transaction_id != ? and status = 'Pending Return'
            """;
        
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(updateTransaction)) {
                    ps.setObject(1, LocalDateTime.now());
                    setNullableInt(ps, 2, processedBy);
                    ps.setString(3, remarks);
                    ps.setInt(4, transactionId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateUnit)) {
                    ps.setInt(1, transactionId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(declineOthers)) {
                    ps.setInt(1, transactionId);
                    ps.setInt(2, transactionId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    /**
     * Approves a pending checkout request using a database transaction.
     * Updates the transaction status to 'Checked-out' and synchronizes the 
     * units table to reflect the current holder and status.
     *
     * @param transactionId the unique identifier of the transaction
     * @param processedBy   the ID of the employee approving the checkout
     * @param remarks       approval notes or asset condition details
     * @throws SQLException if a database access error occurs; triggers a rollback if the operation fails
     */
    @Override
    public void approveCheckout(int transactionId, int processedBy, String remarks) throws SQLException {
        String updateTransaction = "update transaction set status = 'Checked-out', processed_by = ?, remarks = ? where transaction_id = ?";
        String updateUnit = """
            update units set status = 'Checked-out', assigned_to = 
            (select borrowed_by from transaction where transaction_id = ?) 
            where unit_id = (select unit_id from transaction where transaction_id = ?)
            """;
        String declineOthers = """
            update transaction set status = 'Declined' 
            where unit_id in (select unit_id from (select unit_id from transaction where transaction_id = ?) as derived) 
            and transaction_id != ? and status = 'Pending'
            """;
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(updateTransaction)) {
                    setNullableInt(ps, 1, processedBy);
                    ps.setString(2, remarks);
                    ps.setInt(3, transactionId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateUnit)) {
                    ps.setInt(1, transactionId);
                    ps.setInt(2, transactionId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(declineOthers)) {
                    ps.setInt(1, transactionId);
                    ps.setInt(2, transactionId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    /**
     * Marks a checkout request as 'Declined'.
     * Does not modify the unit status, as the unit remains in its original state (likely 'Available').
     *
     * @param transactionId the unique identifier of the transaction
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void declineCheckout(int transactionId) throws SQLException {
        String query = "update transaction set status = 'Declined' where transaction_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        }
    }
    /**
     * Maps a single row from a ResultSet into a Transaction object.
     * Used for basic queries involving only the transaction table columns.
     *
     * @param rs the ResultSet pointing to the current row
     * @return a Transaction object populated with base data
     * @throws SQLException if column labels are invalid or a database error occurs
     */
    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("transaction_id"),
            rs.getInt("unit_id"),
            rs.getInt("borrowed_by"),
            rs.getInt("processed_by"),
            readDateTime(rs, "borrowed_date"),
            readDateTime(rs, "return_date"),
            rs.getString("status"),
            rs.getString("remarks")
        );
    }
    /**
     * Maps a row from a joined ResultSet into a Transaction object.
     * Populates additional display-only fields such as equipment name and employee full names.
     *
     * @param rs the ResultSet containing joined table data (equipment, employees)
     * @return a Transaction object populated with both base and display data
     * @throws SQLException if column labels are invalid or a database error occurs
     */
    private Transaction mapRowDisplay(ResultSet rs) throws SQLException {
        Transaction t = mapRow(rs);
        t.setEquipmentName(rs.getString("equipment_name"));
        t.setBorrowedByName(rs.getString("borrowed_by_name"));
        t.setProcessedByName(rs.getString("processed_by_name"));
        return t;
    }
}