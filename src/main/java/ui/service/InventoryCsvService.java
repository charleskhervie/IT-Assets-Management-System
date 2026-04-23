package ui.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import ui.util.*;

public class InventoryCsvService {

    public ExportSummary exportToCsv(Path targetFile) throws IOException, SQLException {
        List<InventoryExportUtil.InventoryRecord> records =
                InventoryExportUtil.fetchExportRecords();

        StringBuilder csv = new StringBuilder();

        csv.append("unit_id,serial_number,status,equipment_id,equipment_name,brand,model,category_id,category_name,added_by,assigned_to,created_at\n");

        for (var r : records) {
            csv.append(escape(r.unitId())).append(",");
            csv.append(escape(r.serialNumber())).append(",");
            csv.append(escape(r.status())).append(",");
            csv.append(escape(r.equipmentId())).append(",");
            csv.append(escape(r.equipmentName())).append(",");
            csv.append(escape(r.brand())).append(",");
            csv.append(escape(r.model())).append(",");
            csv.append(escape(r.categoryId())).append(",");
            csv.append(escape(r.categoryName())).append(",");
            csv.append(escape(r.addedBy())).append(",");
            csv.append(r.assignedTo() == null ? "" : escape(r.assignedTo())).append(",");
            csv.append(escape(r.createdAt())).append("\n");
        }

        Files.writeString(targetFile, csv.toString(), StandardCharsets.UTF_8);
        return new ExportSummary(targetFile, records.size());
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);

        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }
}