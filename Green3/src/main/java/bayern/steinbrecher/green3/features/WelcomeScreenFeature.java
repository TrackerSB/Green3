package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.about.AboutScreen;
import bayern.steinbrecher.green3.screens.memberManagement.MemberManagementScreen;
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
    public static final WelcomeScreenFeature MEMBER_MANAGEMENT = new WelcomeScreenFeature(
            new FeatureDescription(
                    resources.getString("memberManagement"),
                    WelcomeScreen.class.getResource("content-management.png"), true),
            true, sm -> {
        try {
            sm.switchTo(new MemberManagementScreen());
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not switch to member management screen", ex);
        }
    });
    public static final WelcomeScreenFeature SETTINGS = new WelcomeScreenFeature(
            new FeatureDescription(
                    resources.getString("settings"),
                    WelcomeScreen.class.getResource("settings.png"), true),
            true, sm -> {
        try {
            sm.switchTo(new SettingsScreen());
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not open settings screen", ex);
        }
    });
    public static final WelcomeScreenFeature CREDITS = new WelcomeScreenFeature(
            new FeatureDescription(
                    resources.getString("about"),
                    WelcomeScreen.class.getResource("teamwork.png"), true),
            true, sm -> {
        try {
            sm.switchTo(new AboutScreen());
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not open about screen", ex);
        }
    });
    public static final WelcomeScreenFeature EXIT = new WelcomeScreenFeature(
            new FeatureDescription(
                    resources.getString("exit"),
                    WelcomeScreen.class.getResource("power.png"), true),
            true, sm -> Platform.exit());
    static Iterable<WelcomeScreenFeature> FEATURES = List.of(
            MEMBER_MANAGEMENT,
            SETTINGS,
            CREDITS,
            EXIT
    );

    private WelcomeScreenFeature(@NonNull FeatureDescription description, boolean enabled,
                                 @NonNull Consumer<ScreenManager> action) {
        super(description, enabled, action);
    }
}
