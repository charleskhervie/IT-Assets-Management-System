package dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.dao_util.DBUtil;
import dao.intfc.EquipmentDAO;
import dao.model.Equipment;

public class EquipmentDAOImpl implements EquipmentDAO {

    @Override
    public void add(Equipment equipment) throws SQLException {
        String query = "insert into equipment (equipment_name, brand, model, specifications, category_id) values (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, equipment.getEquipmentName());
            ps.setString(2, equipment.getBrand());
            ps.setString(3, equipment.getModel());
            ps.setString(4, equipment.getSpecifications());
            ps.setInt(5, equipment.getCategoryId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    equipment.setEquipmentId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Equipment equipment) throws SQLException {
        String query = "update equipment set equipment_name = ?, brand = ?, model = ?, specifications = ?, category_id = ? where equipment_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, equipment.getEquipmentName());
            ps.setString(2, equipment.getBrand());
            ps.setString(3, equipment.getModel());
            ps.setString(4, equipment.getSpecifications());
            ps.setInt(5, equipment.getCategoryId());
            ps.setInt(6, equipment.getEquipmentId());
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("No equipment found with ID: " + equipment.getEquipmentId());
            }
        }
    }

    @Override
    public void delete(int equipmentId) throws SQLException {
        String query = "delete from equipment where equipment_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, equipmentId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Equipment> findAll() throws SQLException {
        List<Equipment> equipmentList = new ArrayList<>();
        String query = "select * from equipment";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Equipment equipment = new Equipment(
                    rs.getInt("equipment_id"),
                    rs.getString("equipment_name"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getString("specifications"),
                    rs.getInt("category_id")
                );
                equipmentList.add(equipment);
            }
        }
        return equipmentList;
    }

    @Override
    public Equipment findById(int equipmentId) throws SQLException {
        String query = "select * from equipment where equipment_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Equipment(
                        rs.getInt("equipment_id"),
                        rs.getString("equipment_name"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("specifications"),
                        rs.getInt("category_id")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<Equipment> findWithAttribute(String attribute, String value) throws SQLException {
        List<Equipment> equipmentList = new ArrayList<>();
        String query;

        if ("equipment_name".equals(attribute) || "brand".equals(attribute) || "model".equals(attribute) || "specifications".equals(attribute)) {
            query = "select * from equipment where " + attribute + " like ?";
        } else if ("equipment_id".equals(attribute) || "category_id".equals(attribute)) {
            query = "select * from equipment where " + attribute + " = ?";
        } else {
            throw new SQLException("Unsupported attribute: " + attribute);
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if ("equipment_name".equals(attribute) || "brand".equals(attribute) || "model".equals(attribute) || "specifications".equals(attribute)) {
                ps.setString(1, "%" + value + "%");
            } else {
                ps.setInt(1, Integer.parseInt(value));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Equipment equipment = new Equipment(
                        rs.getInt("equipment_id"),
                        rs.getString("equipment_name"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("specifications"),
                        rs.getInt("category_id")
                    );
                    equipmentList.add(equipment);
                }
            }
        } catch (NumberFormatException e) {
            throw new SQLException("Numeric attribute requires a number: " + attribute, e);
        }

        return equipmentList;
    }
}
