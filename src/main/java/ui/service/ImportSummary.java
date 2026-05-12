package ui.service;

import java.nio.file.Path;
/**
 * Data record for Import Operation Summaries.
 * 


 * - Logs the total count of validation issues encountered, supporting 
 *   post-import audits and error handling.
 */
public record ImportSummary(Path sourceFile, int importedCount, int skippedCount, int issueCount) {
    public int recordCount() {
        return importedCount;
    }
}
