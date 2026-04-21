package view.util;

import dao.model.Category;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CategoryTableUtil {

    public static void setupColumns(
            TableColumn<Category, Integer> idColumn,
            TableColumn<Category, String> nameColumn) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
    }

    public static void setupContextMenu(TableView<Category> table, MenuItem... items) {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ContextMenu contextMenu = new ContextMenu(items);
        table.setRowFactory(t -> {
            TableRow<Category> row = new TableRow<>();
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