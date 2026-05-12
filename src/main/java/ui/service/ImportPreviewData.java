package ui.service;

import java.util.List;
import java.util.Map;
/**
 * Data record for Import Preview Analysis.
 * 
 * Provides a comprehensive snapshot of the data quality and volume 
 * within a source file prior to final database commitment.
 * 
 * - Maintains a detailed collection of {@link ImportIssue} objects to 
 *   inform the user of specific data conflicts or structural errors.
 * - Facilitates pre-import decision-making by allowing UI components 
 *   to render a summary of potential changes and risks.
 */
public record ImportPreviewData(
        int totalRecords,
        int validRecords,
        int issueCount,
        Map<String, Integer> sectionCounts,
        List<ImportIssue> issues) {
}
