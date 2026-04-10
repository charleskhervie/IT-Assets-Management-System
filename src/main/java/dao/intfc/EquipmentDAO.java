package dao.intfc;

import java.sql.SQLException;
import java.util.List;

import dao.model.Equipment;

public interface EquipmentDAO {
    void add(Equipment equipment) throws SQLException;
    void update(Equipment equipment) throws SQLException;
    void delete(int equipmentId) throws SQLException;
    Equipment findById(int equipmentId) throws SQLException;
    List<Equipment> findAll() throws SQLException;
    List<Equipment> findWithAttribute(String attribute, String value) throws SQLException;
}
