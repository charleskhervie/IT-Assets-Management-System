package ui.service;

import java.util.List;
import java.util.Map;

public record ImportValidationResult(
        InventorySnapshot snapshot,
        int totalRecords,
        Map<String, Integer> sectionCounts,
        List<ImportIssue> issues) {

    public int validRecords() {
        return snapshot.totalRecords();
    }

    public int issueCount() {
        return issues.size();
    }

    public boolean hasCriticalIssues() {
        for (ImportIssue issue : issues) {
            if (issue.critical()) {
                return true;
            }
        }
        return false;
    }

    public ImportPreviewData toPreviewData() {
        return new ImportPreviewData(totalRecords, validRecords(), issueCount(), sectionCounts, issues);
    }
}
