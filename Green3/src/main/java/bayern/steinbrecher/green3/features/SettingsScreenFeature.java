package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.featureSelection.FeatureSelectionScreen;
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
    static Iterable<SettingsScreenFeature> FEATURES = List.of(
            new SettingsScreenFeature("SettingsBack",
                    new FeatureDescription(
                            resources.getString("back"),
                            SettingsScreen.class.getResource("back.png"), true),
                    true, ScreenManager::switchBack),
            new SettingsScreenFeature("SettingsFeatures",
                    new FeatureDescription(
                            resources.getString("features"),
                            SettingsScreen.class.getResource("itemsClipboard.png"), true),
                    true, sm -> {
                try {
                    sm.switchTo(new FeatureSelectionScreen());
                } catch (ScreenSwitchFailedException ex) {
                    LOGGER.log(Level.SEVERE, "Could not open feature settings", ex);
                }
            })
    );

    private SettingsScreenFeature(@NonNull String id, @NonNull FeatureDescription description,
                                  boolean enabled, @NonNull Consumer<ScreenManager> action) {
        super(id, description, enabled, action);
    }
}
