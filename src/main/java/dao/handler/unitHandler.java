package dao.handler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import dao.intfc.UnitDAO;
import dao.model.Unit;

public class unitHandler {
    
    public List<Unit> getUnitsDisplay(UnitDAO dao){
        try{
            List<Unit> allUnits = dao.findAllDisplay();
            return allUnits;
        }catch(SQLException e){
            System.out.println("Database " + e.getMessage());
            return Collections.emptyList();
        }
        
    }
    public List<Unit> getUnitsRaw(UnitDAO dao){
        try{
            List<Unit> allUnits = dao.findAllRaw();
            return allUnits;
        }catch(SQLException e){
            System.out.println("Database " + e.getMessage());
            return Collections.emptyList();
        }
        
    }

    public String addUnit(UnitDAO dao, Unit unit){
        if (!isAddInputValid(unit.getEquipmentId(), unit.getSerialNumber())) {
            return "Equipment ID must be greater than 0 and serial number cannot be empty.";
        }

        try {
            dao.add(unit);
            return null;
        } catch (SQLException e) {
            return "Failed to add unit: " + e.getMessage();
        }
    }
    public String deleteUnit(UnitDAO dao, int unitId) {
        if (unitId <= 0) return "Invalid unit ID.";
        try {
            dao.delete(unitId);
            return null;
        } catch (SQLException e) {
            return "Failed to delete: " + e.getMessage();
        }
    }
    public String updateUnit(UnitDAO dao, Unit unit) {
        if (!isUpdateInputValid(unit)) {
            return "Equipment ID must be greater than 0 and serial number cannot be empty.";
        }
        try {
            dao.update(unit);
            return null;
        } catch (SQLException e) {
            return "Failed to update unit: " + e.getMessage();
        }
    }

    private boolean isUpdateInputValid(Unit unit) {
        return unit.getEquipmentId() > 0
            && unit.getSerialNumber() != null
            && !unit.getSerialNumber().isBlank();
    }

    private boolean isAddInputValid(int equipmentId, String serialNumber) {
        return equipmentId > 0 && serialNumber != null && !serialNumber.isBlank();
    }

    

}
