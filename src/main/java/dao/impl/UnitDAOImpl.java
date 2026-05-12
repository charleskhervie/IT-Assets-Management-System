package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dao.dao_util.DBUtil;
import dao.intfc.UnitDAO;
import dao.model.Unit;
import javafx.collections.FXCollections;
/**
 * implementation of the {@link UnitDAO} interface for managing individual 
 * physical equipment instances. this class handles state transitions, 
 * assignment tracking, and soft-deletion logic for units within the 
 * it asset management system.
 * 
 */
public class UnitDAOImpl implements UnitDAO {
    //add unit
    @Override
    public void add(Unit unit) throws SQLException {
        String query = "insert into units (equipment_id, serial_number, status, added_by, created_at, assigned_to) values (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, unit.getEquipmentId());
            ps.setString(2, unit.getSerialNumber());
            ps.setString(3, unit.getStatus());
            ps.setInt(4, unit.getAddedBy());
            ps.setObject(5, unit.getCreatedAt());
            ps.setObject(6, unit.getAssignedTo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    unit.setUnitId(keys.getInt(1));
                }
            }
        }
    }
    //update unit
    @Override
    public void update(Unit unit) throws SQLException {
        String query = "update units set equipment_id = ?, serial_number = ?, status = ?, added_by = ?, assigned_to = ? where unit_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, unit.getEquipmentId());
            ps.setString(2, unit.getSerialNumber());
            ps.setString(3, unit.getStatus());
            ps.setInt(4, unit.getAddedBy());
            ps.setObject(5, unit.getAssignedTo());
            ps.setInt(6, unit.getUnitId());
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("No unit found with ID: " + unit.getUnitId());
            }
        }
    }
    //delete a unit
    @Override
    public void delete(int unitId) throws SQLException {
        String query = "delete from units where unit_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, unitId);
            ps.executeUpdate();
        }
    }
    /**
     * Soft deletes a unit by setting its is_deleted flag to true.
     * This preserves transaction history that references the unit.
     *
     * @param unitId the ID of the unit to soft delete
     * @throws SQLException if a database error occurs
     */
    @Override
    public void softDelete(int unitId) throws SQLException {
        String query = "update units set is_deleted = TRUE where unit_id = ?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, unitId);
            ps.executeUpdate();
        }
    }
    
    //original findall for data taken from database
    
    @Override
    public List<Unit> findAllRaw() throws SQLException {
        List<Unit> units = new ArrayList<>();
        String query = "select * from units where is_deleted = FALSE";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                units.add(mapRowRaw(rs));
            }
        }

        return units;
    }
     /**
     * Retrieves all active units with display-friendly joined fields
     * such as equipment name, category name, added by name, and assigned to name.
     *
     * @return list of units with populated display fields
     * @throws SQLException if a database error occurs
     */
    @Override
    public List<Unit> findAllDisplay() throws SQLException {
        List<Unit> units = new ArrayList<>();
        String query = """
            select u.unit_id, u.equipment_id, u.serial_number, u.status,
                u.added_by, u.created_at, u.assigned_to,
                coalesce(e.equipment_name, 'Unknown') as equipment_name,
                coalesce(c.category_name, 'Unknown') as category_name,
                coalesce(emp.full_name, 'Unknown') as added_by_name,
                coalesce(assigned_emp.full_name, '-') as assigned_to_name
            from units u
            left join equipment e on u.equipment_id = e.equipment_id
            left join categories c on e.category_id = c.category_id
            left join employees emp on u.added_by = emp.emp_id
            left join employees assigned_emp on u.assigned_to = assigned_emp.emp_id
            where u.is_deleted = FALSE
        """;
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Unit unit = mapRowDisplay(rs);
                units.add(unit);
            }
        }
        return units;
    }
     /**
     * Retrieves all active units with specific id
     *
     * @return list of units with matched id
     * @throws SQLException if a database error occurs
     */
    @Override
    public Unit findById(int unitId) throws SQLException {
        String query = "select * from units where unit_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, unitId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Unit(
                        rs.getInt("unit_id"),
                        rs.getInt("equipment_id"),
                        rs.getString("serial_number"),
                        rs.getString("status"),
                        rs.getInt("added_by"),
                        rs.getObject("created_at", LocalDateTime.class),
                        (Integer) rs.getObject("assigned_to")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasUnitsByEquipmentId(int equipmentId) throws SQLException {
        String query = "select count(*) from units where equipment_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<Unit> findWithAttribute(String attribute, String value) throws SQLException {
        List<Unit> units = new ArrayList<>();
        String query;

        if ("serial_number".equals(attribute) || "status".equals(attribute) || "created_at".equals(attribute)) {
            query = "select * from units where " + attribute + " like ?";
        } else if ("unit_id".equals(attribute) || "equipment_id".equals(attribute) || "added_by".equals(attribute) || "assigned_to".equals(attribute)) {
            query = "select * from units where " + attribute + " = ?";
        } else {
            throw new SQLException("Unsupported attribute: " + attribute);
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if ("serial_number".equals(attribute) || "status".equals(attribute) || "created_at".equals(attribute)) {
                ps.setString(1, "%" + value + "%");
            } else {
                ps.setInt(1, Integer.parseInt(value));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Unit unit = new Unit(
                        rs.getInt("unit_id"),
                        rs.getInt("equipment_id"),
                        rs.getString("serial_number"),
                        rs.getString("status"),
                        rs.getInt("added_by"),
                        rs.getObject("created_at", LocalDateTime.class),
                        (Integer) rs.getObject("assigned_to")
                    );
                    units.add(unit);
                }
            }
        } catch (NumberFormatException e) {
            throw new SQLException("Numeric attribute requires a number: " + attribute, e);
        }

        return units;
    }

    @Override
    public Unit findBySerialExact(String serial) throws SQLException {
        String query = "select * from units where BINARY serial_number = ? and is_deleted = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, serial);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowRaw(rs);
                }
            }
        }
        return null;
    }
    
    private Unit mapRowRaw(ResultSet rs) throws SQLException {
        return new Unit(
            rs.getInt("unit_id"),
            rs.getInt("equipment_id"),
            rs.getString("serial_number"),
            rs.getString("status"),
            rs.getInt("added_by"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("assigned_to", Integer.class)
        );
    }
    /**
     * Maps a single row from a ResultSet into a {@code Unit} object, including 
     * additional descriptive fields for display purposes.
     * 
     * <p>In addition to the core fields, this method populates human-readable names 
     * (e.g., equipment name, category, and personnel names) which are expected 
     * to be present in the query result via SQL joins.</p>
     * 
     * @param rs the {@code ResultSet} currently positioned at the desired row
     * @return a {@code Unit} object populated with core data and display attributes
     * @throws SQLException if a database access error occurs or the expected 
     *         join columns are missing
     */
    private Unit mapRowDisplay(ResultSet rs) throws SQLException {
        Unit unit = new Unit(
            rs.getInt("unit_id"),
            rs.getInt("equipment_id"),
            rs.getString("serial_number"),
            rs.getString("status"),
            rs.getInt("added_by"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("assigned_to", Integer.class)
        );
        unit.setEquipmentName(rs.getString("equipment_name"));
        unit.setCategoryName(rs.getString("category_name"));
        unit.setAddedByName(rs.getString("added_by_name"));
        unit.setAssignedToName(rs.getString("assigned_to_name"));
        return unit;
    }

    
}
