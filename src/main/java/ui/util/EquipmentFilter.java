package ui.util;

import dao.model.Equipment;

public class EquipmentFilter {

    private EquipmentFilter() {}

    public static boolean matches(Equipment equipment, String keyword) {
        if (keyword.isEmpty()) return true;
        return containsKeyword(String.valueOf(equipment.getEquipmentId()), keyword)
            || containsKeyword(equipment.getEquipmentName(), keyword)
            || containsKeyword(equipment.getBrand(), keyword)
            || containsKeyword(equipment.getModel(), keyword)
            || containsKeyword(equipment.getSpecifications(), keyword)
            || containsKeyword(String.valueOf(equipment.getCategoryId()), keyword);
    }

    private static boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}