package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.screenswitcher.ScreenManager;
import lombok.NonNull;

import java.net.URL;
import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class SettingsScreenFeature extends TileScreenFeature {
    protected SettingsScreenFeature(@NonNull String id, boolean mandatory, boolean enabled, @NonNull URL imageURL,
                                    @NonNull String descriptionResourceKey, @NonNull Consumer<ScreenManager> action) {
        super(id, mandatory, enabled, imageURL, descriptionResourceKey, action);
    }
}
