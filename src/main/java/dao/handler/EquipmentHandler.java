package dao.handler;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import dao.intfc.EquipmentDAO;
import dao.model.Equipment;
/**
 * handler class for managing equipment definitions.
 * provides validation and error handling for operations involving the 
 * {@link EquipmentDAO}, specifically managing the relationship between 
 * equipment types and their categories.
 */
public class EquipmentHandler {

    public List<Equipment> getEquipments(EquipmentDAO dao) {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String addEquipment(EquipmentDAO dao, Equipment equipment) {
        if (!isInputValid(equipment)) {
            return "Equipment name and category ID must not be empty.";
        }
        
        try {
            dao.add(equipment);
            return null;
        } catch (SQLException e) {
            return "Failed to add equipment: " + e.getMessage();
        }
    }

    public String updateEquipment(EquipmentDAO dao, Equipment equipment) {
        if (!isInputValid(equipment)) {
            return "Equipment name and category ID must not be empty.";
        }
        try {
            dao.update(equipment);
            return null;
        } catch (SQLException e) {
            return "Failed to update equipment: " + e.getMessage();
        }
    }

    public String deleteEquipment(EquipmentDAO dao, int equipmentId) {
        if (equipmentId <= 0) return "Invalid equipment ID.";
        try {
            dao.delete(equipmentId);
            return null;
        } catch (SQLException e) {
             if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key")) {
                return "Cannot delete equipment. Delete units that reference this equipment first.";
            }
            return "Failed to delete equipment: " + e.getMessage();
        }
    }

    

    private boolean isInputValid(Equipment equipment) {
        return equipment.getEquipmentName() != null
            && !equipment.getEquipmentName().isBlank()
            && equipment.getCategoryId() > 0;
    }
}