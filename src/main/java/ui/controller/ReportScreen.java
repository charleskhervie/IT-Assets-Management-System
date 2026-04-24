package ui.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import ui.service.ExportSummary;
import ui.service.ReportService;
import ui.service.ReportService.CategorySummaryRow;
import ui.service.ReportService.EquipmentSummaryRow;
import ui.service.ReportService.ReportSnapshot;
import ui.service.ReportService.TransactionSummaryRow;
import ui.util.NavigationUtil;

public class ReportScreen implements Initializable {

    @FXML private Label totalUnitsLabel;
    @FXML private Label availableUnitsLabel;
    @FXML private Label checkedOutUnitsLabel;
    @FXML private Label maintenanceUnitsLabel;
    @FXML private Label transactionsLabel;

    @FXML private TableView<CategorySummaryRow> categorySummaryTable;
    @FXML private TableColumn<CategorySummaryRow, String> categoryNameColumn;
    @FXML private TableColumn<CategorySummaryRow, Integer> categoryTotalColumn;
    @FXML private TableColumn<CategorySummaryRow, Integer> categoryAvailableColumn;
    @FXML private TableColumn<CategorySummaryRow, Integer> categoryCheckedOutColumn;
    @FXML private TableColumn<CategorySummaryRow, Integer> categoryMaintenanceColumn;

    @FXML private TableView<EquipmentSummaryRow> equipmentSummaryTable;
    @FXML private TableColumn<EquipmentSummaryRow, String> equipmentNameColumn;
    @FXML private TableColumn<EquipmentSummaryRow, Integer> equipmentTotalColumn;
    @FXML private TableColumn<EquipmentSummaryRow, Integer> equipmentAvailableColumn;
    @FXML private TableColumn<EquipmentSummaryRow, Integer> equipmentCheckedOutColumn;
    @FXML private TableColumn<EquipmentSummaryRow, Integer> equipmentMaintenanceColumn;

    @FXML private TableView<TransactionSummaryRow> transactionSummaryTable;
    @FXML private TableColumn<TransactionSummaryRow, Integer> transactionIdColumn;
    @FXML private TableColumn<TransactionSummaryRow, String> transactionUnitColumn;
    @FXML private TableColumn<TransactionSummaryRow, String> transactionBorrowedByColumn;
    @FXML private TableColumn<TransactionSummaryRow, String> transactionProcessedByColumn;
    @FXML private TableColumn<TransactionSummaryRow, String> transactionBorrowDateColumn;
    @FXML private TableColumn<TransactionSummaryRow, String> transactionReturnDateColumn;
    @FXML private TableColumn<TransactionSummaryRow, String> transactionStatusColumn;
    @FXML private TableColumn<TransactionSummaryRow, String> transactionRemarksColumn;

    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> equipmentBarChart;

    @FXML private Button exportCsvButton;
    @FXML private Button exportJsonButton;
    @FXML private Button exportPdfButton;

    private final ReportService reportService = new ReportService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTables();
        loadData();
    }

    private void configureTables() {
        categoryNameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().categoryName()));
        categoryTotalColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().totalUnits()).asObject());
        categoryAvailableColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().available()).asObject());
        categoryCheckedOutColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().checkedOut()).asObject());
        categoryMaintenanceColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().maintenance()).asObject());

        equipmentNameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().equipmentName()));
        equipmentTotalColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().totalUnits()).asObject());
        equipmentAvailableColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().available()).asObject());
        equipmentCheckedOutColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().checkedOut()).asObject());
        equipmentMaintenanceColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().maintenance()).asObject());

        transactionIdColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().transactionId()).asObject());
        transactionUnitColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().unitLabel()));
        transactionBorrowedByColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().borrowedByName()));
        transactionProcessedByColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().processedByName()));
        transactionBorrowDateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().borrowedDate()));
        transactionReturnDateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().returnDate()));
        transactionStatusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status()));
        transactionRemarksColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().remarks()));
    }

    private void loadData() {
        try {
            ReportSnapshot snapshot = reportService.loadReportData();

            totalUnitsLabel.setText(String.valueOf(snapshot.totalUnits()));
            availableUnitsLabel.setText(String.valueOf(snapshot.availableUnits()));
            checkedOutUnitsLabel.setText(String.valueOf(snapshot.checkedOutUnits()));
            maintenanceUnitsLabel.setText(String.valueOf(snapshot.maintenanceUnits()));
            transactionsLabel.setText(String.valueOf(snapshot.totalTransactions()));

            categorySummaryTable.setItems(FXCollections.observableArrayList(snapshot.categoryRows()));
            equipmentSummaryTable.setItems(FXCollections.observableArrayList(snapshot.equipmentRows()));
            transactionSummaryTable.setItems(FXCollections.observableArrayList(snapshot.transactionRows()));

            statusPieChart.setData(FXCollections.observableArrayList(
                snapshot.statusRows().stream()
                    .map(row -> new PieChart.Data(row.status(), row.count()))
                    .toList()
            ));

            equipmentBarChart.getData().clear();
            var series = new javafx.scene.chart.XYChart.Series<String, Number>();
            series.setName("Units per Equipment");
            snapshot.equipmentRows().stream()
                .sorted((left, right) -> Integer.compare(right.totalUnits(), left.totalUnits()))
                .limit(10)
                .forEach(row -> series.getData().add(new javafx.scene.chart.XYChart.Data<>(row.equipmentName(), row.totalUnits())));
            equipmentBarChart.getData().add(series);
        } catch (Exception e) {
            showError("Report Load Failed", e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv(ActionEvent event) {
        exportReport("CSV", "report-summary.csv", "*.csv", path -> reportService.exportToCsv(path));
    }

    @FXML
    private void handleExportJson(ActionEvent event) {
        exportReport("JSON", "report-summary.json", "*.json", path -> reportService.exportToJson(path));
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        exportReport("PDF", "report-summary.pdf", "*.pdf", path -> reportService.exportToPdf(path));
    }

    @FunctionalInterface
    private interface ExportAction {
        ExportSummary execute(Path path) throws Exception;
    }

    private void exportReport(String format, String defaultName, String pattern, ExportAction action) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export Report as " + format);
            chooser.setInitialFileName(defaultName);
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(format + " Files", pattern));

            File file = chooser.showSaveDialog(exportCsvButton.getScene().getWindow());
            if (file == null) {
                return;
            }

            ExportSummary result = action.execute(file.toPath());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(format + " Export Complete");
            alert.setHeaderText(null);
            alert.setContentText("Exported report to:\n" + result.targetFile());
            alert.showAndWait();
        } catch (Exception e) {
            showError(format + " Export Failed", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
