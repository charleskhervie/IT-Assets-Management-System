package view.util;

import dao.model.Unit;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UnitTableUtil {


    public static void setupColumns(
            TableColumn<Unit, Integer> idColumn,
            TableColumn<Unit, String> serialColumn,
            TableColumn<Unit, Integer> equipmentColumn,
            TableColumn<Unit, Integer> addedByColumn,
            TableColumn<Unit, String> statusColumn,
            TableColumn<Unit, Integer> assignedToColumn) {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        equipmentColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        addedByColumn.setCellValueFactory(new PropertyValueFactory<>("addedBy"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        assignedToColumn.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
    }

   
    public static void setupContextMenu(TableView<Unit> table, MenuItem... items) {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ContextMenu contextMenu = new ContextMenu(items);

        table.setRowFactory(t -> {
            TableRow<Unit> row = new TableRow<>();
            row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );
            return row;
        });
    }

    
    public static TableRow<Unit> buildRowWithContextMenu(ContextMenu contextMenu) {
        TableRow<Unit> row = new TableRow<>();
        row.contextMenuProperty().bind(
            Bindings.when(row.emptyProperty())
                .then((ContextMenu) null)
                .otherwise(contextMenu)
        );
        return row;
    }
}