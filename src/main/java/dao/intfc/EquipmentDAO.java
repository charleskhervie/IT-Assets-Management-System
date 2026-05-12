package dao.intfc;

import java.sql.SQLException;
import java.util.List;

import dao.model.Equipment;
/**
 * Data Access Object interface for Unit operations.
 * Defines the contract for all database interactions involving the equipments table.
 */
public interface EquipmentDAO {
    void add(Equipment equipment) throws SQLException;
    void update(Equipment equipment) throws SQLException;
    void delete(int equipmentId) throws SQLException;
    Equipment findById(int equipmentId) throws SQLException;
    List<Equipment> findAll() throws SQLException;
    List<Equipment> findWithAttribute(String attribute, String value) throws SQLException;
}
