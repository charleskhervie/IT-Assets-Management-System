package ui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ui.service.ExportSummary;
import ui.service.ImportIssue;
import ui.service.ImportPreviewData;
import ui.service.ImportSummary;
import ui.service.ImportValidationResult;
import ui.service.InventoryCsvService;
import ui.service.InventoryJsonService;
import ui.service.InventorySqlService;
import ui.util.NavigationUtil;

public class ImportExportScreen implements Initializable {

    @FXML private CheckBox exportJson;
    @FXML private CheckBox exportCsv;
    @FXML private CheckBox exportSql;

    @FXML private Button confirmExportButton;
    @FXML private Button confirmImportButton;
    @FXML private Label fileNameLabel;
    @FXML private Label rowCountLabel;
    @FXML private Label totalRowsValueLabel;
    @FXML private Label validRowsValueLabel;
    @FXML private Label issuesValueLabel;
    @FXML private VBox previewRowsContainer;

    private final InventoryJsonService jsonService = new InventoryJsonService();
    private final InventoryCsvService csvService = new InventoryCsvService();
    private final InventorySqlService sqlService = new InventorySqlService();
    private File selectedImportFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initExportCheckboxes();
        enforceSingleSelection(exportJson, exportCsv, exportSql);
        enforceSingleSelection(exportCsv, exportJson, exportSql);
        enforceSingleSelection(exportSql, exportJson, exportCsv);
    }

    private void enforceSingleSelection(CheckBox selected, CheckBox... others) {
        selected.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                for (CheckBox cb : others) {
                    cb.setSelected(false);
                }
            }
        });
    }

    private void initExportCheckboxes() {
        setupExportCheckbox(exportJson, defaultStyle());
        setupExportCheckbox(exportCsv, defaultStyle());
        setupExportCheckbox(exportSql, defaultStyle());
    }

    private String defaultStyle() {
        return "-fx-background-color: #e8e8e8; -fx-background-radius: 5; -fx-padding: 10 12 10 12;";
    }

    private void setupExportCheckbox(CheckBox checkbox, String defaultStyle) {
        HBox parent = (HBox) checkbox.getParent();

        parent.setOnMouseClicked(e -> checkbox.setSelected(!checkbox.isSelected()));

        checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                parent.setStyle("-fx-background-color: #d6ead6; -fx-background-radius: 5; -fx-padding: 10 12 10 12; -fx-border-color: #7ec87e; -fx-border-width: 1; -fx-border-radius: 5;");
            } else {
                parent.setStyle(defaultStyle);
            }
        });

        parent.setStyle(
            checkbox.isSelected()
                ? "-fx-background-color: #d6ead6; -fx-background-radius: 5; -fx-padding: 10 12 10 12; -fx-border-color: #7ec87e; -fx-border-width: 1; -fx-border-radius: 5;"
                : defaultStyle
        );
    }

    @FXML
    private void handleUnits(ActionEvent e) {
        NavigationUtil.loadIntoDashboard(e, "/fxml/unitsList.fxml");
    }

    @FXML
    private void handleImportExport(ActionEvent e) {
        NavigationUtil.loadScene(e, "/fxml/importExport.fxml");
    }

    @FXML
    private void handleTransactions(ActionEvent e) {
        NavigationUtil.loadIntoDashboard(e, "/fxml/Transaction.fxml");
    }

    @FXML
    private void handleEmployees(ActionEvent e) {
        NavigationUtil.loadScene(e, "/fxml/Employee.fxml");
    }

    @FXML
    private void handleReports(ActionEvent e) {
        NavigationUtil.loadScene(e, "/fxml/report.fxml");
    }

    @FXML
    private void handleDashboard(ActionEvent e) {
        NavigationUtil.loadScene(e, "/fxml/Dashboard.fxml");
    }

    @FXML
    private void handleBackToDashboard(ActionEvent e) {
        NavigationUtil.loadScene(e, "/fxml/Dashboard.fxml");
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Exit");
        confirmAlert.setHeaderText("Exit to Login");
        confirmAlert.setContentText("Are you sure you want to exit and return to the login page?");

        Optional<ButtonType> response = confirmAlert.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) {
            return;
        }

        NavigationUtil.loadScene(event, "/fxml/login.fxml");
    }

    @FXML
    private void handleConfirmExport(ActionEvent event) {
        if (!exportJson.isSelected() && !exportCsv.isSelected() && !exportSql.isSelected()) {
            showError("No Format Selected", "Select at least one format.");
            return;
        }

        if (exportJson.isSelected()) {
            exportFormat("JSON", "inventory-export.json", "*.json",
                path -> jsonService.exportToJson(path));
        }

        if (exportCsv.isSelected()) {
            exportFormat("CSV", "inventory-export.csv", "*.csv",
                path -> csvService.exportToCsv(path));
        }

        if (exportSql.isSelected()) {
            exportFormat("SQL", "inventory-export.sql", "*.sql",
                path -> sqlService.exportToSql(path));
        }
    }

    @FunctionalInterface
    private interface ExportAction {
        ExportSummary execute(Path path) throws IOException;
    }

    private void exportFormat(String format, String defaultName, String ext, ExportAction action) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export " + format);
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(format + " Files", ext));

        File file = fc.showSaveDialog(confirmExportButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            ExportSummary result = action.execute(file.toPath());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(format + " Export Complete");
            alert.setHeaderText(null);
            alert.setContentText("Exported " + result.recordCount() + " record(s) to:\n" + result.targetFile());
            alert.showAndWait();
        } catch (IOException e) {
            showError(format + " Export Failed", e.getMessage());
        }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void handleChooseImportFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Import File");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported Files", "*.json", "*.csv", "*.sql"),
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("SQL Files", "*.sql"));

        File file = fc.showOpenDialog(confirmImportButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            ImportPreviewData preview = previewImport(file.toPath());
            selectedImportFile = file;
            updateImportPreview(file.getName(), preview);
        } catch (IOException | IllegalArgumentException e) {
            selectedImportFile = null;
            resetImportPreview();
            showError("Import Preview Failed", e.getMessage());
        }
    }

    @FXML
    private void handleConfirmImport(ActionEvent event) {
        if (selectedImportFile == null) {
            showError("No File Selected", "Choose a JSON, CSV, or SQL file to import.");
            return;
        }

        try {
            ImportValidationResult validation = validateImport(selectedImportFile.toPath());
            if (validation.issueCount() > 0) {
                updateImportPreview(selectedImportFile.getName(), validation.toPreviewData());
                showError("Import Blocked",
                        "Validation found " + validation.issueCount()
                        + " issue(s). Fix the file and try again.");
                return;
            }

            ImportSummary result = importSelectedFile(selectedImportFile.toPath());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Complete");
            alert.setHeaderText(null);
            alert.setContentText("Imported " + result.importedCount()
                    + " record(s), skipped " + result.skippedCount()
                    + " duplicate(s) from:\n" + result.sourceFile());
            alert.showAndWait();
        } catch (IOException | IllegalArgumentException e) {
            showError("Import Failed", e.getMessage());
        }
    }

    private ImportPreviewData previewImport(Path path) throws IOException {
        return validateImport(path).toPreviewData();
    }

    private ImportValidationResult validateImport(Path path) throws IOException {
        String extension = getExtension(path);
        return switch (extension) {
            case "json" -> jsonService.validateImport(path, false);
            case "csv"  -> csvService.validateImport(path, false);
            case "sql"  -> sqlService.validateImport(path, false);
            default -> throw new IllegalArgumentException("Unsupported import file type: " + extension);
        };
    }

    private ImportSummary importSelectedFile(Path path) throws IOException {
        String extension = getExtension(path);
        return switch (extension) {
            case "json" -> jsonService.importFromJson(path, false);
            case "csv"  -> csvService.importFromCsv(path, false);
            case "sql"  -> sqlService.importFromSql(path, false);
            default -> throw new IllegalArgumentException("Unsupported import file type: " + extension);
        };
    }

    private String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new IllegalArgumentException("Import file must have a .json, .csv, or .sql extension.");
        }
        return fileName.substring(index + 1).toLowerCase();
    }

    private void updateImportPreview(String fileName, ImportPreviewData preview) {
        fileNameLabel.setText(fileName);
        rowCountLabel.setText(preview.totalRecords() + " rows");
        totalRowsValueLabel.setText(String.valueOf(preview.totalRecords()));
        validRowsValueLabel.setText(String.valueOf(preview.validRecords()));
        issuesValueLabel.setText(String.valueOf(preview.issueCount()));

        previewRowsContainer.getChildren().clear();
        for (Map.Entry<String, Integer> entry : preview.sectionCounts().entrySet()) {
            Label label = new Label(formatPreviewLabel(entry.getKey(), entry.getValue()));
            label.setStyle("-fx-text-fill: #222222; -fx-font-size: 12px;");
            previewRowsContainer.getChildren().add(label);
        }

        if (!preview.issues().isEmpty()) {
            for (ImportIssue issue : preview.issues()) {
                Label label = new Label(issue.location() + ": " + issue.message());
                label.setWrapText(true);
                label.setStyle("-fx-text-fill: #a12626; -fx-font-size: 12px;");
                previewRowsContainer.getChildren().add(label);
            }
        } else if (previewRowsContainer.getChildren().isEmpty()) {
            Label label = new Label("No records found in the selected file.");
            label.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
            previewRowsContainer.getChildren().add(label);
        }
    }

    private String formatPreviewLabel(String section, int count) {
        return String.format("%s: %d record(s)", capitalize(section), count);
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private void resetImportPreview() {
        fileNameLabel.setText("No file imported.");
        rowCountLabel.setText("0 rows");
        totalRowsValueLabel.setText("0");
        validRowsValueLabel.setText("0");
        issuesValueLabel.setText("0");
        previewRowsContainer.getChildren().clear();
        Label label = new Label("Import a JSON, CSV, or SQL file to preview records.");
        label.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        previewRowsContainer.getChildren().add(label);
    }
}
