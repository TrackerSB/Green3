package bayern.steinbrecher.green3.screens.settings;

import bayern.steinbrecher.green3.features.SettingsScreenFeature;
import bayern.steinbrecher.green3.screens.TileScreen;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class SettingsScreen extends TileScreen {
    public SettingsScreen() {
        super(ResourceBundle.getBundle("bayern.steinbrecher.green3.screens.settings.SettingsScreen"),
                SettingsScreenFeature.class);
    }
}
