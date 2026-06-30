package utils;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class WindowUtils {

    public static void setWindowPercentage(Stage stage, double widthPercentage, double heightPercentage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth() * widthPercentage;
        double height = screenBounds.getHeight() * heightPercentage;
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
    }

    public static void setFullScreenWithPadding(Stage stage, double padding) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX() + padding);
        stage.setY(screenBounds.getMinY() + padding);
        stage.setWidth(screenBounds.getWidth() - (2 * padding));
        stage.setHeight(screenBounds.getHeight() - (2 * padding));
    }

    public static void centerOnScreen(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - stage.getWidth()) / 2;
        double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - stage.getHeight()) / 2;
        stage.setX(centerX);
        stage.setY(centerY);
    }

    public static double[] getModalSize(double percentage, Stage parentStage) {
        double parentWidth = parentStage.getWidth();
        double parentHeight = parentStage.getHeight();
        double width = parentWidth * percentage;
        double height = parentHeight * percentage;
        width = Math.max(width, 300);
        height = Math.max(height, 200);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        width = Math.min(width, screenBounds.getWidth() * 0.8);
        height = Math.min(height, screenBounds.getHeight() * 0.8);
        return new double[]{width, height};
    }
}