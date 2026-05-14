package itams;

import dao.dao_util.CredentialManager;
import dao.dao_util.DBUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void init() throws Exception {
        DBUtil.initializeDatabase(); 
    }
   @Override
    public void start(Stage stage) throws Exception {
        CredentialManager cm = new CredentialManager();
        String fxml = cm.exists() ? "/fxml/login.fxml" : "/fxml/Setup.fxml";
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        stage.setScene(new Scene(root));
        stage.setTitle("IT Assets Manager");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}