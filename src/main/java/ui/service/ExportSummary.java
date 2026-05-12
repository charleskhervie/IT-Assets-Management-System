package ui.service;

import java.nio.file.Path;
/**
 * Data record for Export Operation Summaries.
 * 
 * Provides a lightweight container for capturing the outcome of an 
 * inventory data extraction process.
 * 
 * - Tracks the total number of records successfully written to ensure 
 *   data parity between the source database and the output file.
 * - Utilizes the Java record feature to provide an immutable, 
 *   thread-safe snapshot of the export transaction results.
 */
public record ExportSummary(Path targetFile, int recordCount) {}