package ui.util;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
/**
 * Utility class for Application Navigation.
 * 
 * Provides centralized logic for switching scenes and managing dynamic 
 * content loading within the application's primary workspace.
 * 
 */
public class NavigationUtil {

    public static void loadIntoDashboard(ActionEvent event, String fxmlPath) {
        try {
            Parent content = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            Scene currentScene = ((Node) event.getSource()).getScene();
            Parent rootNode = currentScene.getRoot();

            if (rootNode instanceof BorderPane borderPane) {
                borderPane.setCenter(content);
                return;
            }

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(content));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load: " + fxmlPath, e);
        }
    }

    public static void loadScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load: " + fxmlPath, e);
        }
    }

    public static void loadIntoDashboardFromPane(BorderPane rootPane, String fxmlPath) {
        try {
            Parent content = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            if (rootPane != null) {
                rootPane.setCenter(content);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load: " + fxmlPath, e);
        }
    }
    
}