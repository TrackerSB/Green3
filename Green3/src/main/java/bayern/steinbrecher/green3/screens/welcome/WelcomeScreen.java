package bayern.steinbrecher.green3.screens.welcome;

import bayern.steinbrecher.green3.features.WelcomeScreenFeature;
import bayern.steinbrecher.green3.screens.TileScreen;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class WelcomeScreen extends TileScreen {
    public WelcomeScreen() {
        super(ResourceBundle.getBundle("bayern.steinbrecher.green3.screens.welcome.WelcomeScreen"),
                WelcomeScreenFeature.class);
    }
}
