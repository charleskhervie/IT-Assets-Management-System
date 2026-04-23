package ui.util;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import dao.model.Transaction;

public class TransactionTableUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void setupColumns(TableView<Object> table) {
        table.getColumns().clear();
        TableColumn<Object, Integer> idCol = new TableColumn<>("Transaction ID");
        TableColumn<Object, Integer> unitIdCol = new TableColumn<>("Unit ID");
        TableColumn<Object, String> equipmentCol = new TableColumn<>("Equipment");
        TableColumn<Object, String> borrowedByCol = new TableColumn<>("Borrowed By");
        TableColumn<Object, String> processedByCol = new TableColumn<>("Processed By");
        TableColumn<Object, LocalDateTime> borrowDateCol = new TableColumn<>("Borrow Date");
        TableColumn<Object, LocalDateTime> returnDateCol = new TableColumn<>("Return Date");
        TableColumn<Object, String> statusCol = new TableColumn<>("Status");
        TableColumn<Object, String> remarksCol = new TableColumn<>("Remarks");

        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        unitIdCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        equipmentCol.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        borrowedByCol.setCellValueFactory(new PropertyValueFactory<>("borrowedByName"));
        processedByCol.setCellValueFactory(new PropertyValueFactory<>("processedByName"));
        borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowedDate"));
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        applyDateCellFactory(borrowDateCol);
        applyDateCellFactory(returnDateCol);

        idCol.setPrefWidth(110); unitIdCol.setPrefWidth(90);
        equipmentCol.setPrefWidth(160); borrowedByCol.setPrefWidth(150);
        processedByCol.setPrefWidth(150); borrowDateCol.setPrefWidth(120);
        returnDateCol.setPrefWidth(120); statusCol.setPrefWidth(110);
        remarksCol.setPrefWidth(130);

        table.getColumns().addAll(idCol, unitIdCol, equipmentCol, borrowedByCol,
                processedByCol, borrowDateCol, returnDateCol, statusCol, remarksCol);
    }

    public static void setupContextMenu(TableView<Object> table, Runnable onDoubleClick, MenuItem... items) {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ContextMenu contextMenu = new ContextMenu(items);
        table.setRowFactory(t -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    onDoubleClick.run();
                }
            });
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

    public static void setupActionsColumn(
            TableColumn<Object, Void> actionsColumn,
            java.util.function.Consumer<Object> onApprove,
            java.util.function.Consumer<Object> onDecline,
            boolean isAdmin) {

        if (!isAdmin) {
            actionsColumn.setVisible(false);
            return;
        }

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button declineBtn = new Button("Decline");
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(6, approveBtn, declineBtn);

            {
                approveBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 11px;");
                declineBtn.setStyle("-fx-background-color: #e05555; -fx-text-fill: white; -fx-font-size: 11px;");
                approveBtn.setOnAction(e -> onApprove.accept(getTableView().getItems().get(getIndex())));
                declineBtn.setOnAction(e -> onDecline.accept(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Transaction t = (Transaction) getTableView().getItems().get(getIndex());
                setGraphic("pending".equalsIgnoreCase(t.getStatus()) ? box : null);
            }
        });
    }

    private static void applyDateCellFactory(TableColumn<Object, LocalDateTime> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(FORMATTER));
            }
        });
    }
}