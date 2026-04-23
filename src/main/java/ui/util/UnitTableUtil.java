package ui.util;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UnitTableUtil {

    public static void setupColumns(TableView<Object> table) {
        table.getColumns().clear();
        TableColumn<Object, Integer> idCol = new TableColumn<>("ID");
        TableColumn<Object, String> serialCol = new TableColumn<>("Serial No.");
        TableColumn<Object, String> equipmentCol = new TableColumn<>("Equipment");
        TableColumn<Object, String> addedByCol = new TableColumn<>("Added By");
        TableColumn<Object, String> statusCol = new TableColumn<>("Status");
        TableColumn<Object, String> assignedToCol = new TableColumn<>("Assigned To");

        idCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        serialCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        equipmentCol.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        addedByCol.setCellValueFactory(new PropertyValueFactory<>("addedByName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        assignedToCol.setCellValueFactory(new PropertyValueFactory<>("assignedToName"));

        idCol.setPrefWidth(60); serialCol.setPrefWidth(160);
        equipmentCol.setPrefWidth(180); addedByCol.setPrefWidth(130);
        statusCol.setPrefWidth(120); assignedToCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, serialCol, equipmentCol, addedByCol, statusCol, assignedToCol);
    }

    public static void setupContextMenu(TableView<Object> table, MenuItem... items) {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ContextMenu contextMenu = new ContextMenu(items);
        table.setRowFactory(t -> {
            TableRow<Object> row = new TableRow<>();
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