package dao.handler;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import dao.intfc.UnitDAO;
import dao.model.Unit;

public class unitHandler {
    
    public List<Unit> getUnits(UnitDAO dao){
        try{
            List<Unit> allUnits = dao.findAll();
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

    private boolean isAddInputValid(int equipmentId, String serialNumber) {
        return equipmentId > 0 && serialNumber != null && !serialNumber.isBlank();
    }

    

    


}
