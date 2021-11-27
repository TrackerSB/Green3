package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.profileSettings.ProfileSettingsScreen;
import bayern.steinbrecher.green3.screens.settings.SettingsScreen;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class SettingsScreenFeature extends TileScreenFeature {
    private static final Logger LOGGER = Logger.getLogger(WelcomeScreenFeature.class.getName());
    public static final SettingsScreenFeature BACK = new SettingsScreenFeature(
            new FeatureDescription(
                    resources.getString("back"),
                    SettingsScreen.class.getResource("back.png"), true),
            true, ScreenManager::switchBack);
    public static final SettingsScreenFeature PROFILES = new SettingsScreenFeature(
            new FeatureDescription(
                    resources.getString("profiles"),
                    SettingsScreen.class.getResource("gear.png"), true),
            true, sm -> {
        try {
            sm.switchTo(new ProfileSettingsScreen());
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not show screen of profile settings", ex);
        }
    });
    static Iterable<SettingsScreenFeature> FEATURES = List.of(
            BACK,
            PROFILES
    );

    private SettingsScreenFeature(@NonNull FeatureDescription description, boolean enabled,
                                  @NonNull Consumer<ScreenManager> action) {
        super(description, enabled, action);
    }
}
