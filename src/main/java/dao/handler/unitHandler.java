package dao.handler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import dao.intfc.UnitDAO;
import dao.model.Unit;
/**
 * handler class that coordinates business logic and validation for unit operations.
 * acts as a service layer between the ui and the {@link UnitDAO}
 * 
 */
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

    /**
     * attempts to add a new unit after validating that the required fields are present.
     * @return null if successful, or a string containing the error message if validation or insertion fails.
     */
    public String addUnit(UnitDAO dao, Unit unit){
        if (!isAddInputValid(unit.getEquipmentId(), unit.getSerialNumber())) {
            return "Equipment ID must be greater than 0 and serial number cannot be empty.";
        }

        try {
            dao.add(unit);
            return null; // indicates success
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

    /**
     * performs a soft delete by marking a unit as deleted in the system.
     * validation: prevents deletion if the unit is currently assigned to an employee.
     */
    public String softDeleteUnit(UnitDAO dao, int unitId) {
        if (unitId <= 0) return "Invalid unit ID.";
        try {
            // fetch the unit first to check its current status
            List<Unit> units = dao.findWithAttribute("unit_id", String.valueOf(unitId));
            
            // business rule: active assets cannot be deleted from the registry
            if (!units.isEmpty() && "checked-out".equalsIgnoreCase(units.get(0).getStatus())) {
                return "Cannot delete a checked-out unit. Check it in first.";
            }
            
            dao.softDelete(unitId);
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

    // helper method to ensure unit data is consistent before updating existing records
    private boolean isUpdateInputValid(Unit unit) {
        return unit.getEquipmentId() > 0
            && unit.getSerialNumber() != null
            && !unit.getSerialNumber().isBlank();
    }

    // helper method to ensure mandatory fields are populated for new records
    private boolean isAddInputValid(int equipmentId, String serialNumber) {
        return equipmentId > 0 && serialNumber != null && !serialNumber.isBlank();
    }

    public String setUnitMaintenance(UnitDAO dao,int id){
        try{
            Unit foundUnit = dao.findById(id);
            if(foundUnit == null){
                return "Unit not found. Unable to change status";
            }
            foundUnit.setStatus("Maintenance");
            dao.update(foundUnit);
            return null;
        }catch(SQLException e){
            return "SetUnitMaintenanceError: "+e.getMessage();
        }
    }
    public String setUnitAvailable(UnitDAO dao, int id) {
        try{
            Unit foundUnit = dao.findById(id);
            if(foundUnit == null){
                return "Unit not found. Unable to change status";
            }
            foundUnit.setStatus("Available");
            dao.update(foundUnit);
            return null;
        }catch(SQLException e){
            return "SetUnitAvailableError: "+e.getMessage();
        }
    }
}