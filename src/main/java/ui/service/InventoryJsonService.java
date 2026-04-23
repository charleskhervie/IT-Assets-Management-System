package ui.service;

import dao.dao_util.CredentialManager;
import dao.dao_util.DBUtil;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class InventoryJsonService {
    private static final DateTimeFormatter EXPORT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_STATUS = "Available";
    private static final String FALLBACK_USER_ID = "1";

    private final UnitDAO unitDAO = new UnitDAOImpl();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();
    private final CredentialManager credentialManager = new CredentialManager();

    public ExportSummary exportUnits(Path targetFile) throws IOException, SQLException {
        List<InventoryRecord> records = fetchExportRecords();

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"exportedAt\": ").append(quote(EXPORT_FORMAT.format(LocalDateTime.now()))).append(",\n");
        json.append("  \"recordCount\": ").append(records.size()).append(",\n");
        json.append("  \"units\": [\n");

        for (int i = 0; i < records.size(); i++) {
            InventoryRecord record = records.get(i);
            json.append("    {\n");
            json.append("      \"unitId\": ").append(record.unitId()).append(",\n");
            json.append("      \"serialNumber\": ").append(quote(record.serialNumber())).append(",\n");
            json.append("      \"status\": ").append(quote(record.status())).append(",\n");
            json.append("      \"equipmentId\": ").append(record.equipmentId()).append(",\n");
            json.append("      \"equipmentName\": ").append(quote(record.equipmentName())).append(",\n");
            json.append("      \"brand\": ").append(quote(record.brand())).append(",\n");
            json.append("      \"model\": ").append(quote(record.model())).append(",\n");
            json.append("      \"categoryId\": ").append(record.categoryId()).append(",\n");
            json.append("      \"categoryName\": ").append(quote(record.categoryName())).append(",\n");
            json.append("      \"addedBy\": ").append(record.addedBy()).append(",\n");
            json.append("      \"assignedTo\": ").append(record.assignedTo() == null ? "null" : record.assignedTo()).append(",\n");
            json.append("      \"createdAt\": ").append(quote(record.createdAt())).append("\n");
            json.append("    }");
            if (i < records.size() - 1) {
                json.append(",");
            }
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

    public ImportExecution importRecords(ImportPreview preview, boolean skipDuplicates, boolean validateBeforeImport)
            throws SQLException {
        if (preview.records().isEmpty()) {
            return new ImportExecution(0, preview.invalidCount(), List.of("No records found in the selected file."));
        }

        List<String> blockingIssues = new ArrayList<>();
        int importedCount = 0;

        for (PreviewRecord record : preview.records()) {
            if (!record.issues().isEmpty()) {
                if (!skipDuplicates || !isDuplicateOnly(record.issues())) {
                    blockingIssues.add("Row " + record.rowNumber() + ": " + String.join("; ", record.issues()));
                }
                continue;
            }

            try {
                unitDAO.add(record.toUnit());
                importedCount++;
            } catch (SQLException e) {
                String message = "Row " + record.rowNumber() + ": " + e.getMessage();
                if (skipDuplicates && isDuplicateException(e)) {
                    continue;
                }
                blockingIssues.add(message);
            }
        }

        if (validateBeforeImport && !blockingIssues.isEmpty()) {
            return new ImportExecution(importedCount, preview.invalidCount(), blockingIssues);
        }

        return new ImportExecution(importedCount, preview.invalidCount(), blockingIssues);
    }

    private ImportPreview buildPreview(List<Map<String, Object>> rawRecords) throws SQLException {
        List<PreviewRecord> previewRecords = new ArrayList<>();
        Set<String> seenSerialNumbers = new HashSet<>();
        int validCount = 0;

        for (int i = 0; i < rawRecords.size(); i++) {
            NormalizedRecord normalized = normalizeRecord(rawRecords.get(i), i + 1);
            List<String> issues = validateRecord(normalized, seenSerialNumbers);
            if (issues.isEmpty()) {
                validCount++;
            }
            previewRecords.add(new PreviewRecord(i + 1, normalized, issues));
        }

        return new ImportPreview(previewRecords, validCount, rawRecords.size() - validCount);
    }

    private List<String> validateRecord(NormalizedRecord record, Set<String> seenSerialNumbers) throws SQLException {
        List<String> issues = new ArrayList<>();

        if (record.equipmentId() <= 0) {
            issues.add("Missing or invalid equipmentId");
        } else if (equipmentDAO.findById(record.equipmentId()) == null) {
            issues.add("equipmentId " + record.equipmentId() + " does not exist");
        }

        if (record.serialNumber() == null || record.serialNumber().isBlank()) {
            issues.add("serialNumber is required");
        } else {
            String normalizedSerial = record.serialNumber().trim().toLowerCase();
            if (!seenSerialNumbers.add(normalizedSerial)) {
                issues.add("Duplicate serialNumber found in file");
            }
            if (!unitDAO.findWithAttribute("serial_number", record.serialNumber().trim()).isEmpty()) {
                issues.add("serialNumber already exists in database");
            }
        }

        if (record.addedBy() <= 0) {
            issues.add("Missing or invalid addedBy");
        } else if (employeeDAO.findById(record.addedBy()) == null) {
            issues.add("addedBy " + record.addedBy() + " does not exist");
        }

        if (record.assignedTo() != null && record.assignedTo() > 0 && employeeDAO.findById(record.assignedTo()) == null) {
            issues.add("assignedTo " + record.assignedTo() + " does not exist");
        }

        return issues;
    }

    private NormalizedRecord normalizeRecord(Map<String, Object> rawRecord, int rowNumber) {
        int equipmentId = toInt(rawRecord, "equipmentId", "equipment_id");
        int addedBy = toInt(rawRecord, "addedBy", "added_by");
        if (addedBy <= 0) {
            addedBy = currentUserId();
        }

        Integer assignedTo = toNullableInt(rawRecord, "assignedTo", "assigned_to");
        String serialNumber = toStringValue(rawRecord, "serialNumber", "serial_number");
        String status = toStringValue(rawRecord, "status");
        if (status == null || status.isBlank()) {
            status = DEFAULT_STATUS;
        }

        LocalDateTime createdAt = parseDateTime(toStringValue(rawRecord, "createdAt", "created_at"));
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

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

    private List<Map<String, Object>> extractRecords(Object parsed) {
        if (parsed instanceof List<?> list) {
            return asRecordList(list);
        }

        if (parsed instanceof Map<?, ?> map) {
            Object units = map.get("units");
            if (units instanceof List<?> list) {
                return asRecordList(list);
            }
        }

        throw new IllegalArgumentException("Expected a JSON array or an object with a 'units' array.");
    }

    private List<Map<String, Object>> asRecordList(List<?> list) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Each record in the JSON file must be an object.");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) map;
            records.add(casted);
        }
        return records;
    }

    private List<InventoryRecord> fetchExportRecords() throws SQLException {
        List<InventoryRecord> records = new ArrayList<>();
        String query = """
                select u.unit_id, u.serial_number, u.status, u.equipment_id, u.added_by, u.assigned_to, u.created_at,
                       e.equipment_name, e.brand, e.model, e.category_id, c.category_name
                from units u
                join equipment e on e.equipment_id = u.equipment_id
                left join categories c on c.category_id = e.category_id
                order by u.unit_id
                """;

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                LocalDateTime createdAt = resultSet.getObject("created_at", LocalDateTime.class);
                records.add(new InventoryRecord(
                        resultSet.getInt("unit_id"),
                        resultSet.getString("serial_number"),
                        resultSet.getString("status"),
                        resultSet.getInt("equipment_id"),
                        resultSet.getString("equipment_name"),
                        resultSet.getString("brand"),
                        resultSet.getString("model"),
                        resultSet.getInt("category_id"),
                        resultSet.getString("category_name"),
                        resultSet.getInt("added_by"),
                        (Integer) resultSet.getObject("assigned_to"),
                        createdAt == null ? null : EXPORT_FORMAT.format(createdAt)
                ));
            }
        }

        return records;
    }

    private String toStringValue(Map<String, Object> rawRecord, String... keys) {
        for (String key : keys) {
            Object value = rawRecord.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
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
            if (value == null) {
                continue;
            }
            if (value instanceof Number number) {
                return number.intValue();
            }
            String text = String.valueOf(value).trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return null;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value, EXPORT_FORMAT);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private int currentUserId() {
        if (!credentialManager.exists()) {
            return Integer.parseInt(FALLBACK_USER_ID);
        }
        try {
            Properties props = credentialManager.load();
            String userId = props.getProperty("emp_id");
            if (userId == null || userId.isBlank()) {
                return Integer.parseInt(FALLBACK_USER_ID);
            }
            return Integer.parseInt(userId);
        } catch (Exception e) {
            return Integer.parseInt(FALLBACK_USER_ID);
        }
    }

    private boolean isDuplicateOnly(List<String> issues) {
        return !issues.isEmpty() && issues.stream().allMatch(issue -> issue.toLowerCase().contains("duplicate")
                || issue.toLowerCase().contains("already exists"));
    }

    private boolean isDuplicateException(SQLException exception) {
        String message = exception.getMessage();
        return message != null && message.toLowerCase().contains("duplicate");
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + escape(value) + "\"";
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    public record ExportSummary(Path targetFile, int recordCount) {}

    public record ImportPreview(List<PreviewRecord> records, int validCount, int invalidCount) {}

    public record ImportExecution(int importedCount, int invalidCount, List<String> issues) {}

    public record PreviewRecord(int rowNumber, NormalizedRecord normalizedRecord, List<String> issues) {
        public Unit toUnit() {
            return normalizedRecord.toUnit();
        }
    }

    public record NormalizedRecord(int rowNumber, int equipmentId, String serialNumber, String status, int addedBy,
                                   LocalDateTime createdAt, Integer assignedTo) {
        public Unit toUnit() {
            return new Unit(0, equipmentId, serialNumber, status, addedBy, createdAt, assignedTo);
        }
    }

    public record InventoryRecord(int unitId, String serialNumber, String status, int equipmentId, String equipmentName,
                                  String brand, String model, int categoryId, String categoryName, int addedBy,
                                  Integer assignedTo, String createdAt) {}
}
