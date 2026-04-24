package ui.service;

import java.util.List;
import java.util.Map;

public record ImportPreviewData(
        int totalRecords,
        int validRecords,
        int issueCount,
        Map<String, Integer> sectionCounts,
        List<ImportIssue> issues) {
}
