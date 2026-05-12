package ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
/**
 * Controller for the Add Remarks Modal.
 * 
 * Provides a streamlined interface for capturing supplemental text annotations 
 * during inventory or transaction processes. This controller acts as a 
 * data-entry buffer, allowing users to append qualitative notes to records 
 * before final commitment.
 * 
 */
public class AddRemarksModal {

    @FXML private TextArea remarksArea;

    private String remarks;
    private boolean saved = false;

    public String getRemarks() {
        return remarks;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        remarks = remarksArea.getText().trim();
        saved = true;
        close(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        saved = false;
        close(event);
    }

    private void close(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}