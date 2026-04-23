package ui.service;

import java.nio.file.Path;

public record ExportSummary(Path targetFile, int recordCount) {}