package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.about.AboutScreen;
import bayern.steinbrecher.green3.screens.featureSelection.FeatureSelectionScreen;
import bayern.steinbrecher.green3.screens.settings.SettingsScreen;
import bayern.steinbrecher.green3.screens.welcome.WelcomeScreen;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import javafx.application.Platform;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public final class FeatureRegistry {
    private static final Logger LOGGER = Logger.getLogger(FeatureRegistry.class.getName());
    private static final Collection<Feature> registeredFeatures = new ArrayList<>();
    private static final ResourceBundle resources
            = ResourceBundle.getBundle("bayern.steinbrecher.green3.features.Features");

    static {
        add(new WelcomeScreenFeature("WelcomeSettings",
                new FeatureDescription(
                        resources.getString("settings"), WelcomeScreen.class.getResource("settings.png"), true),
                true, sm -> {
            try {
                sm.switchTo(new SettingsScreen());
            } catch (ScreenSwitchFailedException ex) {
                LOGGER.log(Level.SEVERE, "Could not open settings screen", ex);
            }
        }));
        add(new WelcomeScreenFeature("WelcomeCredits",
                new FeatureDescription(
                        resources.getString("about"), WelcomeScreen.class.getResource("teamwork.png"), true),
                true, sm -> {
            try {
                sm.switchTo(new AboutScreen());
            } catch (ScreenSwitchFailedException ex) {
                LOGGER.log(Level.SEVERE, "Could not open about screen", ex);
            }
        }));
        add(new WelcomeScreenFeature("WelcomeExit",
                new FeatureDescription(
                        resources.getString("exit"), WelcomeScreen.class.getResource("power.png"), true),
                true, sm -> Platform.exit()));
        add(new SettingsScreenFeature("SettingsBack",
                new FeatureDescription(
                        resources.getString("back"), SettingsScreen.class.getResource("back.png"), true),
                true, ScreenManager::switchBack));
        add(new SettingsScreenFeature("SettingsFeatures",
                new FeatureDescription(
                        resources.getString("features"), SettingsScreen.class.getResource("itemsClipboard.png"), true),
                true, sm -> {
            try {
                sm.switchTo(new FeatureSelectionScreen());
            } catch (ScreenSwitchFailedException ex) {
                LOGGER.log(Level.SEVERE, "Could not open feature settings", ex);
            }
        }));
    }

    private FeatureRegistry() {
        throw new UnsupportedOperationException("The creation of instances is prohibited");
    }

    public static void add(@NonNull Feature feature) {
        assert registeredFeatures.stream().noneMatch(f -> f.getId().equalsIgnoreCase(feature.getId()))
                : String.format("Feature with id \"%s\" already registered", feature.getId());
        registeredFeatures.add(feature);
    }

    public static <C extends Feature> Collection<C> find(@NonNull Class<C> type) {
        return registeredFeatures.stream()
                .filter(f -> type == f.getClass())
                .map(type::cast)
                .toList();
    }

    public static <C extends Feature> Collection<C> findSub(@NonNull Class<C> type) {
        return registeredFeatures.stream()
                .filter(f -> type.isAssignableFrom(f.getClass()))
                .map(type::cast)
                .toList();
    }
}
