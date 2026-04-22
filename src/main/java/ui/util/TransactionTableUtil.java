package ui.util;

import dao.model.Transaction;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionTableUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void setupColumns(
            TableColumn<Transaction, Integer> idColumn,
            TableColumn<Transaction, Integer> unitIdColumn,
            TableColumn<Transaction, Integer> borrowedByColumn,
            TableColumn<Transaction, Integer> processedByColumn,
            TableColumn<Transaction, LocalDateTime> borrowDateColumn,
            TableColumn<Transaction, LocalDateTime> returnDateColumn,
            TableColumn<Transaction, String> statusColumn,
            TableColumn<Transaction, String> remarksColumn) {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        unitIdColumn.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        borrowedByColumn.setCellValueFactory(new PropertyValueFactory<>("borrower"));
        processedByColumn.setCellValueFactory(new PropertyValueFactory<>("processedBy"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        borrowDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(FORMATTER));
            }
        });

        returnDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(FORMATTER));
            }
        });
    }

    public static void setupContextMenu(TableView<Transaction> table, Runnable onDoubleClick, MenuItem... items) {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ContextMenu contextMenu = new ContextMenu(items);
        table.setRowFactory(t -> {
            TableRow<Transaction> row = new TableRow<>();
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
}