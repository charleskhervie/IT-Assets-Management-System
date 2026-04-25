package ui.util;

import dao.model.Equipment;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class EquipmentTableUtil {

    public static void setupColumns(
            TableView<Equipment> table,
            TableColumn<Equipment, Integer> idColumn,
            TableColumn<Equipment, String> nameColumn,
            TableColumn<Equipment, String> brandColumn,
            TableColumn<Equipment, String> modelColumn,
            TableColumn<Equipment, String> specificationsColumn,
            TableColumn<Equipment, Integer> categoryIdColumn,
            EventHandler<ActionEvent> onEdit,
            EventHandler<ActionEvent> onDelete) {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        specificationsColumn.setCellValueFactory(new PropertyValueFactory<>("specifications"));
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));

        if (onEdit != null || onDelete != null) {
        TableColumn<Equipment, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(160);
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #78A1BB; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");

                if (onEdit != null) {
                    editBtn.setOnAction(e -> {
                        table.getSelectionModel().select(getIndex());
                        onEdit.handle(e);
                    });
                }
                if (onDelete != null) {
                    deleteBtn.setOnAction(e -> {
                        table.getSelectionModel().select(getIndex());
                        onDelete.handle(e);
                    });
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().add(actionsCol);
    }

    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
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