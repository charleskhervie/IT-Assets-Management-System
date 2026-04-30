package ui.service;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dao.model.Unit;
import dao.model.Transaction;

public class TableExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Export filtered units list to PDF
     */
    public static void exportUnitsToPdf(Path targetFile, List<Unit> units, String filterDescription) throws IOException {
        try (OutputStream output = Files.newOutputStream(targetFile)) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
            Font sectionFont = new Font(Font.HELVETICA, 13, Font.BOLD, Color.DARK_GRAY);
            Font textFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

            document.add(new Paragraph("Units List Export", titleFont));
            document.add(new Paragraph("Generated: " + LocalDateTime.now().format(DATE_TIME_FORMATTER), textFont));
            if (filterDescription != null && !filterDescription.isBlank()) {
                document.add(new Paragraph("Filter: " + filterDescription, textFont));
            }
            document.add(new Paragraph("Total Records: " + units.size(), textFont));
            document.add(new Paragraph(" "));

            String[] headers = {"ID", "Serial No.", "Equipment", "Added By", "Status", "Assigned To"};
            List<String[]> rows = units.stream().map(unit -> new String[]{
                String.valueOf(unit.getUnitId()),
                unit.getSerialNumber(),
                unit.getEquipmentName() != null ? unit.getEquipmentName() : "Unknown",
                unit.getAddedByName() != null ? unit.getAddedByName() : "Unknown",
                unit.getStatus(),
                unit.getAssignedToName() != null ? unit.getAssignedToName() : "-"
            }).toList();

            addTableSection(document, "Units", sectionFont, headers, rows, textFont);
            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to export units to PDF", e);
        }
    }

    /**
     * Export filtered transactions list to PDF
     */
    public static void exportTransactionsToPdf(Path targetFile, List<Transaction> transactions, String filterDescription) throws IOException {
        try (OutputStream output = Files.newOutputStream(targetFile)) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
            Font sectionFont = new Font(Font.HELVETICA, 13, Font.BOLD, Color.DARK_GRAY);
            Font textFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

            document.add(new Paragraph("Transactions Export", titleFont));
            document.add(new Paragraph("Generated: " + LocalDateTime.now().format(DATE_TIME_FORMATTER), textFont));
            if (filterDescription != null && !filterDescription.isBlank()) {
                document.add(new Paragraph("Filter: " + filterDescription, textFont));
            }
            document.add(new Paragraph("Total Records: " + transactions.size(), textFont));
            document.add(new Paragraph(" "));

            String[] headers = {"ID", "Unit ID", "Equipment", "Borrowed By", "Processed By", "Borrow Date", "Return Date", "Status", "Remarks"};
            List<String[]> rows = transactions.stream().map(trans -> new String[]{
                String.valueOf(trans.getTransactionId()),
                String.valueOf(trans.getUnitId()),
                trans.getEquipmentName() != null ? trans.getEquipmentName() : "Unknown",
                trans.getBorrowedByName() != null ? trans.getBorrowedByName() : "Unknown",
                trans.getProcessedByName() != null ? trans.getProcessedByName() : "-",
                formatDateTime(trans.getBorrowedDate()),
                formatDateTime(trans.getReturnDate()),
                trans.getStatus(),
                trans.getRemarks() != null ? trans.getRemarks() : ""
            }).toList();

            addTableSection(document, "Transactions", sectionFont, headers, rows, textFont);
            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to export transactions to PDF", e);
        }
    }

    private static void addTableSection(Document document, String title, Font sectionFont, 
                                       String[] headers, List<String[]> rows, Font textFont) throws IOException {
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

    private static PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(220, 220, 220));
        return cell;
    }

    private static PdfPCell createCell(String text, Font font) {
        return new PdfPCell(new Phrase(text == null ? "" : text, font));
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }
}
