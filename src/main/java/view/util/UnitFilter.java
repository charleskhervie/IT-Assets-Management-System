package view.util;

import dao.model.Unit;

public class UnitFilter {

    private static final String STATUS_ALL = "All";

    private UnitFilter() {}

    public static boolean matches(Unit unit, String status, String keyword) {
        return matchesStatus(unit, status) && matchesKeyword(unit, keyword);
    }

    private static boolean matchesStatus(Unit unit, String status) {
        return STATUS_ALL.equals(status)
            || status.equalsIgnoreCase(unit.getStatus());
    }

   private static boolean matchesKeyword(Unit unit, String keyword) {
        if (keyword.isEmpty()) return true;

        String unitId = String.valueOf(unit.getUnitId());
        String serial = unit.getSerialNumber();
        String equipmentId = String.valueOf(unit.getEquipmentId());
        String addedBy = String.valueOf(unit.getAddedBy());
        String assignedTo = unit.getAssignedTo() != null ? String.valueOf(unit.getAssignedTo()): "";

        if(containsKeyword(unitId, keyword)|| containsKeyword(serial, keyword)|| containsKeyword(equipmentId, keyword)
            || containsKeyword(addedBy, keyword) || containsKeyword(assignedTo, keyword)){
            return true;
        }else{
            return false;
        }

    }

    private static boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}