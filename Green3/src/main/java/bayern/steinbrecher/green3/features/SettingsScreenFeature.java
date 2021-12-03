package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.settings.SettingsScreen;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class SettingsScreenFeature extends TileScreenFeature {
    SettingsScreenFeature(@NonNull FeatureDescription description, boolean enabled,
                          @NonNull Consumer<ScreenManager> action) {
        super(description, enabled, action);
    }

    @Override
    @NonNull
    protected Class<?> getAssociatedClass() {
        return SettingsScreen.class;
    }
}
