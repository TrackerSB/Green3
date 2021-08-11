package bayern.steinbrecher.green3.features;

import lombok.NonNull;

import java.net.URL;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class WelcomeScreenFeature extends Feature {
    private final URL imageURL;
    private final String descriptionResourceKey;
    private final Runnable action;

    protected WelcomeScreenFeature(@NonNull String id, boolean enabled, @NonNull URL imageURL,
                                   @NonNull String descriptionResourceKey, @NonNull Runnable action) {
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

    public Runnable getAction() {
        return action;
    }
}
