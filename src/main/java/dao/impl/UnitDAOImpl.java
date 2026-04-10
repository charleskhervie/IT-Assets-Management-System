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

public class UnitDAOImpl implements UnitDAO {

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

    @Override
    public void delete(int unitId) throws SQLException {
        String query = "delete from units where unit_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, unitId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Unit> findAll() throws SQLException {
        List<Unit> units = new ArrayList<>();
        String query = "select * from units";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
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
        return units;
    }

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
}
