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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import ui.service.ExportSummary;
import ui.service.InventoryCsvService;
import ui.service.InventoryJsonService;
import ui.service.InventorySqlService;
import ui.util.NavigationUtil;

public class ImportExportScreen implements Initializable {

    @FXML private CheckBox exportJson;
    @FXML private CheckBox exportCsv;
    @FXML private CheckBox exportSql;

    @FXML private Button confirmExportButton;

    private final InventoryJsonService jsonService = new InventoryJsonService();
    private final InventoryCsvService csvService = new InventoryCsvService();
    private final InventorySqlService sqlService = new InventorySqlService();

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
        // placeholder (import removed)
    }

    @FXML
    private void handleConfirmImport(ActionEvent event) {
        // placeholder (import removed)
    }
}