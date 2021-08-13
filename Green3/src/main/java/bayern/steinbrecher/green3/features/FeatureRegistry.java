package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.about.AboutScreen;
import bayern.steinbrecher.green3.screens.welcome.WelcomeScreen;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import javafx.application.Platform;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public final class FeatureRegistry {
    private static final Logger LOGGER = Logger.getLogger(FeatureRegistry.class.getName());
    private static final Collection<Feature> registeredFeatures = new ArrayList<>();

    static {
        add(new WelcomeScreenFeature("Credits", true,
                WelcomeScreen.class.getResource("teamwork.png"), "about", sm -> {
            try {
                sm.switchTo(new AboutScreen());
            } catch (ScreenSwitchFailedException ex) {
                LOGGER.log(Level.SEVERE, "Could not open about screen", ex);
            }
        }));
        add(new WelcomeScreenFeature("Exit", true,
                WelcomeScreen.class.getResource("power.png"), "exit", sm -> Platform.exit()));
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
}
