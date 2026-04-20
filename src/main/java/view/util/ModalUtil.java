package view.util;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ModalUtil {

    private ModalUtil() {}

    public static void openModal(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root  = FXMLLoader.load(ModalUtil.class.getResource(fxmlPath));
            Stage  owner = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage  modal = new Stage();
            modal.initOwner(owner);
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setResizable(false);
            modal.setTitle(title);
            modal.setScene(new Scene(root));
            modal.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load modal: " + fxmlPath, e);
        }
    }
}