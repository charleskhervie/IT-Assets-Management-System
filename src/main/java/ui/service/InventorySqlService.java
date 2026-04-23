package ui.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import ui.util.*;

public class InventorySqlService {

    public ExportSummary exportToSql(Path targetFile) throws IOException, SQLException {
        List<InventoryExportUtil.InventoryRecord> records =
                InventoryExportUtil.fetchExportRecords();

        StringBuilder sql = new StringBuilder();

        sql.append("-- ITAMS Inventory Export\n");
        sql.append("-- Generated at: ").append(LocalDateTime.now()).append("\n\n");
        sql.append("INSERT INTO units (unit_id, serial_number, status, equipment_id, added_by, assigned_to, created_at)\n");
        sql.append("VALUES\n");

        for (int i = 0; i < records.size(); i++) {
            var r = records.get(i);

            sql.append("  (");
            sql.append(r.unitId()).append(", ");
            sql.append(sqlString(r.serialNumber())).append(", ");
            sql.append(sqlString(r.status())).append(", ");
            sql.append(r.equipmentId()).append(", ");
            sql.append(r.addedBy()).append(", ");
            sql.append(r.assignedTo() == null ? "NULL" : r.assignedTo()).append(", ");
            sql.append(sqlString(r.createdAt())).append(")");
            sql.append(i < records.size() - 1 ? ",\n" : ";\n");
        }

        Files.writeString(targetFile, sql.toString(), StandardCharsets.UTF_8);
        return new ExportSummary(targetFile, records.size());
    }

    private String sqlString(String value) {
        if (value == null) {
            return "NULL";
        }
        return "'" + value.replace("'", "''") + "'";
    }
}