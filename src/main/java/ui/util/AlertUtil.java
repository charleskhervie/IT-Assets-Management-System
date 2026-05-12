package ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
/**
 * Utility class for UI Dialog Management.
 * 
 * Provides a centralized interface for triggering standardized JavaFX 
 * dialogs to facilitate user interaction and feedback.
 * 
 * - Simplifies the creation of confirmation dialogs, returning a boolean 
 *   result based on {@link ButtonType#OK} selection.
 * - Standardizes the display of error alerts to inform users of validation 
 *   failures or system exceptions.
 * - Facilitates information signaling to provide non-critical updates or 
 *   operation success confirmations.
 */
public class AlertUtil {

    private AlertUtil() {}

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}