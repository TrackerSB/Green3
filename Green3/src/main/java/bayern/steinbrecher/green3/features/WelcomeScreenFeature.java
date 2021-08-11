package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.screenswitcher.ScreenManager;
import lombok.NonNull;

import java.net.URL;
import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class WelcomeScreenFeature extends Feature {
    private final URL imageURL;
    private final String descriptionResourceKey;
    private final Consumer<ScreenManager> action;

    protected WelcomeScreenFeature(@NonNull String id, boolean enabled, @NonNull URL imageURL,
                                   @NonNull String descriptionResourceKey, @NonNull Consumer<ScreenManager> action) {
        super(id, enabled);
        this.imageURL = imageURL;
        this.descriptionResourceKey = descriptionResourceKey;
        this.action = action;
    }

    public URL getImageURL() {
        return imageURL;
    }

    public String getDescriptionResourceKey() {
        return descriptionResourceKey;
    }

    public Consumer<ScreenManager> getAction() {
        return action;
    }
}
