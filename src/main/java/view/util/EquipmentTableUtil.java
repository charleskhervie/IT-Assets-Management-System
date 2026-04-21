package view.util;

import dao.model.Equipment;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class EquipmentTableUtil {

    public static void setupColumns(
            TableColumn<Equipment, Integer> idColumn,
            TableColumn<Equipment, String> nameColumn,
            TableColumn<Equipment, String> brandColumn,
            TableColumn<Equipment, String> modelColumn,
            TableColumn<Equipment, String> specificationsColumn,
            TableColumn<Equipment, Integer> categoryIdColumn) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        specificationsColumn.setCellValueFactory(new PropertyValueFactory<>("specifications"));
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
    }

    public static void setupContextMenu(TableView<Equipment> table, MenuItem... items) {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ContextMenu contextMenu = new ContextMenu(items);
        table.setRowFactory(t -> {
            TableRow<Equipment> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    table.getSelectionModel().select(row.getItem());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
                event.consume();
            });
            return row;
        });
    }
}