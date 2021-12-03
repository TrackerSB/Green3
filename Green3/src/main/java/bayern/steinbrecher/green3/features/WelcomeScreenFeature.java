package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.welcome.WelcomeScreen;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class WelcomeScreenFeature extends TileScreenFeature {
    WelcomeScreenFeature(@NonNull FeatureDescription description, boolean enabled,
                         @NonNull Consumer<ScreenManager> action) {
        super(description, enabled, action);
    }

    @Override
    protected @NonNull Class<?> getAssociatedClass() {
        return WelcomeScreen.class;
    }
}
