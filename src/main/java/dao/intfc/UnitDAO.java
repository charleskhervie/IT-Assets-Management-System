package dao.intfc;

import java.sql.SQLException;
import java.util.List;

import dao.model.Unit;

public interface UnitDAO {
    void add(Unit unit) throws SQLException;
    void update(Unit unit) throws SQLException;
    void delete(int unitId) throws SQLException;
    void softDelete(int unitId) throws SQLException;
    Unit findById(int unitId) throws SQLException;
    List<Unit> findAllRaw() throws SQLException;
    List<Unit> findAllDisplay() throws SQLException;
    List<Unit> findWithAttribute(String attribute, String value) throws SQLException;
    boolean hasUnitsByEquipmentId(int equipmentId) throws SQLException;
}
