package ui.util;

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
        String equipmentName = unit.getEquipmentName();
        String categoryName = unit.getCategoryName();
        String addedBy = String.valueOf(unit.getAddedBy());
        String addedByName = unit.getAddedByName();
        String assignedTo = unit.getAssignedTo() != null ? String.valueOf(unit.getAssignedTo()): "";
        String assignedToName = unit.getAssignedToName();
        String status = unit.getStatus();

        return containsKeyword(unitId, keyword)
            || containsKeyword(serial, keyword)
            || containsKeyword(equipmentId, keyword)
            || containsKeyword(equipmentName, keyword)
            || containsKeyword(categoryName, keyword)
            || containsKeyword(addedBy, keyword)
            || containsKeyword(addedByName, keyword)
            || containsKeyword(assignedTo, keyword)
            || containsKeyword(assignedToName, keyword)
            || containsKeyword(status, keyword);

    }

    private static boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
