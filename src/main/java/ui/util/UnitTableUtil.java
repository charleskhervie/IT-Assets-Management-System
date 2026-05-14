package ui.util;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;

public class UnitTableUtil {

    public static void setupColumns(TableView<Object> table) {
        setupColumns(table, null, null);
    }

    public static void setupColumns(TableView<Object> table, EventHandler<ActionEvent> onEdit, EventHandler<ActionEvent> onDelete) {
        table.getColumns().clear();
        TableColumn<Object, Integer> idCol = new TableColumn<>("ID");
        TableColumn<Object, String> serialCol = new TableColumn<>("Serial No.");
        TableColumn<Object, String> equipmentCol = new TableColumn<>("Equipment");
        TableColumn<Object, String> categoryCol = new TableColumn<>("Category");
        TableColumn<Object, String> addedByCol = new TableColumn<>("Added By");
        TableColumn<Object, String> statusCol = new TableColumn<>("Status");
        TableColumn<Object, String> assignedToCol = new TableColumn<>("Assigned To");

        idCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        serialCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        equipmentCol.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        addedByCol.setCellValueFactory(new PropertyValueFactory<>("addedByName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        assignedToCol.setCellValueFactory(new PropertyValueFactory<>("assignedToName"));

        idCol.setPrefWidth(60); serialCol.setPrefWidth(160);
        equipmentCol.setPrefWidth(180); categoryCol.setPrefWidth(150); addedByCol.setPrefWidth(130);
        statusCol.setPrefWidth(120); assignedToCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, serialCol, equipmentCol, categoryCol, addedByCol, statusCol, assignedToCol);

        if (onEdit != null || onDelete != null) {
            TableColumn<Object, Object> actionsCol = new TableColumn<>("Actions");
            actionsCol.setMinWidth(160);
            actionsCol.setPrefWidth(160);
            actionsCol.setMaxWidth(160);
            actionsCol.setResizable(false);
            actionsCol.setStyle("-fx-alignment: CENTER;");
            actionsCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
            actionsCol.setCellFactory(col -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Delete");
                private final HBox box = new HBox(8, editBtn, deleteBtn);

                {
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    editBtn.setStyle("-fx-background-color: #78A1BB; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                    deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11px;");
                    editBtn.setPrefWidth(65);
                    deleteBtn.setPrefWidth(65);
                    editBtn.setMinWidth(65);
                    deleteBtn.setMinWidth(65);

                    if (onEdit != null) {
                        editBtn.setOnAction(e -> {
                            table.getSelectionModel().clearSelection();
                            table.getSelectionModel().select(getIndex());
                            onEdit.handle(e);
                        });
                    }
                    if (onDelete != null) {
                        deleteBtn.setOnAction(e -> {
                            table.getSelectionModel().clearSelection();
                            table.getSelectionModel().select(getIndex());
                            onDelete.handle(e);
                        });
                    }
                }

                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty || item == null ? null : box);
                }
            });
            table.getColumns().add(actionsCol);

            table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            MenuItem editItem = new MenuItem("Edit Unit");
            MenuItem deleteItem = new MenuItem("Delete Selected");
            if (onEdit != null) editItem.setOnAction(e -> onEdit.handle(e));
            if (onDelete != null) deleteItem.setOnAction(e -> onDelete.handle(e));
            ContextMenu contextMenu = new ContextMenu(editItem, deleteItem);

