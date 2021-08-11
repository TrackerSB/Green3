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

    protected WelcomeScreenFeature(@NonNull String id, boolean enabled, @NonNull URL imageURL,
                                   @NonNull String descriptionResourceKey) {
        super(id, enabled);
        this.imageURL = imageURL;
        this.descriptionResourceKey = descriptionResourceKey;
    }

    public URL getImageURL() {
        return imageURL;
    }

    public String getDescriptionResourceKey() {
        return descriptionResourceKey;
    }
}
