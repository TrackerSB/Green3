package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.screenswitcher.ScreenManager;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class TileScreenFeature extends Feature {
    private final Consumer<ScreenManager> action;

    protected TileScreenFeature(@NonNull String id, @NonNull FeatureDescription description, boolean enabled,
                                @NonNull Consumer<ScreenManager> action) {
        super(id, description, enabled);
        this.action = action;
    }

    public Consumer<ScreenManager> getAction() {
        return action;
    }
}
