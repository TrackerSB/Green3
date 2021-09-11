package bayern.steinbrecher.green3.screens.profileSettings;

import bayern.steinbrecher.screenswitcher.Screen;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class ProfileSettingsScreen extends Screen<ProfileSettingsScreenController> {
    public ProfileSettingsScreen(){
        super(
                ProfileSettingsScreen.class.getResource("ProfileSettingsScreen.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.green3.screens.profileSettings.ProfileSettings")
        );
    }
}
