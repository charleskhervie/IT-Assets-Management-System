package ui.service;
/**
 * Data record for Import Validation Issues.
 * 
 * Provides a structured container for reporting errors and warnings 
 * encountered during the data importation and validation process.
 * 
 */
public record ImportIssue(String location, String message, boolean critical) {
}
