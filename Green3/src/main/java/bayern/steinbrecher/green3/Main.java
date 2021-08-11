package bayern.steinbrecher.green3;

import bayern.steinbrecher.green3.screens.WelcomeScreen;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.Objects;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public final class Main extends Application {

    private static final String DEFAULT_STYLESHEET_PATH
            = Objects.requireNonNull(Main.class.getResource("main.css"))
            .toExternalForm();
    private static final Image APPLICATION_ICON
            = new Image(Objects.requireNonNull(Main.class.getResource("logo.png")).toExternalForm());

    @Override
    public void start(@NonNull Stage primaryStage) throws ScreenSwitchFailedException {
        ScreenManager screenManager = new ScreenManager(primaryStage);
        screenManager.switchTo(new WelcomeScreen());

        primaryStage.getScene()
                .getStylesheets()
                .add(DEFAULT_STYLESHEET_PATH);
        primaryStage.getIcons()
                .add(APPLICATION_ICON);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
