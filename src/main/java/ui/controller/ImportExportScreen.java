package ui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import ui.service.ExportSummary;
import ui.service.InventoryCsvService;
import ui.service.InventoryJsonService;
import ui.service.InventorySqlService;
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

    private final InventoryJsonService jsonService = new InventoryJsonService();
    private final InventoryCsvService csvService = new InventoryCsvService();
    private final InventorySqlService sqlService = new InventorySqlService();

    private File selectedImportFile;
    private ImportPreview currentPreview;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updatePreviewState(null, null);
        initExportCheckboxes();
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
    private void handleChooseImportFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Import Inventory JSON");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fc.showOpenDialog(confirmImportButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            selectedImportFile = file;
            currentPreview = jsonService.loadPreview(file.toPath());
            updatePreviewState(file, currentPreview);
        } catch (Exception e) {
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
            showError("Validation Failed", "Fix issues or disable validation.");
            return;
        }

        try {
            ImportExecution result = jsonService.importRecords(
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
                message.append("\n\nIssues:\n");
                message.append(String.join("\n", result.issues().stream().limit(5).toList()));
                if (result.issues().size() > 5) {
                    message.append("\n...");
                }
            }

            alert.setContentText(message.toString());
            alert.showAndWait();

            currentPreview = jsonService.loadPreview(selectedImportFile.toPath());
            updatePreviewState(selectedImportFile, currentPreview);
        } catch (Exception e) {
            showError("Import Failed", e.getMessage());
        }
    }

    @FXML
    private void handleConfirmExport(ActionEvent event) {
        if (!exportJson.isSelected() && !exportCsv.isSelected() && !exportSql.isSelected()) {
            showError("No Format Selected", "Select at least one format.");
            return;
        }

        if (exportJson.isSelected()) {
            exportFormat("JSON", "inventory-export.json", "*.json",
                path -> jsonService.exportUnits(path));
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
        ExportSummary execute(Path path) throws IOException, SQLException;
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
        } catch (Exception e) {
            showError(format + " Export Failed", e.getMessage());
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
            Label empty = new Label("Import a JSON file to preview.");
            empty.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
            previewRowsContainer.getChildren().add(empty);
            return;
        }

        preview.records().stream()
            .limit(6)
            .forEach(r -> previewRowsContainer.getChildren().add(buildPreviewRow(r)));

        if (preview.records().size() > 6) {
            Label more = new Label("Showing first 6 rows.");
            more.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
            previewRowsContainer.getChildren().add(more);
        }
    }

    private HBox buildPreviewRow(PreviewRecord r) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 6 0 6 0;");

        Label number = createCell(String.valueOf(r.rowNumber()), 30);
        Label serial = createCell(nullToDash(r.normalizedRecord().serialNumber()), 100);
        Label equipment = createCell(String.valueOf(r.normalizedRecord().equipmentId()), 120);
        Label status = createCell(nullToDash(r.normalizedRecord().status()), 80);
        Label issues = createCell(
            r.issues().isEmpty() ? "OK" : String.join("; ", r.issues()),
            250
        );

        if (!r.issues().isEmpty()) {
            issues.setStyle("-fx-text-fill: #b23a3a; -fx-font-size: 11px;");
        }

        row.getChildren().addAll(number, serial, equipment, status, issues);
        return row;
    }

    private Label createCell(String text, double width) {
        Label l = new Label(text);
        l.setMinWidth(width);
        l.setPrefWidth(width);
        l.setWrapText(true);
        HBox.setHgrow(l, Priority.NEVER);
        return l;
    }

    private String nullToDash(String v) {
        return (v == null || v.isBlank()) ? "-" : v;
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}