package ui.service;

import java.nio.file.Path;

public record ImportSummary(Path sourceFile, int importedCount, int skippedCount, int issueCount) {
    public int recordCount() {
        return importedCount;
    }
}
