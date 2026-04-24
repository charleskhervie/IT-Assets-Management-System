package ui.service;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dao.impl.CategoryDAOImpl;
import dao.impl.DepartmentDAOImpl;
import dao.impl.EmployeeDAOImpl;
import dao.impl.EquipmentDAOImpl;
import dao.impl.TransactionDAOImpl;
import dao.impl.UnitDAOImpl;
import dao.intfc.CategoryDAO;
import dao.intfc.DepartmentDAO;
import dao.intfc.EmployeeDAO;
import dao.intfc.EquipmentDAO;
import dao.intfc.TransactionDAO;
import dao.intfc.UnitDAO;
import dao.model.Category;
import dao.model.Department;
import dao.model.Employee;
import dao.model.Equipment;
import dao.model.Transaction;
import dao.model.Unit;

public class ReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UnitDAO unitDAO = new UnitDAOImpl();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final CategoryDAO categoryDAO = new CategoryDAOImpl();
    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();
    private final DepartmentDAO departmentDAO = new DepartmentDAOImpl();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();

    public ReportSnapshot loadReportData() throws SQLException {
        List<Unit> units = unitDAO.findAllDisplay();
        List<Equipment> equipments = equipmentDAO.findAll();
        List<Category> categories = categoryDAO.findAll();
        List<Employee> employees = employeeDAO.findAll();
        List<Department> departments = departmentDAO.findAll();
        List<Transaction> transactions = transactionDAO.findAllDisplay();

        Map<Integer, Equipment> equipmentById = equipments.stream()
            .collect(Collectors.toMap(Equipment::getEquipmentId, equipment -> equipment));
        Map<Integer, String> categoryNameById = categories.stream()
            .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName));
        Map<Integer, String> employeeNameById = employees.stream()
            .collect(Collectors.toMap(Employee::getEmpId, Employee::getFullName));
        Map<Integer, String> departmentNameById = departments.stream()
            .collect(Collectors.toMap(Department::getDepartmentId, Department::getDepartmentName));

        Map<Integer, Unit> unitById = units.stream()
            .collect(Collectors.toMap(Unit::getUnitId, unit -> unit));

        Map<String, MutableCounts> categoryCounts = new LinkedHashMap<>();
        Map<String, MutableCounts> equipmentCounts = new LinkedHashMap<>();
        Map<String, Integer> statusCounts = new LinkedHashMap<>();

        for (Unit unit : units) {
            Equipment equipment = equipmentById.get(unit.getEquipmentId());
            String categoryName = equipment == null ? "Unknown" : categoryNameById.getOrDefault(equipment.getCategoryId(), "Unknown");
            String equipmentName = equipment == null ? "Unknown Equipment" : equipment.getEquipmentName();
            String normalizedStatus = normalizeUnitStatus(unit.getStatus());

            categoryCounts.computeIfAbsent(categoryName, ignored -> new MutableCounts()).increment(normalizedStatus);
            equipmentCounts.computeIfAbsent(equipmentName, ignored -> new MutableCounts()).increment(normalizedStatus);
            statusCounts.merge(displayStatus(normalizedStatus), 1, Integer::sum);
        }

        List<CategorySummaryRow> categoryRows = categoryCounts.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
            .map(entry -> new CategorySummaryRow(
                entry.getKey(),
                entry.getValue().total(),
                entry.getValue().available,
                entry.getValue().checkedOut,
                entry.getValue().maintenance,
                entry.getValue().other
            ))
            .toList();

        List<EquipmentSummaryRow> equipmentRows = equipmentCounts.entrySet().stream()
            .sorted(Map.Entry.<String, MutableCounts>comparingByValue(Comparator.comparingInt(MutableCounts::total).reversed())
                .thenComparing(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)))
            .map(entry -> new EquipmentSummaryRow(
                entry.getKey(),
                entry.getValue().total(),
                entry.getValue().available,
                entry.getValue().checkedOut,
                entry.getValue().maintenance,
                entry.getValue().other
            ))
            .toList();

        List<TransactionSummaryRow> transactionRows = transactions.stream()
            .sorted(Comparator.comparing(Transaction::getBorrowedDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .map(transaction -> {
                Unit unit = unitById.get(transaction.getUnitId());
                String unitLabel = unit == null
                    ? "Unit #" + transaction.getUnitId()
                    : unit.getSerialNumber() + " (Unit #" + unit.getUnitId() + ")";
                String borrowedByName = transaction.getBorrowedByName();
                if (borrowedByName == null || borrowedByName.isBlank()) {
                    borrowedByName = employeeNameById.getOrDefault(transaction.getBorrower(), "Unknown");
                }
                String processedByName = transaction.getProcessedByName();
                if (processedByName == null || processedByName.isBlank()) {
                    processedByName = transaction.getProcessedBy() > 0
                        ? employeeNameById.getOrDefault(transaction.getProcessedBy(), "-")
                        : "-";
                }
                return new TransactionSummaryRow(
                    transaction.getTransactionId(),
                    unitLabel,
                    borrowedByName,
                    processedByName,
                    formatDateTime(transaction.getBorrowedDate()),
                    formatDateTime(transaction.getReturnDate()),
                    transaction.getStatus() == null ? "-" : transaction.getStatus(),
                    transaction.getRemarks() == null ? "" : transaction.getRemarks()
                );
            })
            .toList();

        List<StatusSummaryRow> statusRows = statusCounts.entrySet().stream()
            .map(entry -> new StatusSummaryRow(entry.getKey(), entry.getValue()))
            .toList();

        int totalUnits = units.size();
        int availableUnits = statusCounts.getOrDefault("Available", 0);
        int checkedOutUnits = statusCounts.getOrDefault("Checked Out", 0);
        int maintenanceUnits = statusCounts.getOrDefault("Maintenance", 0);
        int totalTransactions = transactions.size();

        return new ReportSnapshot(
            totalUnits,
            availableUnits,
            checkedOutUnits,
            maintenanceUnits,
            totalTransactions,
            categoryRows,
            equipmentRows,
            transactionRows,
            statusRows
        );
    }

    public ExportSummary exportToCsv(Path targetFile) throws IOException, SQLException {
        ReportSnapshot snapshot = loadReportData();
        StringBuilder csv = new StringBuilder();

        csv.append("Report Summary\n");
        csv.append("Metric,Value\n");
        csv.append("Total Units,").append(snapshot.totalUnits()).append('\n');
        csv.append("Available Units,").append(snapshot.availableUnits()).append('\n');
        csv.append("Checked Out Units,").append(snapshot.checkedOutUnits()).append('\n');
        csv.append("Maintenance Units,").append(snapshot.maintenanceUnits()).append('\n');
        csv.append("Total Transactions,").append(snapshot.totalTransactions()).append('\n');

        csv.append("\nCategory Summary\n");
        csv.append("Category,Total,Available,Checked Out,Maintenance,Other\n");
        for (CategorySummaryRow row : snapshot.categoryRows()) {
            csv.append(escape(row.categoryName())).append(',')
                .append(row.totalUnits()).append(',')
                .append(row.available()).append(',')
                .append(row.checkedOut()).append(',')
                .append(row.maintenance()).append(',')
                .append(row.other())
                .append('\n');
        }

        csv.append("\nEquipment Summary\n");
        csv.append("Equipment,Total,Available,Checked Out,Maintenance,Other\n");
        for (EquipmentSummaryRow row : snapshot.equipmentRows()) {
            csv.append(escape(row.equipmentName())).append(',')
                .append(row.totalUnits()).append(',')
                .append(row.available()).append(',')
                .append(row.checkedOut()).append(',')
                .append(row.maintenance()).append(',')
                .append(row.other())
                .append('\n');
        }

        csv.append("\nRecent Transactions\n");
        csv.append("Transaction ID,Unit,Borrowed By,Processed By,Borrow Date,Return Date,Status,Remarks\n");
        for (TransactionSummaryRow row : snapshot.transactionRows()) {
            csv.append(row.transactionId()).append(',')
                .append(escape(row.unitLabel())).append(',')
                .append(escape(row.borrowedByName())).append(',')
                .append(escape(row.processedByName())).append(',')
                .append(escape(row.borrowedDate())).append(',')
                .append(escape(row.returnDate())).append(',')
                .append(escape(row.status())).append(',')
                .append(escape(row.remarks()))
                .append('\n');
        }

        Files.writeString(targetFile, csv.toString(), StandardCharsets.UTF_8);
        return new ExportSummary(targetFile, snapshot.totalUnits() + snapshot.totalTransactions());
    }

    public ExportSummary exportToJson(Path targetFile) throws IOException, SQLException {
        ReportSnapshot snapshot = loadReportData();
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"summary\": {\n");
        json.append("    \"totalUnits\": ").append(snapshot.totalUnits()).append(",\n");
        json.append("    \"availableUnits\": ").append(snapshot.availableUnits()).append(",\n");
        json.append("    \"checkedOutUnits\": ").append(snapshot.checkedOutUnits()).append(",\n");
        json.append("    \"maintenanceUnits\": ").append(snapshot.maintenanceUnits()).append(",\n");
        json.append("    \"totalTransactions\": ").append(snapshot.totalTransactions()).append("\n");
        json.append("  },\n");

        json.append("  \"statusBreakdown\": [\n");
        for (int i = 0; i < snapshot.statusRows().size(); i++) {
            StatusSummaryRow row = snapshot.statusRows().get(i);
            json.append("    {\"status\": ").append(jsonString(row.status()))
                .append(", \"count\": ").append(row.count()).append("}");
            if (i < snapshot.statusRows().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");

        json.append("  \"categorySummary\": [\n");
        for (int i = 0; i < snapshot.categoryRows().size(); i++) {
            CategorySummaryRow row = snapshot.categoryRows().get(i);
            json.append("    {\"categoryName\": ").append(jsonString(row.categoryName()))
                .append(", \"totalUnits\": ").append(row.totalUnits())
                .append(", \"available\": ").append(row.available())
                .append(", \"checkedOut\": ").append(row.checkedOut())
                .append(", \"maintenance\": ").append(row.maintenance())
                .append(", \"other\": ").append(row.other())
                .append("}");
            if (i < snapshot.categoryRows().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");

        json.append("  \"equipmentSummary\": [\n");
        for (int i = 0; i < snapshot.equipmentRows().size(); i++) {
            EquipmentSummaryRow row = snapshot.equipmentRows().get(i);
            json.append("    {\"equipmentName\": ").append(jsonString(row.equipmentName()))
                .append(", \"totalUnits\": ").append(row.totalUnits())
                .append(", \"available\": ").append(row.available())
                .append(", \"checkedOut\": ").append(row.checkedOut())
                .append(", \"maintenance\": ").append(row.maintenance())
                .append(", \"other\": ").append(row.other())
                .append("}");
            if (i < snapshot.equipmentRows().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");

        json.append("  \"recentTransactions\": [\n");
        for (int i = 0; i < snapshot.transactionRows().size(); i++) {
            TransactionSummaryRow row = snapshot.transactionRows().get(i);
            json.append("    {")
                .append("\"transactionId\": ").append(row.transactionId()).append(", ")
                .append("\"unitLabel\": ").append(jsonString(row.unitLabel())).append(", ")
                .append("\"borrowedByName\": ").append(jsonString(row.borrowedByName())).append(", ")
                .append("\"processedByName\": ").append(jsonString(row.processedByName())).append(", ")
                .append("\"borrowedDate\": ").append(jsonString(row.borrowedDate())).append(", ")
                .append("\"returnDate\": ").append(jsonString(row.returnDate())).append(", ")
                .append("\"status\": ").append(jsonString(row.status())).append(", ")
                .append("\"remarks\": ").append(jsonString(row.remarks()))
                .append("}");
            if (i < snapshot.transactionRows().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");

        Files.writeString(targetFile, json.toString(), StandardCharsets.UTF_8);
        return new ExportSummary(targetFile, snapshot.totalUnits() + snapshot.totalTransactions());
    }

    public ExportSummary exportToPdf(Path targetFile) throws IOException, SQLException {
        ReportSnapshot snapshot = loadReportData();
        try (OutputStream output = Files.newOutputStream(targetFile)) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
            Font sectionFont = new Font(Font.HELVETICA, 13, Font.BOLD, Color.DARK_GRAY);
            Font textFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

            document.add(new Paragraph("IT Assets Management System Report", titleFont));
            document.add(new Paragraph("Generated: " + LocalDateTime.now().format(DATE_TIME_FORMATTER), textFont));
            document.add(new Paragraph(" "));

            addSummarySection(document, sectionFont, textFont, snapshot);
            addCategorySection(document, sectionFont, textFont, snapshot);
            addEquipmentSection(document, sectionFont, textFont, snapshot);
            addTransactionSection(document, sectionFont, textFont, snapshot);

            document.close();
        }

        return new ExportSummary(targetFile, snapshot.totalUnits() + snapshot.totalTransactions());
    }

    private void addSummarySection(Document document, Font sectionFont, Font textFont, ReportSnapshot snapshot) throws IOException {
        try {
            document.add(new Paragraph("Summary", sectionFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(45f);
            addKeyValueRow(table, "Total Units", String.valueOf(snapshot.totalUnits()), textFont);
            addKeyValueRow(table, "Available Units", String.valueOf(snapshot.availableUnits()), textFont);
            addKeyValueRow(table, "Checked Out Units", String.valueOf(snapshot.checkedOutUnits()), textFont);
            addKeyValueRow(table, "Maintenance Units", String.valueOf(snapshot.maintenanceUnits()), textFont);
            addKeyValueRow(table, "Total Transactions", String.valueOf(snapshot.totalTransactions()), textFont);
            document.add(table);
            document.add(new Paragraph(" "));
        } catch (Exception e) {
            throw new IOException("Failed to build PDF summary section", e);
        }
    }

    private void addCategorySection(Document document, Font sectionFont, Font textFont, ReportSnapshot snapshot) throws IOException {
        addTableSection(document, "Category Summary", sectionFont,
            new String[] {"Category", "Total", "Available", "Checked Out", "Maintenance", "Other"},
            snapshot.categoryRows().stream().map(row -> new String[] {
                row.categoryName(),
                String.valueOf(row.totalUnits()),
                String.valueOf(row.available()),
                String.valueOf(row.checkedOut()),
                String.valueOf(row.maintenance()),
                String.valueOf(row.other())
            }).toList(), textFont);
    }

    private void addEquipmentSection(Document document, Font sectionFont, Font textFont, ReportSnapshot snapshot) throws IOException {
        addTableSection(document, "Equipment Summary", sectionFont,
            new String[] {"Equipment", "Total", "Available", "Checked Out", "Maintenance", "Other"},
            snapshot.equipmentRows().stream().map(row -> new String[] {
                row.equipmentName(),
                String.valueOf(row.totalUnits()),
                String.valueOf(row.available()),
                String.valueOf(row.checkedOut()),
                String.valueOf(row.maintenance()),
                String.valueOf(row.other())
            }).toList(), textFont);
    }

    private void addTransactionSection(Document document, Font sectionFont, Font textFont, ReportSnapshot snapshot) throws IOException {
        addTableSection(document, "Recent Transactions", sectionFont,
            new String[] {"ID", "Unit", "Borrowed By", "Processed By", "Borrow Date", "Return Date", "Status", "Remarks"},
            snapshot.transactionRows().stream().map(row -> new String[] {
                String.valueOf(row.transactionId()),
                row.unitLabel(),
                row.borrowedByName(),
                row.processedByName(),
                row.borrowedDate(),
                row.returnDate(),
                row.status(),
                row.remarks()
            }).toList(), textFont);
    }

    private void addTableSection(Document document, String title, Font sectionFont, String[] headers, List<String[]> rows, Font textFont) throws IOException {
        try {
            document.add(new Paragraph(title, sectionFont));
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100f);
            for (String header : headers) {
                table.addCell(createHeaderCell(header, textFont));
            }
            for (String[] row : rows) {
                for (String value : row) {
                    table.addCell(createCell(value, textFont));
                }
            }
            document.add(table);
            document.add(new Paragraph(" "));
        } catch (Exception e) {
            throw new IOException("Failed to build PDF table section: " + title, e);
        }
    }

    private void addKeyValueRow(PdfPTable table, String key, String value, Font textFont) {
        table.addCell(createHeaderCell(key, textFont));
        table.addCell(createCell(value, textFont));
    }

    private PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(220, 220, 220));
        return cell;
    }

    private PdfPCell createCell(String text, Font font) {
        return new PdfPCell(new Phrase(text == null ? "" : text, font));
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return '"' + text.replace("\"", "\"\"") + '"';
        }
        return text;
    }

    private String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\"";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private String normalizeUnitStatus(String status) {
        if (status == null) {
            return "other";
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("avail")) {
            return "available";
        }
        if (normalized.contains("maint")) {
            return "maintenance";
        }
        if (normalized.contains("check") || normalized.contains("deploy") || normalized.contains("borrow")) {
            return "checked-out";
        }
        return "other";
    }

    private String displayStatus(String normalizedStatus) {
        return switch (normalizedStatus) {
            case "available" -> "Available";
            case "checked-out" -> "Checked Out";
            case "maintenance" -> "Maintenance";
            default -> "Other";
        };
    }

    private static final class MutableCounts {
        private int available;
        private int checkedOut;
        private int maintenance;
        private int other;

        private void increment(String normalizedStatus) {
            switch (normalizedStatus) {
                case "available" -> available++;
                case "checked-out" -> checkedOut++;
                case "maintenance" -> maintenance++;
                default -> other++;
            }
        }

        private int total() {
            return available + checkedOut + maintenance + other;
        }
    }

    public record StatusSummaryRow(String status, int count) {}

    public record CategorySummaryRow(
        String categoryName,
        int totalUnits,
        int available,
        int checkedOut,
        int maintenance,
        int other
    ) {}

    public record EquipmentSummaryRow(
        String equipmentName,
        int totalUnits,
        int available,
        int checkedOut,
        int maintenance,
        int other
    ) {}

    public record TransactionSummaryRow(
        int transactionId,
        String unitLabel,
        String borrowedByName,
        String processedByName,
        String borrowedDate,
        String returnDate,
        String status,
        String remarks
    ) {}

    public record ReportSnapshot(
        int totalUnits,
        int availableUnits,
        int checkedOutUnits,
        int maintenanceUnits,
        int totalTransactions,
        List<CategorySummaryRow> categoryRows,
        List<EquipmentSummaryRow> equipmentRows,
        List<TransactionSummaryRow> transactionRows,
        List<StatusSummaryRow> statusRows
    ) {}
}