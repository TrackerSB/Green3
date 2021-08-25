package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.screenswitcher.ScreenManager;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class SettingsScreenFeature extends TileScreenFeature {
    SettingsScreenFeature(@NonNull String id, @NonNull FeatureDescription description,
                          boolean enabled, @NonNull Consumer<ScreenManager> action) {
        super(id, description, enabled, action);
    }
}
