package ui.service;

import java.util.List;
import java.util.Map;
/**
 * Data record for Import Validation Results.
 * 
 * Acts as the comprehensive output of the pre-import validation engine, 
 * encapsulating both the parsed data snapshot and its quality metrics.
 * 
 * - Holds a reference to the {@link InventorySnapshot}, representing the 
 *   entire dataset extracted from the source file.
 * - Provides derived metrics to calculate the volume of valid records 
 *   versus the total count of detected {@link ImportIssue} objects.
 * - Implements logic to scan for critical failures that would 
 *   programmatically prevent the progression of a database write operation.
 * - Facilitates data transformation by providing a conversion method to 
 *   {@link ImportPreviewData} for UI-focused summary displays.
 * - Leverages the Java record structure to provide an immutable and 
 *   consistent view of data integrity across the service layer.
 */
public record ImportValidationResult(
        InventorySnapshot snapshot,
        int totalRecords,
        Map<String, Integer> sectionCounts,
        List<ImportIssue> issues) {

    public int validRecords() {
        return Math.max(0, totalRecords - issueCount());
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
