package ui.controller;

import java.time.format.DateTimeFormatter;
import dao.model.Transaction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
/**
 * Controller for the Description Transaction screen.
 * 
 * Implements a description table displaying formatted data from selected items
 */
public class ViewTransactionModalScreen {

    @FXML private Label transactionIdLabel;
    @FXML private Label unitIdLabel;
    @FXML private Label borrowedByLabel;
    @FXML private Label processedByLabel;
    @FXML private Label borrowedDateLabel;
    @FXML private Label returnDateLabel;
    @FXML private Label statusLabel;
    @FXML private Label remarksLabel;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setTransaction(Transaction transaction) {
        transactionIdLabel.setText(String.valueOf(transaction.getTransactionId()));
        unitIdLabel.setText(String.valueOf(transaction.getUnitId()));
        borrowedByLabel.setText(transaction.getBorrowedByName() != null ? transaction.getBorrowedByName() : "-");
        processedByLabel.setText(transaction.getProcessedByName() != null ? transaction.getProcessedByName() : "-");
        borrowedDateLabel.setText(transaction.getBorrowedDate() != null
                ? transaction.getBorrowedDate().format(FORMATTER) : "-");
        returnDateLabel.setText(transaction.getReturnDate() != null
                ? transaction.getReturnDate().format(FORMATTER) : "-");
        statusLabel.setText(transaction.getStatus() != null ? transaction.getStatus() : "-");
        remarksLabel.setText(transaction.getRemarks() != null ? transaction.getRemarks() : "-");
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) transactionIdLabel.getScene().getWindow();
        stage.close();
    }
}