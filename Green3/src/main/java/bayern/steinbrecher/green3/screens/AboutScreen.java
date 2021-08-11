package bayern.steinbrecher.green3.screens;

import bayern.steinbrecher.screenswitcher.Screen;

import java.util.ResourceBundle;

public class AboutScreen extends Screen<AboutScreenController> {
    public AboutScreen() {
        super(
                AboutScreen.class.getResource("AboutScreen.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.green3.screens.AboutScreen"));
    }
}
