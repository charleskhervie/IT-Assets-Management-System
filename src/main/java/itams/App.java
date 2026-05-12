package itams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import ui.controller.DatabaseSetupDialogController;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!AppConfig.isConfigured()) {
            showDatabaseSetupDialog(primaryStage);
        } else {
            loadMainApplication(primaryStage);
        }
    }
    
    private void showDatabaseSetupDialog(Stage primaryStage) throws Exception {
        Stage setupStage = new Stage();
        setupStage.initModality(Modality.APPLICATION_MODAL);
        setupStage.setTitle("Database Configuration");
        setupStage.setResizable(false);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DatabaseSetupDialog.fxml"));
        Parent root = loader.load();
        
        DatabaseSetupDialogController controller = loader.getController();
        controller.setOnSuccess(() -> {
            try {
                setupStage.close();
                loadMainApplication(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        setupStage.setScene(new Scene(root));
        setupStage.showAndWait();
    }
    
    private void loadMainApplication(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        
        primaryStage.setTitle("IT Assets Management System");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}