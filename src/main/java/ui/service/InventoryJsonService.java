package ui.service;

import dao.dao_util.CredentialManager;
import dao.impl.EmployeeDAOImpl;
import dao.impl.EquipmentDAOImpl;
import dao.impl.UnitDAOImpl;
import dao.intfc.EmployeeDAO;
import dao.intfc.EquipmentDAO;
import dao.intfc.UnitDAO;
import dao.model.Unit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import ui.util.*;

public class InventoryJsonService {

    private static final String DEFAULT_STATUS = "Available";

    private final UnitDAO unitDAO = new UnitDAOImpl();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();
    private final CredentialManager credentialManager = new CredentialManager();

    public ExportSummary exportUnits(Path targetFile) throws IOException, SQLException {
        List<InventoryExportUtil.InventoryRecord> records =
                InventoryExportUtil.fetchExportRecords();

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"exportedAt\": ")
                .append(InventoryExportUtil.jsonQuote(LocalDateTime.now().toString())).append(",\n");
        json.append("  \"recordCount\": ").append(records.size()).append(",\n");
        json.append("  \"units\": [\n");

        for (int i = 0; i < records.size(); i++) {
            var r = records.get(i);

            json.append("    {\n");
            json.append("      \"unitId\": ").append(r.unitId()).append(",\n");
            json.append("      \"serialNumber\": ").append(InventoryExportUtil.jsonQuote(r.serialNumber())).append(",\n");
            json.append("      \"status\": ").append(InventoryExportUtil.jsonQuote(r.status())).append(",\n");
            json.append("      \"equipmentId\": ").append(r.equipmentId()).append(",\n");
            json.append("      \"equipmentName\": ").append(InventoryExportUtil.jsonQuote(r.equipmentName())).append(",\n");
            json.append("      \"brand\": ").append(InventoryExportUtil.jsonQuote(r.brand())).append(",\n");
            json.append("      \"model\": ").append(InventoryExportUtil.jsonQuote(r.model())).append(",\n");
            json.append("      \"categoryId\": ").append(r.categoryId()).append(",\n");
            json.append("      \"categoryName\": ").append(InventoryExportUtil.jsonQuote(r.categoryName())).append(",\n");
            json.append("      \"addedBy\": ").append(r.addedBy()).append(",\n");
            json.append("      \"assignedTo\": ")
                    .append(r.assignedTo() == null ? "null" : r.assignedTo()).append(",\n");
            json.append("      \"createdAt\": ")
                    .append(InventoryExportUtil.jsonQuote(r.createdAt())).append("\n");
            json.append("    }");

            if (i < records.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        Files.writeString(targetFile, json.toString(), StandardCharsets.UTF_8);
        return new ExportSummary(targetFile, records.size());
    }

    public ImportPreview loadPreview(Path sourceFile) throws IOException, SQLException {
        String json = Files.readString(sourceFile, StandardCharsets.UTF_8);
        Object parsed = SimpleJsonParser.parse(json);
        List<Map<String, Object>> rawRecords = extractRecords(parsed);
        return buildPreview(rawRecords);
    }

    public ImportExecution importRecords(ImportPreview preview,
                                         boolean skipDuplicates,
                                         boolean validateBeforeImport) throws SQLException {

        if (preview.records().isEmpty()) {
            return new ImportExecution(0, preview.invalidCount(),
                    List.of("No records found."));
        }

        List<String> issues = new ArrayList<>();
        int imported = 0;

        for (PreviewRecord record : preview.records()) {
            if (!record.issues().isEmpty()) {
                if (!skipDuplicates || !isDuplicateOnly(record.issues())) {
                    issues.add("Row " + record.rowNumber() + ": " +
                            String.join("; ", record.issues()));
                }
                continue;
            }

            try {
                unitDAO.add(record.toUnit());
                imported++;
            } catch (SQLException e) {
                if (!skipDuplicates || !isDuplicateException(e)) {
                    issues.add("Row " + record.rowNumber() + ": " + e.getMessage());
                }
            }
        }

        return new ImportExecution(imported, preview.invalidCount(), issues);
    }

    private ImportPreview buildPreview(List<Map<String, Object>> rawRecords)
            throws SQLException {

        List<PreviewRecord> previewRecords = new ArrayList<>();
        Set<String> seenSerials = new HashSet<>();
        int valid = 0;

        for (int i = 0; i < rawRecords.size(); i++) {
            NormalizedRecord n = normalizeRecord(rawRecords.get(i), i + 1);
            List<String> issues = validateRecord(n, seenSerials);
            if (issues.isEmpty()) valid++;
            previewRecords.add(new PreviewRecord(i + 1, n, issues));
        }

        return new ImportPreview(previewRecords, valid,
                rawRecords.size() - valid);
    }

    private List<String> validateRecord(NormalizedRecord r,
                                        Set<String> seenSerials)
            throws SQLException {

        List<String> issues = new ArrayList<>();

        if (r.equipmentId() <= 0 ||
                equipmentDAO.findById(r.equipmentId()) == null) {
            issues.add("Invalid equipmentId");
        }

        if (r.serialNumber() == null || r.serialNumber().isBlank()) {
            issues.add("serialNumber is required");
        } else {
            String s = r.serialNumber().toLowerCase();
            if (!seenSerials.add(s)) {
                issues.add("Duplicate serialNumber");
            }
            if (!unitDAO.findWithAttribute("serial_number", r.serialNumber()).isEmpty()) {
                issues.add("Already exists");
            }
        }

        if (r.addedBy() <= 0 ||
                employeeDAO.findById(r.addedBy()) == null) {
            issues.add("Invalid addedBy");
        }

        return issues;
    }

    private NormalizedRecord normalizeRecord(Map<String, Object> rawRecord, int rowNumber) {
        int equipmentId = toInt(rawRecord, "equipmentId", "equipment_id");

        int addedBy = toInt(rawRecord, "addedBy", "added_by");
        if (addedBy <= 0) addedBy = currentUserId();

        Integer assignedTo = toNullableInt(rawRecord, "assignedTo", "assigned_to");

        String serialNumber = toStringValue(rawRecord, "serialNumber", "serial_number");

        String status = toStringValue(rawRecord, "status");
        if (status == null || status.isBlank()) status = DEFAULT_STATUS;

        LocalDateTime createdAt = parseDateTime(
                toStringValue(rawRecord, "createdAt", "created_at"));
        if (createdAt == null) createdAt = LocalDateTime.now();

        return new NormalizedRecord(
                rowNumber,
                equipmentId,
                serialNumber == null ? null : serialNumber.trim(),
                status.trim(),
                addedBy,
                createdAt,
                assignedTo
        );
    }

    private String toStringValue(Map<String, Object> rawRecord, String... keys) {
        for (String key : keys) {
            Object value = rawRecord.get(key);
            if (value != null) return String.valueOf(value);
        }
        return null;
    }

    private int toInt(Map<String, Object> rawRecord, String... keys) {
        Integer value = toNullableInt(rawRecord, keys);
        return value == null ? 0 : value;
    }

    private Integer toNullableInt(Map<String, Object> rawRecord, String... keys) {
        for (String key : keys) {
            Object value = rawRecord.get(key);
            if (value == null) continue;

            if (value instanceof Number number) return number.intValue();

            try {
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (Exception ignored) {
                return 0;
            }
        }
        return null;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {}
        return null;
    }

    private int currentUserId() {
        try {
            if (!credentialManager.exists()) return 1;
            return Integer.parseInt(
                    credentialManager.load().getProperty("emp_id", "1"));
        } catch (Exception e) {
            return 1;
        }
    }

    private boolean isDuplicateOnly(List<String> issues) {
        return issues.stream().allMatch(i -> i.toLowerCase().contains("duplicate"));
    }

    private boolean isDuplicateException(SQLException e) {
        return e.getMessage() != null &&
                e.getMessage().toLowerCase().contains("duplicate");
    }

    private List<Map<String, Object>> extractRecords(Object parsed) {
        if (parsed instanceof List<?> list) return (List<Map<String, Object>>) list;

        if (parsed instanceof Map<?, ?> map) {
            Object units = map.get("units");
            if (units instanceof List<?> list) {
                return (List<Map<String, Object>>) list;
            }
        }

        throw new IllegalArgumentException("Invalid JSON");
    }

    public record ImportPreview(List<PreviewRecord> records,
                                int validCount, int invalidCount) {}

    public record ImportExecution(int importedCount,
                                  int invalidCount,
                                  List<String> issues) {}

    public record PreviewRecord(int rowNumber,
                                NormalizedRecord normalizedRecord,
                                List<String> issues) {
        public Unit toUnit() {
            return normalizedRecord.toUnit();
        }
    }

    public record NormalizedRecord(int rowNumber, int equipmentId,
                                   String serialNumber, String status,
                                   int addedBy, LocalDateTime createdAt,
                                   Integer assignedTo) {
        public Unit toUnit() {
            return new Unit(0, equipmentId, serialNumber,
                    status, addedBy, createdAt, assignedTo);
        }
    }
}