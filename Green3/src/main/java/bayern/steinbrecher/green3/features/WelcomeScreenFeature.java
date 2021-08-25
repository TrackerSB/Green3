package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.about.AboutScreen;
import bayern.steinbrecher.green3.screens.settings.SettingsScreen;
import bayern.steinbrecher.green3.screens.welcome.WelcomeScreen;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import javafx.application.Platform;
import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class WelcomeScreenFeature extends TileScreenFeature {
    private static final Logger LOGGER = Logger.getLogger(WelcomeScreenFeature.class.getName());
    static Iterable<WelcomeScreenFeature> FEATURES = List.of(
            new WelcomeScreenFeature("WelcomeSettings",
                    new FeatureDescription(
                            resources.getString("settings"),
                            WelcomeScreen.class.getResource("settings.png"), true),
                    true, sm -> {
                try {
                    sm.switchTo(new SettingsScreen());
                } catch (ScreenSwitchFailedException ex) {
                    LOGGER.log(Level.SEVERE, "Could not open settings screen", ex);
                }
            }),
            new WelcomeScreenFeature("WelcomeCredits",
                    new FeatureDescription(
                            resources.getString("about"),
                            WelcomeScreen.class.getResource("teamwork.png"), true),
                    true, sm -> {
                try {
                    sm.switchTo(new AboutScreen());
                } catch (ScreenSwitchFailedException ex) {
                    LOGGER.log(Level.SEVERE, "Could not open about screen", ex);
                }
            }),
            new WelcomeScreenFeature("WelcomeExit",
                    new FeatureDescription(
                            resources.getString("exit"),
                            WelcomeScreen.class.getResource("power.png"), true),
                    true, sm -> Platform.exit())
    );

    private WelcomeScreenFeature(@NonNull String id, @NonNull FeatureDescription description,
                                 boolean enabled, @NonNull Consumer<ScreenManager> action) {
        super(id, description, enabled, action);
    }
}
