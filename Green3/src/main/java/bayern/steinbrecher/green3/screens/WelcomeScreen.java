package bayern.steinbrecher.green3.screens;

import bayern.steinbrecher.screenswitcher.Screen;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class WelcomeScreen extends Screen<WelcomeScreenController> {
    public WelcomeScreen() {
        super(
                WelcomeScreen.class.getResource("WelcomeScreen.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.green3.screens.WelcomeScreen"));
    }
}
