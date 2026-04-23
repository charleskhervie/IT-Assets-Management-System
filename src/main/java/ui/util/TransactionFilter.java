package ui.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import dao.model.Transaction;

public class TransactionFilter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private TransactionFilter() {}

    public static boolean matches(Transaction transaction, String status, String keyword) {
        return matchesStatus(transaction, status) && matchesKeyword(transaction, keyword);
    }

    private static boolean matchesStatus(Transaction transaction, String status) {
        if (status == null || status.equals("All")) return true;
        return status.equalsIgnoreCase(transaction.getStatus());
    }

    private static boolean matchesKeyword(Transaction transaction, String keyword) {
        if (keyword.isEmpty()) return true;
        return containsKeyword(String.valueOf(transaction.getTransactionId()), keyword)
            || containsKeyword(String.valueOf(transaction.getUnitId()), keyword)
            || containsKeyword(String.valueOf(transaction.getBorrower()), keyword)
            || containsKeyword(String.valueOf(transaction.getProcessedBy()), keyword)
            || containsKeyword(formatDate(transaction.getBorrowedDate()), keyword)
            || containsKeyword(formatDate(transaction.getReturnDate()), keyword)
            || containsKeyword(transaction.getStatus(), keyword)
            || containsKeyword(transaction.getRemarks(), keyword);
    }

    private static String formatDate(LocalDateTime date) {
        return date != null ? date.format(FORMATTER) : "";
    }

    private static boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}