            table.setRowFactory(t -> {
                TableRow<Object> row = new TableRow<>();
                row.setOnContextMenuRequested(event -> {
                    if (!row.isEmpty()) {
                        if (!table.getSelectionModel().getSelectedItems().contains(row.getItem())) {
                            table.getSelectionModel().clearSelection();
                            table.getSelectionModel().select(row.getItem());
                        }
                        boolean multiSelect = table.getSelectionModel().getSelectedItems().size() > 1;
                        editItem.setDisable(multiSelect);
                        contextMenu.show(row, event.getScreenX(), event.getScreenY());
                    }
                    event.consume();
                });
                return row;
            });
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public static void setupColumnsWithActions(TableView<Object> table,
            String btn1Label, String btn1Style, EventHandler<ActionEvent> onBtn1,
            String btn2Label, String btn2Style, EventHandler<ActionEvent> onBtn2,
            EventHandler<ActionEvent> onMaintenance, EventHandler<ActionEvent> onAvailable) {

        table.getColumns().clear();

        TableColumn<Object, Integer> idCol = new TableColumn<>("ID");
        TableColumn<Object, String> serialCol = new TableColumn<>("Serial No.");
        TableColumn<Object, String> equipmentCol = new TableColumn<>("Equipment");
        TableColumn<Object, String> categoryCol = new TableColumn<>("Category");
        TableColumn<Object, String> addedByCol = new TableColumn<>("Added By");
        TableColumn<Object, String> statusCol = new TableColumn<>("Status");
        TableColumn<Object, String> assignedToCol = new TableColumn<>("Assigned To");

        idCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        serialCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        equipmentCol.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        addedByCol.setCellValueFactory(new PropertyValueFactory<>("addedByName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        assignedToCol.setCellValueFactory(new PropertyValueFactory<>("assignedToName"));

        idCol.setPrefWidth(60); serialCol.setPrefWidth(160);
        equipmentCol.setPrefWidth(180); categoryCol.setPrefWidth(150); addedByCol.setPrefWidth(130);
        statusCol.setPrefWidth(120); assignedToCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, serialCol, equipmentCol, categoryCol, addedByCol, statusCol, assignedToCol);

        if (onBtn1 != null || onBtn2 != null) {
            TableColumn<Object, Object> actionsCol = new TableColumn<>("Actions");
            actionsCol.setMinWidth(160);
            actionsCol.setPrefWidth(160);
            actionsCol.setMaxWidth(160);
            actionsCol.setResizable(false);
            actionsCol.setStyle("-fx-alignment: CENTER;");
            actionsCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));

            actionsCol.setCellFactory(col -> new TableCell<>() {
                private final Button btn1 = new Button(btn1Label);
                private final Button btn2 = new Button(btn2Label);
                private final HBox box = new HBox(8, btn1, btn2);

                {
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    btn1.setStyle(btn1Style);
                    btn2.setStyle(btn2Style);
                    btn1.setPrefWidth(65);
                    btn2.setPrefWidth(65);
                    btn1.setMinWidth(65);
                    btn2.setMinWidth(65);

                    btn1.setOnAction(e -> {
                        table.getSelectionModel().select(getIndex());
                        onBtn1.handle(e);
                    });
                    btn2.setOnAction(e -> {
                        table.getSelectionModel().select(getIndex());
                        onBtn2.handle(e);
                    });
                }

                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        btn1.setDisable(false);
                        btn2.setDisable(false);

                        if (item instanceof dao.model.Unit) {
                            dao.model.Unit unit = (dao.model.Unit) item;
                            String status = unit.getStatus().toLowerCase();

                            if (btn1.getText().equalsIgnoreCase("Check Out")) {
                                btn1.setDisable(status.equals("checked-out") || status.equals("maintenance"));
                            }
                            if (btn2.getText().equalsIgnoreCase("Check In")) {
                                btn2.setDisable(status.equals("available"));
                            }
                        }
                        setGraphic(box);
                    }
                }
            });
            table.getColumns().add(actionsCol);

            table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            MenuItem editItem = new MenuItem("Edit Unit");
            MenuItem deleteItem = new MenuItem("Delete Selected");
            MenuItem maintenanceItem = new MenuItem("Set Status: Maintenance");
            MenuItem availableItem = new MenuItem("Set Status: Available");

            

            editItem.setOnAction(e -> {
                if (table.getSelectionModel().getSelectedItems().size() > 1) return;
                onBtn1.handle(e);
            });
            deleteItem.setOnAction(e -> onBtn2.handle(e));
            maintenanceItem.setOnAction(e -> onMaintenance.handle(e));
            availableItem.setOnAction(e -> onAvailable.handle(e));

            ContextMenu contextMenu = new ContextMenu(editItem, deleteItem, maintenanceItem, availableItem);

            table.setRowFactory(t -> {
                TableRow<Object> row = new TableRow<>();
                row.setOnContextMenuRequested(event -> {
                    if (!row.isEmpty()) {
                        if (!table.getSelectionModel().getSelectedItems().contains(row.getItem())) {
                            table.getSelectionModel().clearSelection();
                            table.getSelectionModel().select(row.getItem());
                        }
                        boolean multiSelect = table.getSelectionModel().getSelectedItems().size() > 1;
                        editItem.setDisable(multiSelect);
                        contextMenu.show(row, event.getScreenX(), event.getScreenY());
                    }
                    event.consume();
                });
                return row;
            });
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
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