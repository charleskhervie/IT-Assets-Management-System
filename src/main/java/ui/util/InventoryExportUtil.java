package ui.util;

import dao.dao_util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InventoryExportUtil {

    private static final DateTimeFormatter EXPORT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static List<InventoryRecord> fetchExportRecords() throws SQLException {
        List<InventoryRecord> records = new ArrayList<>();

        String query = """
            select u.unit_id, u.serial_number, u.status, u.equipment_id,
                   u.added_by, u.assigned_to, u.created_at,
                   e.equipment_name, e.brand, e.model,
                   e.category_id, c.category_name
            from units u
            join equipment e on e.equipment_id = u.equipment_id
            left join categories c on c.category_id = e.category_id
            order by u.unit_id
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDateTime createdAt =
                        rs.getObject("created_at", LocalDateTime.class);

                records.add(new InventoryRecord(
                        rs.getInt("unit_id"),
                        rs.getString("serial_number"),
                        rs.getString("status"),
                        rs.getInt("equipment_id"),
                        rs.getString("equipment_name"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getInt("added_by"),
                        (Integer) rs.getObject("assigned_to"),
                        createdAt == null ? null :
                                EXPORT_FORMAT.format(createdAt)
                ));
            }
        }

        return records;
    }

    public static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String sqlString(String value) {
        if (value == null) return "NULL";
        return "'" + value.replace("'", "''") + "'";
    }

    public static String jsonQuote(String value) {
        if (value == null) return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
    
    public record InventoryRecord(
            int unitId,
            String serialNumber,
            String status,
            int equipmentId,
            String equipmentName,
            String brand,
            String model,
            int categoryId,
            String categoryName,
            int addedBy,
            Integer assignedTo,
            String createdAt
    ) {}
}