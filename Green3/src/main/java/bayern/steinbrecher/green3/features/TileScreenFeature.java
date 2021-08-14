package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.screenswitcher.ScreenManager;
import lombok.NonNull;

import java.net.URL;
import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class TileScreenFeature extends Feature {
    private final URL imageURL;
    private final Consumer<ScreenManager> action;

    protected TileScreenFeature(@NonNull String id, @NonNull String name, boolean mandatory, boolean enabled,
                                @NonNull URL imageURL, @NonNull Consumer<ScreenManager> action) {
        super(id, name, mandatory, enabled);
        this.imageURL = imageURL;
        this.action = action;
    }

    public URL getImageURL() {
        return imageURL;
    }

    public Consumer<ScreenManager> getAction() {
        return action;
    }
}
