package ui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ui.service.InventoryJsonService;
import ui.service.InventoryJsonService.ImportExecution;
import ui.service.InventoryJsonService.ImportPreview;
import ui.service.InventoryJsonService.PreviewRecord;
import ui.util.NavigationUtil;

public class ImportExportScreen implements Initializable {
    @FXML private Label fileNameLabel;
    @FXML private Label rowCountLabel;
    @FXML private Label totalRowsValueLabel;
    @FXML private Label validRowsValueLabel;
    @FXML private Label issuesValueLabel;
    @FXML private VBox previewRowsContainer;
    @FXML private CheckBox skipDuplicatesCheckBox;
    @FXML private CheckBox validateBeforeImportCheckBox;
    @FXML private CheckBox exportJson;
    @FXML private CheckBox exportCsv;
    @FXML private CheckBox exportSql;
    @FXML private Button confirmImportButton;
    @FXML private Button confirmExportButton;

    private final InventoryJsonService inventoryJsonService = new InventoryJsonService();
    private File selectedImportFile;
    private ImportPreview currentPreview;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updatePreviewState(null, null);
    }

    @FXML private void handleUnits(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/fxml/unitsList.fxml"); 
    }
    @FXML private void handleImportExport(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/importExport.fxml"); 
    }
    @FXML private void handleTransactions(ActionEvent event){ 
        NavigationUtil.loadIntoDashboard(event, "/fxml/Transaction.fxml"); 
    }
    @FXML private void handleEmployees(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Employee.fxml"); 
    }
    @FXML private void handleReports(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/report.fxml"); 
    }
    @FXML private void handleDashboard(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Dashboard.fxml"); 
    }
    @FXML private void handleBackToDashboard(ActionEvent event){ 
        NavigationUtil.loadScene(event, "/fxml/Dashboard.fxml"); 
    }
    @FXML private void handleExit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Exit");
        confirmAlert.setHeaderText("Exit to Login");
        confirmAlert.setContentText("Are you sure you want to exit and return to the login page?");

        Optional<ButtonType> response = confirmAlert.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) return;

        NavigationUtil.loadScene(event, "/fxml/login.fxml");
    }

    @FXML
    private void handleChooseImportFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Inventory JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(confirmImportButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            selectedImportFile = file;
            currentPreview = inventoryJsonService.loadPreview(file.toPath());
            updatePreviewState(file, currentPreview);
        } catch (IOException | RuntimeException | java.sql.SQLException e) {
            selectedImportFile = null;
            currentPreview = null;
            updatePreviewState(null, null);
            showError("Import Preview Failed", e.getMessage());
        }
    }

    @FXML
    private void handleConfirmImport(ActionEvent event) {
        if (selectedImportFile == null || currentPreview == null) {
            showError("No File Selected", "Choose a JSON file first.");
            return;
        }

        if (validateBeforeImportCheckBox.isSelected() && currentPreview.invalidCount() > 0) {
            showError("Validation Failed", "The selected file has issues. Resolve them or uncheck validation before importing.");
            return;
        }

        try {
            ImportExecution result = inventoryJsonService.importRecords(
                    currentPreview,
                    skipDuplicatesCheckBox.isSelected(),
                    validateBeforeImportCheckBox.isSelected()
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Complete");
            alert.setHeaderText(null);
            StringBuilder message = new StringBuilder();
            message.append("Imported ").append(result.importedCount()).append(" record(s).");
            if (result.invalidCount() > 0) {
                message.append("\nSkipped/invalid: ").append(result.invalidCount()).append(".");
            }
            if (!result.issues().isEmpty()) {
                message.append("\n\nIssues:\n").append(String.join("\n", result.issues().stream().limit(5).toList()));
                if (result.issues().size() > 5) {
                    message.append("\n...");
                }
            }
            alert.setContentText(message.toString());
            alert.showAndWait();

            currentPreview = inventoryJsonService.loadPreview(selectedImportFile.toPath());
            updatePreviewState(selectedImportFile, currentPreview);
        } catch (IOException | java.sql.SQLException e) {
            showError("Import Failed", e.getMessage());
        }
    }

    @FXML
    private void handleConfirmExport(ActionEvent event) {
        if (!exportJson.isSelected()) {
            showError("JSON Required", "Select JSON export to continue.");
            return;
        }

        if (exportCsv.isSelected() || exportSql.isSelected()) {
            showError("Unsupported Format", "This screen currently supports JSON export only.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Inventory JSON");
        fileChooser.setInitialFileName("inventory-export.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(confirmExportButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            var summary = inventoryJsonService.exportUnits(ensureJsonExtension(file.toPath()));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Complete");
            alert.setHeaderText(null);
            alert.setContentText("Exported " + summary.recordCount() + " record(s) to:\n" + summary.targetFile());
            alert.showAndWait();
        } catch (IOException | java.sql.SQLException e) {
            showError("Export Failed", e.getMessage());
        }
    }

    private void updatePreviewState(File file, ImportPreview preview) {
        fileNameLabel.setText(file == null ? "No file imported." : file.getName());
        int total = preview == null ? 0 : preview.records().size();
        int valid = preview == null ? 0 : preview.validCount();
        int issues = preview == null ? 0 : preview.invalidCount();

        rowCountLabel.setText(total + " rows");
        totalRowsValueLabel.setText(String.valueOf(total));
        validRowsValueLabel.setText(String.valueOf(valid));
        issuesValueLabel.setText(String.valueOf(issues));
        renderPreviewRows(preview);
    }

    private void renderPreviewRows(ImportPreview preview) {
        previewRowsContainer.getChildren().clear();

        if (preview == null || preview.records().isEmpty()) {
            Label empty = new Label("Import a JSON file to preview records.");
            empty.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
            previewRowsContainer.getChildren().add(empty);
            return;
        }

        preview.records().stream().limit(6).forEach(record -> previewRowsContainer.getChildren().add(buildPreviewRow(record)));
        if (preview.records().size() > 6) {
            Label more = new Label("Showing first 6 rows.");
            more.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
            previewRowsContainer.getChildren().add(more);
        }
    }

    private HBox buildPreviewRow(PreviewRecord record) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 6 0 6 0;");

        Label number = createCell(String.valueOf(record.rowNumber()), 25);
        Label serial = createCell(nullToDash(record.normalizedRecord().serialNumber()), 90);
        Label equipment = createCell(String.valueOf(record.normalizedRecord().equipmentId()), 150);
        Label status = createCell(nullToDash(record.normalizedRecord().status()), 90);
        Label issues = createCell(record.issues().isEmpty() ? "OK" : String.join("; ", record.issues()), 240);

        if (!record.issues().isEmpty()) {
            issues.setStyle("-fx-text-fill: #b23a3a; -fx-font-size: 11px;");
        }

        row.getChildren().addAll(number, serial, equipment, status, issues);
        return row;
    }

    private Label createCell(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setWrapText(true);
        HBox.setHgrow(label, Priority.NEVER);
        return label;
    }

    private Path ensureJsonExtension(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".json")) {
            return path;
        }
        return path.resolveSibling(path.getFileName() + ".json");
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
