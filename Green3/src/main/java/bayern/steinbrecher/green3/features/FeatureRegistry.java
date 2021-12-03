package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.about.AboutScreen;
import bayern.steinbrecher.green3.screens.memberManagement.MemberManagementScreen;
import bayern.steinbrecher.green3.screens.profileSettings.ProfileSettingsScreen;
import bayern.steinbrecher.green3.screens.settings.SettingsScreen;
import bayern.steinbrecher.green3.screens.welcome.WelcomeScreen;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import javafx.application.Platform;
import lombok.NonNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public final class FeatureRegistry {
    private static final Logger LOGGER = Logger.getLogger(FeatureRegistry.class.getName());

    // Member management screen
    public static final MemberManagementScreenFeature MEMBER_MANAGEMENT_TABLE_FILTERS
            = new MemberManagementScreenFeature(
            new FeatureDescription("tableFilter", "add.png", false, List.of()),
            true
    );
    public static final Feature MEMBER_MANAGEMENT_SCREEN = new Feature(
            new FeatureDescription("memberManagementScreen", null, true, List.of(
                    MEMBER_MANAGEMENT_TABLE_FILTERS
            )), true) {
        @Override
        protected @NonNull Class<?> getAssociatedClass() {
            return MemberManagementScreen.class;
        }
    };

    // Settings screen
    public static final SettingsScreenFeature SETTINGS_BACK = new SettingsScreenFeature(
            new FeatureDescription("back", "back.png", true, List.of()),
            true, ScreenManager::switchBack);
    public static final SettingsScreenFeature SETTINGS_PROFILES = new SettingsScreenFeature(
            new FeatureDescription("profiles", "gear.png", true, List.of()),
            true, sm -> {
        try {
            sm.switchTo(new ProfileSettingsScreen());
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not show screen of profile settings", ex);
        }
    });
    public static final Feature SETTINGS_SCREEN = new Feature(
            new FeatureDescription("settingsScreen", null, true, List.of(
                    SETTINGS_BACK,
                    SETTINGS_PROFILES
            )), true) {
        @Override
        @NonNull
        protected Class<?> getAssociatedClass() {
            return SettingsScreen.class;
        }
    };

    // Welcome screen
    public static final WelcomeScreenFeature WELCOME_MEMBER_MANAGEMENT = new WelcomeScreenFeature(
            new FeatureDescription("memberManagement", "content-management.png", true, List.of()),
            true, sm -> {
        try {
            sm.switchTo(new MemberManagementScreen());
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not switch to member management screen", ex);
        }
    });
    public static final WelcomeScreenFeature WELCOME_SETTINGS = new WelcomeScreenFeature(
            new FeatureDescription("settings", "settings.png", true, List.of()),
            true, sm -> {
        try {
            sm.switchTo(new SettingsScreen());
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not open settings screen", ex);
        }
    });
    public static final WelcomeScreenFeature WELCOME_CREDITS = new WelcomeScreenFeature(
            new FeatureDescription("about", "teamwork.png", true, List.of()),
            true, sm -> {
        try {
            sm.switchTo(new AboutScreen());
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not open about screen", ex);
        }
    });
    public static final WelcomeScreenFeature WELCOME_EXIT = new WelcomeScreenFeature(
            new FeatureDescription("exit", "power.png", true, List.of()),
            true, sm -> Platform.exit());
    public static final Feature WELCOME_SCREEN = new Feature(
            new FeatureDescription("welcomeScreen", null, true, List.of(
                    WELCOME_MEMBER_MANAGEMENT,
                    WELCOME_SETTINGS,
                    WELCOME_CREDITS,
                    WELCOME_EXIT
            )), true) {
        @Override
        protected @NonNull Class<?> getAssociatedClass() {
            return WelcomeScreen.class;
        }
    };

    // Root feature
    public static final Feature ROOT = new Feature(
            new FeatureDescription("features", null, true, List.of(
                    MEMBER_MANAGEMENT_SCREEN,
                    SETTINGS_SCREEN
            )), true) {
        @Override
        protected @NonNull Class<?> getAssociatedClass() {
            return Feature.class;
        }
    };

    private FeatureRegistry() {
        throw new UnsupportedOperationException("The creation of instances is prohibited");
    }
}
